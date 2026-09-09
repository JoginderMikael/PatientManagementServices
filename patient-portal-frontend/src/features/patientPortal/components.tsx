import React from "react";
import { isApiError } from "../../api/errors";
import { Alert } from "../../components/feedback/Alert";
import { EmptyState } from "../../components/feedback/EmptyState";

export function PortalSection({
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

export function NoActivity({
  title,
  message,
}: {
  title: string;
  message: string;
}) {
  return <EmptyState title={title} message={message} />;
}

export function MutationError({
  error,
  conflictMessage,
}: {
  error: unknown;
  conflictMessage?: string;
}) {
  if (!error) return null;
  const conflict = isApiError(error) && error.status === 409;
  return (
    <Alert
      title={conflict ? "Current state needs review" : "Request not completed"}
      tone={conflict ? "warning" : "danger"}
    >
      {conflict && conflictMessage
        ? conflictMessage
        : isApiError(error)
          ? error.message
          : "The request could not be completed. Try again."}
      {isApiError(error) && error.requestId && (
        <span className="request-id-inline">
          {" "}
          Request ID: {error.requestId}
        </span>
      )}
    </Alert>
  );
}
