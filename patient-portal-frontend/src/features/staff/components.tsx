import React from "react";
import { isApiError } from "../../api/errors";
import { Alert } from "../../components/feedback/Alert";
import { EmptyState } from "../../components/feedback/EmptyState";
import { ErrorState } from "../../components/feedback/ErrorState";
import { Skeleton } from "../../components/feedback/Skeleton";

export function StaffSection({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
}) {
  return (
    <section className="portal-section surface-card">
      <header>
        <h2>{title}</h2>
        {description && <p>{description}</p>}
      </header>
      <div className="portal-section__body">{children}</div>
    </section>
  );
}
export function QueryState({
  loading,
  error,
  empty,
  onRetry,
  children,
}: {
  loading: boolean;
  error: unknown;
  empty?: boolean;
  onRetry(): void;
  children: React.ReactNode;
}) {
  if (loading) return <Skeleton lines={4} />;
  if (error) {
    const denied = isApiError(error) && error.status === 403;
    return (
      <ErrorState
        title={denied ? "Access denied" : "Workflow unavailable"}
        message={
          denied
            ? "Your current role or patient access does not permit this view."
            : isApiError(error)
              ? error.message
              : "The service could not load this workflow."
        }
        requestId={isApiError(error) ? error.requestId : undefined}
        onRetry={denied ? undefined : onRetry}
      />
    );
  }
  if (empty)
    return (
      <EmptyState
        title="No matching records"
        message="Adjust the filters or create the first permitted record."
      />
    );
  return <>{children}</>;
}
export function MutationStatus({
  error,
  success,
  conflict,
}: {
  error: unknown;
  success?: string;
  conflict?: string;
}) {
  if (success)
    return (
      <Alert tone="success" title="Update confirmed">
        {success}
      </Alert>
    );
  if (!error) return null;
  const stale = isApiError(error) && error.status === 409;
  return (
    <Alert
      tone={stale ? "warning" : "danger"}
      title={stale ? "Current state needs review" : "Update not completed"}
    >
      {stale
        ? (conflict ??
          "The record changed. Current data has been refreshed; review it before trying again.")
        : isApiError(error)
          ? error.message
          : "The update could not be completed."}
    </Alert>
  );
}
