import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../api/queryKeys";
import { isApiError } from "../../api/errors";
import { useAuth } from "../../auth/AuthProvider";
import { ErrorState } from "../../components/feedback/ErrorState";
import { Skeleton } from "../../components/feedback/Skeleton";
import { getPortalOverview } from "./api";

const uuidPattern =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function usePortalOverview() {
  const { session } = useAuth();
  const claimPatientId = session?.user.patientId;
  const patientId =
    claimPatientId && uuidPattern.test(claimPatientId)
      ? claimPatientId
      : session?.user.subject && uuidPattern.test(session.user.subject)
        ? session.user.subject
        : null;
  const token = session?.token ?? "";
  const query = useQuery({
    queryKey: patientId
      ? queryKeys.patientResource(patientId, "portal-overview")
      : ["portal-overview", "unavailable"],
    queryFn: () => getPortalOverview(patientId!, token),
    enabled: Boolean(patientId && token),
  });
  return { patientId, token, ...query };
}

export function PortalBoundary({
  state,
  children,
}: {
  state: ReturnType<typeof usePortalOverview>;
  children: React.ReactNode;
}) {
  if (!state.patientId)
    return (
      <ErrorState
        title="Portal identity unavailable"
        message="Your signed-in account is not linked to an opaque patient identifier. Contact registration support."
      />
    );
  if (state.isLoading)
    return <Skeleton label="Loading your patient portal" lines={5} />;
  if (state.error) {
    const denied = isApiError(state.error) && state.error.status === 403;
    return (
      <ErrorState
        title={
          denied
            ? "Patient access not available"
            : "Portal information unavailable"
        }
        message={
          denied
            ? "This account is not authorized for the requested patient portal."
            : isApiError(state.error)
              ? state.error.message
              : "Your portal information could not be loaded."
        }
        requestId={isApiError(state.error) ? state.error.requestId : undefined}
        onRetry={
          state.error instanceof Error && !denied
            ? () => state.refetch()
            : undefined
        }
      />
    );
  }
  return <>{children}</>;
}
