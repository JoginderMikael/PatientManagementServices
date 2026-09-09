import { StatusBadge } from "../../components/data/StatusBadge";

export function formatPortalDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.valueOf())
    ? "Date unavailable"
    : new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(date);
}

export function formatPortalMoney(value: number): string {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "KES",
  }).format(value);
}

export function PortalStatus({ value }: { value: string }) {
  const normalized = value.toUpperCase();
  const tone = ["FULFILLED", "SETTLED", "SCHEDULED", "ACTIVE"].includes(
    normalized,
  )
    ? "success"
    : ["CANCELLED", "DECLINED"].includes(normalized)
      ? "danger"
      : ["REQUESTED", "READY_FOR_REVIEW", "SUBMITTED"].includes(normalized)
        ? "warning"
        : "neutral";
  return <StatusBadge tone={tone}>{normalized.replace(/_/g, " ")}</StatusBadge>;
}
