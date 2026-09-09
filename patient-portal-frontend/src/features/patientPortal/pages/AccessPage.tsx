import { PageHeader } from "../../../components/PageHeader";
import { DefinitionList } from "../../../components/data/DefinitionList";
import { Alert } from "../../../components/feedback/Alert";
import { useAuth } from "../../../auth/AuthProvider";
import { PortalSection } from "../components";
import { PortalBoundary, usePortalOverview } from "../portalState";
import { PortalStatus } from "../format";

export function AccessPage() {
  const { session } = useAuth();
  const state = usePortalOverview();
  return (
    <>
      <PageHeader
        eyebrow="Patient portal"
        title="Profile and proxy access"
        description="Review the portal identity used for this secure session."
      />
      <PortalBoundary state={state}>
        {state.data && (
          <div className="portal-stack">
            <PortalSection title="Portal profile">
              <DefinitionList
                items={[
                  {
                    term: "Account",
                    description: session?.user.email ?? "Unavailable",
                  },
                  {
                    term: "Role",
                    description: session?.user.role ?? "Unavailable",
                  },
                  {
                    term: "Account status",
                    description: (
                      <PortalStatus value={state.data.accountStatus} />
                    ),
                  },
                  {
                    term: "Patient identifier",
                    description: <code>{state.patientId}</code>,
                  },
                ]}
              />
            </PortalSection>
            <PortalSection title="Proxy access">
              <Alert
                title="Proxy management is not yet available"
                tone="warning"
              >
                The gateway can create and revoke a known grant, but it does not
                yet provide a safe list of verified proxy identities or existing
                grants. No control is shown until those contracts prevent blind
                or incorrect access changes.
              </Alert>
            </PortalSection>
          </div>
        )}
      </PortalBoundary>
    </>
  );
}
