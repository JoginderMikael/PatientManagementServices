"""Synthetic PostgreSQL backup/restore drill. Never connects to an existing database."""
import hashlib
import json
import pathlib
import re
import subprocess
import time
import uuid

ROOT = pathlib.Path(__file__).resolve().parents[2]
REPORT = ROOT / "integration-tests" / "target" / "phase4-restore.json"


def run(*args, data=None, check=True):
    result = subprocess.run(list(args), input=data, capture_output=True, timeout=120)
    if check and result.returncode:
        raise RuntimeError(result.stderr.decode(errors="replace"))
    return result


def main():
    container = "phase4-drill-" + uuid.uuid4().hex
    created = False
    started = time.monotonic()
    report = {"synthetic": True, "passed": False}
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    try:
        run("docker", "run", "-d", "--name", container, "--network", "none",
            "-e", "POSTGRES_HOST_AUTH_METHOD=trust", "postgres:16-alpine")
        created = True
        for _ in range(60):
            if run("docker", "exec", container, "pg_isready", "-U", "postgres", check=False).returncode == 0:
                break
            time.sleep(1)
        else:
            raise RuntimeError("PostgreSQL did not become ready")

        def sql(database, statement):
            return run("docker", "exec", "-i", container, "psql", "-X", "-q", "-A", "-t",
                       "-v", "ON_ERROR_STOP=1", "-U", "postgres", "-d", database,
                       data=statement.encode()).stdout

        sql("postgres", "CREATE DATABASE drill_source; CREATE DATABASE drill_restored;")
        modules = []
        for migrations in sorted(ROOT.glob("*-service/src/main/resources/db/migration")):
            module = migrations.parents[4].name
            schema = module.replace("-", "_")
            files = sorted(migrations.glob("V*__*.sql"), key=lambda p: int(p.name.split("__")[0][1:]))
            sql("drill_source", f'CREATE SCHEMA "{schema}";')
            for migration in files:
                sql("drill_source", f'SET search_path TO "{schema}";\n' + migration.read_text(encoding="utf-8"))
            modules.append(module)
        sql("drill_source", "CREATE ROLE drill_runtime;")
        hardening = (ROOT / "ops/security/audit-immutability.sql").read_text(encoding="utf-8")
        sql("drill_source", "SET search_path TO audit_compliance_service;\n" + hardening.replace(':"runtime_role"', '"drill_runtime"'))
        for table in ("audit_event", "compliance_history"):
            for statement in (f"DELETE FROM {table}", f"TRUNCATE {table}"):
                try:
                    sql("drill_source", "SET search_path TO audit_compliance_service;" + statement)
                except RuntimeError as error:
                    if "Evidence is append-only" not in str(error):
                        raise
                else:
                    raise RuntimeError("Evidence mutation was allowed")
        # Nonempty data includes Unicode, nulls, binary values and a foreign key.
        sql("drill_source", """
            CREATE TABLE public.drill_parent(id integer PRIMARY KEY, value text, raw bytea);
            CREATE TABLE public.drill_child(id integer PRIMARY KEY, parent_id integer REFERENCES public.drill_parent(id));
            INSERT INTO public.drill_parent VALUES (1,'Synthetic restore — 验证',decode('00ff01','hex')),(2,NULL,NULL);
            INSERT INTO public.drill_child VALUES (1,1);
        """)
        backup = run("docker", "exec", container, "pg_dump", "-U", "postgres", "-Fc", "--no-owner", "drill_source").stdout
        if not backup.startswith(b"PGDMP"):
            raise RuntimeError("Backup is not a custom-format archive")
        restore_start = time.monotonic()
        run("docker", "exec", "-i", container, "pg_restore", "-U", "postgres", "--exit-on-error",
            "--single-transaction", "--no-owner", "-d", "drill_restored", data=backup)
        restore_seconds = time.monotonic() - restore_start
        tables = sql("drill_source", "SELECT schemaname || '.' || tablename FROM pg_tables WHERE schemaname NOT IN ('pg_catalog','information_schema') ORDER BY 1;").decode().splitlines()
        for table in tables:
            query = f"SELECT row_to_json(t)::text FROM {table} t ORDER BY row_to_json(t)::text;"
            if sql("drill_source", query) != sql("drill_restored", query):
                raise RuntimeError("Restored data mismatch: " + table)
        # Compare catalog definitions, independent of pg_dump ordering and internal OIDs.
        schema_query = """
            SELECT json_build_array('column',table_schema,table_name,column_name,ordinal_position,
                data_type,udt_name,is_nullable,column_default,character_maximum_length)::text
            FROM information_schema.columns WHERE table_schema NOT IN ('pg_catalog','information_schema')
            UNION ALL
            SELECT json_build_array('constraint',n.nspname,c.relname,con.conname,pg_get_constraintdef(con.oid))::text
            FROM pg_constraint con JOIN pg_class c ON c.oid=con.conrelid JOIN pg_namespace n ON n.oid=c.relnamespace
            WHERE n.nspname NOT IN ('pg_catalog','information_schema')
            UNION ALL
            SELECT json_build_array('index',schemaname,tablename,indexname,indexdef)::text
            FROM pg_indexes WHERE schemaname NOT IN ('pg_catalog','information_schema')
            UNION ALL
            SELECT json_build_array('trigger',pg_get_triggerdef(oid))::text FROM pg_trigger WHERE NOT tgisinternal
            ORDER BY 1;
        """
        def normalize_catalog(value):
            # PostgreSQL reparses varchar[] -> text[] casts into per-element casts on restore.
            # Normalize only this exact, equivalent literal-array transformation.
            return re.sub(rb"\(ARRAY\[((?:'[^']*'::character varying)(?:, '[^']*'::character varying)*)\]\)::text\[\]",
                          lambda match: b"ARRAY[" + b", ".join(b"(" + item + b")::text" for item in match[1].split(b", ")) + b"]", value)
        source_catalog = normalize_catalog(sql("drill_source", schema_query))
        restored_catalog = normalize_catalog(sql("drill_restored", schema_query))
        if source_catalog != restored_catalog:
            import difflib
            (REPORT.parent / "phase4-schema-diff.txt").write_text("\n".join(difflib.unified_diff(
                source_catalog.decode().splitlines(), restored_catalog.decode().splitlines())), encoding="utf-8")
            raise RuntimeError("Restored schema mismatch")
        corrupt = run("docker", "exec", "-i", container, "pg_restore", "-U", "postgres", "--exit-on-error",
                      "-d", "drill_restored", data=backup[:20], check=False)
        if corrupt.returncode == 0:
            raise RuntimeError("Truncated archive was not rejected")
        report.update(passed=True, modules=modules, tables_verified=len(tables),
                      backup_sha256=hashlib.sha256(backup).hexdigest(), backup_bytes=len(backup),
                      restore_seconds=round(restore_seconds, 3), corrupt_archive_rejected=True)
        report["evidence_mutation_rejected"] = True
    finally:
        report["elapsed_seconds"] = round(time.monotonic() - started, 3)
        REPORT.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
        if created:
            run("docker", "rm", "-f", "-v", container)
    print(REPORT)


if __name__ == "__main__":
    main()
