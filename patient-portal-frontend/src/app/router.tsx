import { Navigate, Route, Routes } from "react-router-dom";
import { RequireAuth } from "../auth/RequireAuth";
import { RequireRole } from "../auth/RequireRole";
import { AppShell } from "../components/layout/AppShell";
import { ForbiddenPage } from "../features/foundation/pages/ForbiddenPage";
import { LoginPage } from "../features/foundation/pages/LoginPage";
import { NotFoundPage } from "../features/foundation/pages/NotFoundPage";
import { WorkspacePage } from "../features/foundation/pages/WorkspacePage";
import {
  patientNavigation,
  patientRoles,
  staffNavigation,
  staffRoles,
} from "./routePolicy";

const patientPages = [
  [
    "overview",
    "Overview",
    "Appointments, record requests and payment activity.",
  ],
  ["appointments", "Appointments", "Request and review appointments."],
  [
    "records",
    "Health records",
    "Request and securely download fulfilled records.",
  ],
  [
    "billing",
    "Bills and payments",
    "Review invoices and submit payment intents.",
  ],
  [
    "access",
    "Profile and proxy access",
    "Review profile and narrowly scoped proxy grants.",
  ],
] as const;

const staffPages = [
  ["work-queue", "Work queue", "Review assigned and permitted queue work."],
  ["patients", "Patients", "Search, register and select patient context."],
  ["schedule", "Schedule", "Review availability and appointment lists."],
  [
    "clinical-chart",
    "Clinical chart",
    "Open a patient context before viewing clinical records.",
  ],
  ["billing", "Billing", "Review patient accounts, invoices and postings."],
  ["insurance", "Insurance", "Manage coverage, claims and remittances."],
  [
    "pharmacy",
    "Pharmacy",
    "Review prescriptions and controlled dispensing steps.",
  ],
  ["inventory", "Inventory", "Review stock, batches and movements."],
  [
    "notifications",
    "Notifications",
    "Review permitted operational delivery activity.",
  ],
  [
    "compliance",
    "Compliance",
    "Manage privacy cases, reviews and legal holds.",
  ],
  ["audit", "Audit evidence", "Review append-only access evidence."],
  [
    "reports",
    "Reports",
    "Review aggregate operational and population-health measures.",
  ],
] as const;

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route element={<RequireRole allow={patientRoles} />}>
          <Route path="/patient" element={<AppShell surface="patient" />}>
            <Route index element={<Navigate to="overview" replace />} />
            {patientPages.map(([path, title, description]) => {
              const policy = patientNavigation.find(
                (item) => item.to === `/patient/${path}`,
              )!;
              return (
                <Route
                  key={path}
                  path={path}
                  element={
                    <RequireRole allow={policy.roles}>
                      <WorkspacePage
                        eyebrow="Patient portal"
                        title={title}
                        description={description}
                      />
                    </RequireRole>
                  }
                />
              );
            })}
          </Route>
        </Route>
        <Route element={<RequireRole allow={staffRoles} />}>
          <Route path="/staff" element={<AppShell surface="staff" />}>
            <Route index element={<Navigate to="work-queue" replace />} />
            {staffPages.map(([path, title, description]) => {
              const policy = staffNavigation.find(
                (item) => item.to === `/staff/${path}`,
              )!;
              return (
                <Route
                  key={path}
                  path={path}
                  element={
                    <RequireRole allow={policy.roles}>
                      <WorkspacePage
                        eyebrow="Staff operations"
                        title={title}
                        description={description}
                      />
                    </RequireRole>
                  }
                />
              );
            })}
          </Route>
        </Route>
      </Route>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
