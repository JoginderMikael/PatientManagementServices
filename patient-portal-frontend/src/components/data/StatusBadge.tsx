import React from "react";

export function StatusBadge({
  tone = "neutral",
  children,
}: {
  tone?: "neutral" | "info" | "success" | "warning" | "danger";
  children: React.ReactNode;
}) {
  return (
    <span className={`status status--${tone}`}>
      <span aria-hidden="true">●</span>
      {children}
    </span>
  );
}
