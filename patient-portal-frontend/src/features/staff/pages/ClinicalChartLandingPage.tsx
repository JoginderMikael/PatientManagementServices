import { Link } from "react-router-dom";
import { useAuth } from "../../../auth/AuthProvider";
import { Alert } from "../../../components/feedback/Alert";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { PageHeader } from "../../../components/PageHeader";

export function ClinicalChartLandingPage() {
  const { session } = useAuth();
  const nurse = session?.user.role === "NURSE";
  return (
    <>
      <PageHeader
        eyebrow="Staff operations"
        title="Clinical chart"
        description="Open a chart only from an explicit patient context."
      />
      {nurse ? (
        <Alert
          tone="warning"
          title="Patient identity lookup is not available to this role"
        >
          The current patient service does not authorize nurses to retrieve the
          identity banner. Safety alerts remain available from assigned queue
          tasks, but a safe chart cannot be opened until the backend provides a
          minimum-necessary nurse patient-context response.
        </Alert>
      ) : (
        <div className="surface-card">
          <EmptyState
            title="Select a patient first"
            message="Search the patient registry, verify identity and then open the clinical chart."
          />
          <p className="center-action">
            <Link className="button button--primary" to="/staff/patients">
              Search patients
            </Link>
          </p>
        </div>
      )}
    </>
  );
}
