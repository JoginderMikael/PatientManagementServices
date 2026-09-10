import { UserRole } from "../auth/tokenClaims";

export interface NavigationItem {
  label: string;
  to: string;
  abbreviation: string;
  roles: readonly UserRole[];
}

const staffTaskRoles: readonly UserRole[] = [
  "ADMIN",
  "CLINICIAN",
  "NURSE",
  "RECEPTIONIST",
  "BILLING_STAFF",
  "PHARMACIST",
  "LAB_STAFF",
];

export const patientNavigation: readonly NavigationItem[] = [
  {
    label: "Overview",
    to: "/patient/overview",
    abbreviation: "OV",
    roles: ["PATIENT", "ADMIN"],
  },
  {
    label: "Appointments",
    to: "/patient/appointments",
    abbreviation: "AP",
    roles: ["PATIENT", "ADMIN"],
  },
  {
    label: "Health records",
    to: "/patient/records",
    abbreviation: "HR",
    roles: ["PATIENT", "ADMIN"],
  },
  {
    label: "Bills and payments",
    to: "/patient/billing",
    abbreviation: "BP",
    roles: ["PATIENT", "ADMIN"],
  },
  {
    label: "Profile and proxy access",
    to: "/patient/access",
    abbreviation: "PA",
    roles: ["PATIENT", "ADMIN"],
  },
];

export const staffNavigation: readonly NavigationItem[] = [
  {
    label: "Work queue",
    to: "/staff/work-queue",
    abbreviation: "WQ",
    roles: staffTaskRoles,
  },
  {
    label: "Patients",
    to: "/staff/patients",
    abbreviation: "PT",
    roles: ["ADMIN", "CLINICIAN", "REGISTRATION_STAFF"],
  },
  {
    label: "Schedule",
    to: "/staff/schedule",
    abbreviation: "SC",
    roles: ["ADMIN", "CLINICIAN", "REGISTRATION_STAFF"],
  },
  {
    label: "Clinical chart",
    to: "/staff/clinical-chart",
    abbreviation: "CC",
    roles: ["ADMIN", "CLINICIAN", "NURSE"],
  },
  {
    label: "Billing",
    to: "/staff/billing",
    abbreviation: "BL",
    roles: ["ADMIN", "BILLING_STAFF"],
  },
  {
    label: "Insurance",
    to: "/staff/insurance",
    abbreviation: "IN",
    roles: ["ADMIN", "BILLING_STAFF"],
  },
  {
    label: "Pharmacy",
    to: "/staff/pharmacy",
    abbreviation: "RX",
    roles: ["ADMIN", "PHARMACIST"],
  },
  {
    label: "Inventory",
    to: "/staff/inventory",
    abbreviation: "IV",
    roles: ["ADMIN", "PHARMACIST"],
  },
  {
    label: "Notifications",
    to: "/staff/notifications",
    abbreviation: "NT",
    roles: ["ADMIN", "CLINICIAN", "REGISTRATION_STAFF", "BILLING_STAFF"],
  },
  {
    label: "Compliance",
    to: "/staff/compliance",
    abbreviation: "CP",
    roles: ["PRIVACY_OFFICER"],
  },
  {
    label: "Audit evidence",
    to: "/staff/audit",
    abbreviation: "AU",
    roles: ["ADMIN", "AUDITOR"],
  },
  {
    label: "Reports",
    to: "/staff/reports",
    abbreviation: "RP",
    roles: ["ADMIN", "ANALYST"],
  },
];

export function navigationFor(
  role: UserRole,
  surface: "patient" | "staff",
): readonly NavigationItem[] {
  const items = surface === "patient" ? patientNavigation : staffNavigation;
  return items.filter((item) => item.roles.includes(role));
}

export function startRouteFor(role: UserRole): string {
  if (role === "PATIENT") return "/patient/overview";
  return navigationFor(role, "staff")[0]?.to ?? "/forbidden";
}

export const patientRoles: readonly UserRole[] = ["PATIENT", "ADMIN"];
export const staffRoles: readonly UserRole[] = [
  "ADMIN",
  "CLINICIAN",
  "NURSE",
  "REGISTRATION_STAFF",
  "RECEPTIONIST",
  "BILLING_STAFF",
  "PHARMACIST",
  "LAB_STAFF",
  "PRIVACY_OFFICER",
  "AUDITOR",
  "ANALYST",
];
