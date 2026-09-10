import { Navigate, Route, Routes } from "react-router-dom";
import { RequireAuth } from "../auth/RequireAuth";
import { RequireRole } from "../auth/RequireRole";
import { AppShell } from "../components/layout/AppShell";
import { ForbiddenPage } from "../features/foundation/pages/ForbiddenPage";
import { LoginPage } from "../features/foundation/pages/LoginPage";
import { NotFoundPage } from "../features/foundation/pages/NotFoundPage";
import { AuditOperationsPage } from "../features/operations/pages/AuditOperationsPage";
import { BillingOperationsPage } from "../features/operations/pages/BillingOperationsPage";
import { ComplianceOperationsPage } from "../features/operations/pages/ComplianceOperationsPage";
import { InsuranceOperationsPage } from "../features/operations/pages/InsuranceOperationsPage";
import { InventoryOperationsPage } from "../features/operations/pages/InventoryOperationsPage";
import { NotificationsOperationsPage } from "../features/operations/pages/NotificationsOperationsPage";
import { PharmacyOperationsPage } from "../features/operations/pages/PharmacyOperationsPage";
import { ReportsOperationsPage } from "../features/operations/pages/ReportsOperationsPage";
import { AccessPage } from "../features/patientPortal/pages/AccessPage";
import { AppointmentsPage } from "../features/patientPortal/pages/AppointmentsPage";
import { BillingPage } from "../features/patientPortal/pages/BillingPage";
import { OverviewPage } from "../features/patientPortal/pages/OverviewPage";
import { RecordsPage } from "../features/patientPortal/pages/RecordsPage";
import { StaffPatientLayout } from "../features/staff/StaffPatientLayout";
import { ClinicalChartLandingPage } from "../features/staff/pages/ClinicalChartLandingPage";
import { PatientRecordPage } from "../features/staff/pages/PatientRecordPage";
import { PatientsPage } from "../features/staff/pages/PatientsPage";
import { SchedulePage } from "../features/staff/pages/SchedulePage";
import { WorkQueuePage } from "../features/staff/pages/WorkQueuePage";
import { patientRoles, staffNavigation, staffRoles } from "./routePolicy";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route element={<RequireRole allow={patientRoles} />}>
          <Route path="/patient" element={<AppShell surface="patient" />}>
            <Route index element={<Navigate to="overview" replace />} />
            <Route path="overview" element={<OverviewPage />} />
            <Route path="appointments" element={<AppointmentsPage />} />
            <Route path="records" element={<RecordsPage />} />
            <Route path="billing" element={<BillingPage />} />
            <Route path="access" element={<AccessPage />} />
          </Route>
        </Route>
        <Route element={<RequireRole allow={staffRoles} />}>
          <Route path="/staff" element={<AppShell surface="staff" />}>
            <Route index element={<Navigate to="work-queue" replace />} />
            <Route
              path="work-queue"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/work-queue",
                    )!.roles
                  }
                >
                  <WorkQueuePage />
                </RequireRole>
              }
            />
            <Route
              path="patients"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/patients",
                    )!.roles
                  }
                >
                  <PatientsPage />
                </RequireRole>
              }
            />
            <Route
              path="schedule"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/schedule",
                    )!.roles
                  }
                >
                  <SchedulePage />
                </RequireRole>
              }
            />
            <Route
              path="clinical-chart"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/clinical-chart",
                    )!.roles
                  }
                >
                  <ClinicalChartLandingPage />
                </RequireRole>
              }
            />
            <Route
              element={
                <RequireRole
                  allow={["ADMIN", "CLINICIAN", "REGISTRATION_STAFF"]}
                />
              }
            >
              <Route
                path="patients/:patientId"
                element={<StaffPatientLayout />}
              >
                <Route index element={<Navigate to="summary" replace />} />
                <Route path="summary" element={<PatientRecordPage />} />
                <Route path="demographics" element={<PatientRecordPage />} />
                <Route
                  path="encounters"
                  element={
                    <RequireRole allow={["ADMIN", "CLINICIAN"]}>
                      <PatientRecordPage />
                    </RequireRole>
                  }
                />
                <Route
                  path="medications"
                  element={
                    <RequireRole allow={["ADMIN", "CLINICIAN"]}>
                      <PatientRecordPage />
                    </RequireRole>
                  }
                />
                <Route
                  path="labs"
                  element={
                    <RequireRole allow={["ADMIN", "CLINICIAN"]}>
                      <PatientRecordPage />
                    </RequireRole>
                  }
                />
                <Route
                  path="notes"
                  element={
                    <RequireRole allow={["ADMIN", "CLINICIAN"]}>
                      <PatientRecordPage />
                    </RequireRole>
                  }
                />
                <Route
                  path="vaccinations"
                  element={
                    <RequireRole allow={["ADMIN", "CLINICIAN"]}>
                      <PatientRecordPage />
                    </RequireRole>
                  }
                />
              </Route>
            </Route>
            <Route
              path="billing"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/billing",
                    )!.roles
                  }
                >
                  <BillingOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="insurance"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/insurance",
                    )!.roles
                  }
                >
                  <InsuranceOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="pharmacy"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/pharmacy",
                    )!.roles
                  }
                >
                  <PharmacyOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="inventory"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/inventory",
                    )!.roles
                  }
                >
                  <InventoryOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="notifications"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/notifications",
                    )!.roles
                  }
                >
                  <NotificationsOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="compliance"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/compliance",
                    )!.roles
                  }
                >
                  <ComplianceOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="audit"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find((item) => item.to === "/staff/audit")!
                      .roles
                  }
                >
                  <AuditOperationsPage />
                </RequireRole>
              }
            />
            <Route
              path="reports"
              element={
                <RequireRole
                  allow={
                    staffNavigation.find(
                      (item) => item.to === "/staff/reports",
                    )!.roles
                  }
                >
                  <ReportsOperationsPage />
                </RequireRole>
              }
            />
          </Route>
        </Route>
      </Route>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
