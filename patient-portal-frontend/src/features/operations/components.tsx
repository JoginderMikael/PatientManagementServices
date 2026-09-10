export { MutationStatus, QueryState, StaffSection } from "../staff/components";
export const formatDate = (value?: string) =>
  value
    ? new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(new Date(value))
    : "—";
export const formatMoney = (value: number, currency = "USD") =>
  new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: currency || "USD",
  }).format(value);
export const statusTone = (
  status: string,
): "neutral" | "info" | "success" | "warning" | "danger" => {
  if (
    ["ACTIVE", "APPROVED", "PAID", "SENT", "RECONCILED", "CLOSED"].includes(
      status,
    )
  )
    return "success";
  if (["DENIED", "FAILED", "REVOKED", "OVERDUE"].includes(status))
    return "danger";
  if (
    ["PENDING", "OPEN", "INVESTIGATING", "REMEDIATING", "LOW_STOCK"].includes(
      status,
    )
  )
    return "warning";
  return "neutral";
};
