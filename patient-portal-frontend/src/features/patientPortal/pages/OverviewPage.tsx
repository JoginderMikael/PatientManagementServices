import { Link } from "react-router-dom";
import { PageHeader } from "../../../components/PageHeader";
import { DefinitionList } from "../../../components/data/DefinitionList";
import { Alert } from "../../../components/feedback/Alert";
import { PortalBoundary, usePortalOverview } from "../portalState";
import { PortalSection } from "../components";
import { PortalStatus } from "../format";

export function OverviewPage() {
  const state = usePortalOverview();
  const overview = state.data;
  return (
    <>
      <PageHeader
        eyebrow="Patient portal"
        title="Overview"
        description="A concise view of your requests and payment activity."
      />
      <PortalBoundary state={state}>
        {overview && (
          <div className="portal-stack">
            {state.isFetching && (
              <Alert title="Refreshing portal information">
                Showing the latest available information while an update
                completes.
              </Alert>
            )}
            <PortalSection
              title="Account summary"
              description="Only activity linked to your authenticated portal identity is shown."
            >
              <DefinitionList
                items={[
                  {
                    term: "Account status",
                    description: (
                      <PortalStatus value={overview.accountStatus} />
                    ),
                  },
                  {
                    term: "Appointment requests",
                    description: String(overview.appointments.length),
                  },
                  {
                    term: "Record requests",
                    description: String(overview.recordRequests.length),
                  },
                  {
                    term: "Payments",
                    description: String(overview.payments.length),
                  },
                ]}
              />
            </PortalSection>
            <div className="portal-summary-grid">
              <PortalSection title="Appointments">
                <p>
                  {overview.appointments.length
                    ? `${overview.appointments.filter((item) => item.status === "REQUESTED").length} request(s) awaiting resolution.`
                    : "No appointment requests yet."}
                </p>
                <Link to="/patient/appointments">View appointments</Link>
              </PortalSection>
              <PortalSection title="Health records">
                <p>
                  {overview.recordRequests.length
                    ? `${overview.recordRequests.filter((item) => item.status === "FULFILLED").length} release(s) ready to download.`
                    : "No record requests yet."}
                </p>
                <Link to="/patient/records">View health records</Link>
              </PortalSection>
              <PortalSection title="Payments">
                <p>
                  {overview.payments.length
                    ? `${overview.payments.length} payment submission(s) recorded.`
                    : "No payment activity yet."}
                </p>
                <Link to="/patient/billing">View bills and payments</Link>
              </PortalSection>
            </div>
          </div>
        )}
      </PortalBoundary>
    </>
  );
}
