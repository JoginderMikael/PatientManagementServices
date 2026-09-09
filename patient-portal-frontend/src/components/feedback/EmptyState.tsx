import React from "react";

export function EmptyState({
  title,
  message,
  action,
}: {
  title: string;
  message: string;
  action?: React.ReactNode;
}) {
  return (
    <section className="empty-state">
      <span className="empty-mark" aria-hidden="true">
        0
      </span>
      <h2>{title}</h2>
      <p>{message}</p>
      {action}
    </section>
  );
}
