import { rest } from "msw";
import { screen, waitFor } from "@testing-library/react";
import { axe } from "jest-axe";
import { API_BASE_URL } from "../api/client";
import { AppRoutes } from "../app/router";
import { UserRole } from "../auth/tokenClaims";
import { createSyntheticSession } from "../test/factories/sessionFactory";
import { renderWithProviders } from "../test/render";
import { server } from "../test/server";
import { syntheticOverview } from "../test/handlers/portalHandlers";
import { writeSession } from "../auth/session";

const journeys: Array<[UserRole, string, string, number]> = [
  ["PATIENT", "/patient/overview", "Overview", 320],
  ["CLINICIAN", "/staff/work-queue", "Work queue", 1440],
  ["REGISTRATION_STAFF", "/staff/patients", "Patient registry", 768],
  ["BILLING_STAFF", "/staff/billing", "Billing", 1280],
  ["PHARMACIST", "/staff/pharmacy", "Pharmacy", 390],
  ["PRIVACY_OFFICER", "/staff/compliance", "Privacy and compliance", 1024],
  ["AUDITOR", "/staff/audit", "Audit evidence", 1440],
  ["ANALYST", "/staff/reports", "Population reports", 412],
];

describe("Phase 5 integrated frontend release journeys", () => {
  test.each(journeys)(
    "%s reaches only its intended %s surface",
    async (role, route, heading, width) => {
      Object.defineProperty(window, "innerWidth", {
        configurable: true,
        value: width,
      });
      writeSession(createSyntheticSession(role));
      const { container } = renderWithProviders(<AppRoutes />, { route });
      expect(
        await screen.findByRole("heading", { name: heading }),
      ).toBeInTheDocument();
      await waitFor(() =>
        expect(container.querySelector(".skeleton")).not.toBeInTheDocument(),
      );
      expect(screen.getByRole("main")).toBeInTheDocument();
      expect(await axe(container)).toHaveNoViolations();
    },
  );

  test.each([
    ["PATIENT", "/staff/patients", "/patient/overview"],
    ["CLINICIAN", "/staff/billing", "/staff/work-queue"],
    ["BILLING_STAFF", "/staff/pharmacy", "/staff/work-queue"],
    ["AUDITOR", "/staff/compliance", "/staff/audit"],
  ] as const)(
    "%s is denied cross-role entry to %s",
    async (role, forbiddenRoute, expectedStart) => {
      writeSession(createSyntheticSession(role));
      renderWithProviders(<AppRoutes />, { route: forbiddenRoute });
      expect(
        await screen.findByRole("heading", { name: /do not have access/i }),
      ).toBeInTheDocument();
      expect(screen.getByRole("link", { name: /start page/i })).toHaveAttribute(
        "href",
        expectedStart,
      );
    },
  );

  test("patient portal uses only the token-bound self endpoint", async () => {
    let selfRequests = 0;
    let patientLookupRequests = 0;
    server.use(
      rest.get(
        `${API_BASE_URL}/api/portal/me`,
        (_request, response, context) => {
          selfRequests += 1;
          return response(context.json(syntheticOverview));
        },
      ),
      rest.get(
        `${API_BASE_URL}/api/portal/patients/:id/overview`,
        (_request, response, context) => {
          patientLookupRequests += 1;
          return response(context.status(403));
        },
      ),
    );
    writeSession(createSyntheticSession("PATIENT"));
    renderWithProviders(<AppRoutes />, { route: "/patient/overview" });
    expect(
      await screen.findByRole("heading", { name: "Account summary" }),
    ).toBeInTheDocument();
    expect(selfRequests).toBe(1);
    expect(patientLookupRequests).toBe(0);
  });
});
