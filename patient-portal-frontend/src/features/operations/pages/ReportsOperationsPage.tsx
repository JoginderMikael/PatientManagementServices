import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import { Field, TextField } from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { createReport, listCohorts, listReports, listTrends } from "../api";
import {
  formatDate,
  MutationStatus,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";
export function ReportsOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [form, setForm] = useState({
    reportType: "",
    periodStart: "",
    periodEnd: "",
  });
  const trends = useQuery({
    queryKey: ["operations", "analytics", "trends"],
    queryFn: ({ signal }) => listTrends(session!.token, signal),
  });
  const cohorts = useQuery({
    queryKey: ["operations", "analytics", "cohorts"],
    queryFn: ({ signal }) => listCohorts(session!.token, signal),
  });
  const reports = useQuery({
    queryKey: ["operations", "analytics", "reports"],
    queryFn: ({ signal }) => listReports(session!.token, signal),
  });
  const create = useMutation({
    mutationFn: () =>
      createReport(
        {
          reportType: form.reportType,
          ...(form.periodStart && { periodStart: form.periodStart }),
          ...(form.periodEnd && { periodEnd: form.periodEnd }),
        },
        session!.token,
      ),
    onSuccess: () => {
      setForm({ reportType: "", periodStart: "", periodEnd: "" });
      client.invalidateQueries({
        queryKey: ["operations", "analytics", "reports"],
      });
    },
  });
  const submit = (e: FormEvent) => {
    e.preventDefault();
    create.mutate();
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Population reports"
        description="Review aggregate trends and request regulatory-report generation without exposing patient-level records."
      />
      <Alert tone="warning" title="Aggregation boundary">
        Do not infer or export patient identities from small cohort cells. This
        UI intentionally provides no raw patient drill-down or browser export.
      </Alert>
      <StaffSection title="Request regulatory report">
        <form className="form-grid" onSubmit={submit}>
          <Field id="report-type" label="Report type" required>
            <TextField
              required
              value={form.reportType}
              onChange={(e) => setForm({ ...form, reportType: e.target.value })}
            />
          </Field>
          <Field id="report-start" label="Period start">
            <TextField
              type="date"
              value={form.periodStart}
              onChange={(e) =>
                setForm({ ...form, periodStart: e.target.value })
              }
            />
          </Field>
          <Field id="report-end" label="Period end">
            <TextField
              type="date"
              min={form.periodStart}
              value={form.periodEnd}
              onChange={(e) => setForm({ ...form, periodEnd: e.target.value })}
            />
          </Field>
          <div>
            <Button type="submit" disabled={create.isPending}>
              Generate report
            </Button>
          </div>
        </form>
        <MutationStatus
          error={create.error}
          success={
            create.isSuccess ? "Report generation was accepted." : undefined
          }
        />
        <QueryState
          loading={reports.isLoading}
          error={reports.error}
          empty={!reports.data?.length}
          onRetry={() => reports.refetch()}
        >
          <Table
            caption="Regulatory reports"
            headers={["Type", "Period", "Status", "Created"]}
          >
            {reports.data?.map((r) => (
              <tr key={r.id}>
                <td>{r.reportType}</td>
                <td>
                  {r.periodStart ?? "—"} – {r.periodEnd ?? "—"}
                </td>
                <td>
                  <StatusBadge tone={statusTone(r.status)}>
                    {r.status}
                  </StatusBadge>
                </td>
                <td>{formatDate(r.createdAt)}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection title="Population health trends">
        <QueryState
          loading={trends.isLoading}
          error={trends.error}
          empty={!trends.data?.length}
          onRetry={() => trends.refetch()}
        >
          <Table
            caption="Population trends"
            headers={["Metric", "Segment", "Period", "Value"]}
          >
            {trends.data?.map((t) => (
              <tr key={t.id}>
                <td>{t.metric}</td>
                <td>{t.segment}</td>
                <td>
                  {t.periodStart ?? "—"} – {t.periodEnd ?? "—"}
                </td>
                <td>{t.value}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection title="Chronic-disease cohorts">
        <QueryState
          loading={cohorts.isLoading}
          error={cohorts.error}
          empty={!cohorts.data?.length}
          onRetry={() => cohorts.refetch()}
        >
          <Table
            caption="Disease cohorts"
            headers={["Condition", "Risk level", "Population"]}
          >
            {cohorts.data?.map((c) => (
              <tr key={c.id}>
                <td>{c.condition}</td>
                <td>{c.riskLevel}</td>
                <td>
                  {c.patientCount < 5 ? "Suppressed (<5)" : c.patientCount}
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
