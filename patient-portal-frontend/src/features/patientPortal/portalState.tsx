import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../api/queryKeys";
import { isApiError } from "../../api/errors";
import { useAuth } from "../../auth/AuthProvider";
import { ErrorState } from "../../components/feedback/ErrorState";
import { Skeleton } from "../../components/feedback/Skeleton";
import { getPortalOverview } from "./api";

export function usePortalOverview() {
  const { session } = useAuth();
  const token = session?.token ?? "";
  const query = useQuery({
    queryKey: queryKeys.portalOverview(),
    queryFn: () => getPortalOverview(token),
    enabled: Boolean(token),
  });
  return { patientId: query.data?.patientId ?? null, token, ...query };
}

export function PortalBoundary({
  state,
  children,
}: {
  state: ReturnType<typeof usePortalOverview>;
  children: React.ReactNode;
}) {
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
  if (!state.patientId)
    return (
      <ErrorState
        title="Portal identity unavailable"
        message="Your signed-in account is not linked to an active patient record. Contact registration support."
      />
    );
  return <>{children}</>;
}
