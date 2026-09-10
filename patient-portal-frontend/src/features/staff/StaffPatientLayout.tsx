import { useEffect, useRef } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Navigate, Outlet, useNavigate, useParams } from "react-router-dom";
import { queryKeys } from "../../api/queryKeys";
import { useAuth } from "../../auth/AuthProvider";
import { PatientBanner } from "../../components/layout/PatientBanner";
import { getPatient } from "./api";
import { QueryState } from "./components";

export function StaffPatientLayout() {
  const { patientId } = useParams();
  const { session } = useAuth();
  const client = useQueryClient();
  const previous = useRef<string | undefined>(undefined);
  const navigate = useNavigate();
  useEffect(() => {
    const old = previous.current;
    if (old && old !== patientId) {
      client.cancelQueries({ queryKey: queryKeys.patient(old) });
      client.removeQueries({ queryKey: queryKeys.patient(old) });
    }
    previous.current = patientId;
  }, [client, patientId]);
  const query = useQuery({
    queryKey: queryKeys.patient(patientId ?? ""),
    queryFn: ({ signal }) =>
      getPatient(patientId as string, session!.token, signal),
    enabled: Boolean(patientId && session),
  });
  if (!patientId) return <Navigate to="/staff/patients" replace />;
  return (
    <QueryState
      loading={query.isLoading}
      error={query.error}
      onRetry={() => query.refetch()}
    >
      {query.data && (
        <>
          <PatientBanner
            patient={{
              id: query.data.id,
              name: query.data.name,
              mrn: query.data.mrn,
              dateOfBirth: query.data.dateOfBirth,
              lifecycleStatus: query.data.status,
              accessState: "Authorized staff context",
            }}
            onChangePatient={() => navigate("/staff/patients")}
          />
          <div className="patient-workspace">
            <Outlet context={{ patient: query.data }} />
          </div>
        </>
      )}
    </QueryState>
  );
}
