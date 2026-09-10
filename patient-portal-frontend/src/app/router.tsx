import { Navigate, Route, Routes } from "react-router-dom";
import { RequireAuth } from "../auth/RequireAuth";
import { RequireRole } from "../auth/RequireRole";
import { AppShell } from "../components/layout/AppShell";
import { ForbiddenPage } from "../features/foundation/pages/ForbiddenPage";
import { LoginPage } from "../features/foundation/pages/LoginPage";
import { NotFoundPage } from "../features/foundation/pages/NotFoundPage";
import { WorkspacePage } from "../features/foundation/pages/WorkspacePage";
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
import {
  patientRoles,
  staffNavigation,
  staffRoles,
} from "./routePolicy";

const staffPages = [
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
            <Route path="work-queue" element={<RequireRole allow={staffNavigation.find(item=>item.to==='/staff/work-queue')!.roles}><WorkQueuePage /></RequireRole>} />
            <Route path="patients" element={<RequireRole allow={staffNavigation.find(item=>item.to==='/staff/patients')!.roles}><PatientsPage /></RequireRole>} />
            <Route path="schedule" element={<RequireRole allow={staffNavigation.find(item=>item.to==='/staff/schedule')!.roles}><SchedulePage /></RequireRole>} />
            <Route path="clinical-chart" element={<RequireRole allow={staffNavigation.find(item=>item.to==='/staff/clinical-chart')!.roles}><ClinicalChartLandingPage /></RequireRole>} />
            <Route element={<RequireRole allow={['ADMIN','CLINICIAN','REGISTRATION_STAFF']} />}>
              <Route path="patients/:patientId" element={<StaffPatientLayout />}>
                <Route index element={<Navigate to="summary" replace />} />
                <Route path="summary" element={<PatientRecordPage />} />
                <Route path="demographics" element={<PatientRecordPage />} />
                <Route path="encounters" element={<RequireRole allow={['ADMIN','CLINICIAN']}><PatientRecordPage /></RequireRole>} />
                <Route path="medications" element={<RequireRole allow={['ADMIN','CLINICIAN']}><PatientRecordPage /></RequireRole>} />
                <Route path="labs" element={<RequireRole allow={['ADMIN','CLINICIAN']}><PatientRecordPage /></RequireRole>} />
                <Route path="notes" element={<RequireRole allow={['ADMIN','CLINICIAN']}><PatientRecordPage /></RequireRole>} />
                <Route path="vaccinations" element={<RequireRole allow={['ADMIN','CLINICIAN']}><PatientRecordPage /></RequireRole>} />
              </Route>
            </Route>
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
