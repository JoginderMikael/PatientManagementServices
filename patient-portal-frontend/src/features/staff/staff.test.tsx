import { rest } from "msw";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { axe } from "jest-axe";
import { API_BASE_URL } from "../../api/client";
import { AppRoutes } from "../../app/router";
import { writeSession } from "../../auth/session";
import { createSyntheticSession } from "../../test/factories/sessionFactory";
import { syntheticPatient } from "../../test/handlers/staffHandlers";
import { renderWithProviders } from "../../test/render";
import { server } from "../../test/server";

function renderStaff(
  route: string,
  role: "CLINICIAN" | "REGISTRATION_STAFF" = "CLINICIAN",
) {
  writeSession(createSyntheticSession(role));
  return renderWithProviders(<AppRoutes />, { route });
}
describe("phase 3 staff workflows", () => {
  test("searches patients and establishes a persistent patient context", async () => {
    renderStaff("/staff/patients?q=Amara");
    expect(await screen.findByText("MRN-1047-62")).toBeInTheDocument();
    userEvent.click(screen.getByRole("link", { name: "Open record" }));
    expect(
      await screen.findByRole("region", { name: "Current patient context" }),
    ).toHaveTextContent("Amara Njeri");
    expect(
      await screen.findByText("Synthetic allergy alert"),
    ).toBeInTheDocument();
  });
  test("claims a queue task and refreshes server state", async () => {
    let claims = 0;
    server.use(
      rest.post(
        `${API_BASE_URL}/api/staff-dashboard/tasks/:id/claim`,
        (_req, res, ctx) => {
          claims++;
          return res(ctx.status(200));
        },
      ),
    );
    renderStaff("/staff/work-queue");
    userEvent.click(await screen.findByRole("button", { name: "Review" }));
    userEvent.click(screen.getByRole("button", { name: "Claim" }));
    await waitFor(() => expect(claims).toBe(1));
    expect(
      await screen.findByText("The server confirmed the task update."),
    ).toBeInTheDocument();
  });
  test("keeps schedule conflicts distinct and refreshes the day list", async () => {
    server.use(
      rest.post(`${API_BASE_URL}/api/appointments`, (_req, res, ctx) =>
        res(ctx.status(409)),
      ),
    );
    renderStaff("/staff/schedule?day=2026-09-12");
    await screen.findByText("Synthetic follow-up");
    userEvent.selectOptions(
      screen.getByLabelText(/^Patient/),
      syntheticPatient.id,
    );
    userEvent.selectOptions(
      screen.getByLabelText(/^Clinician schedule/),
      "10000000-0000-4000-8000-000000000001",
    );
    userEvent.type(screen.getByLabelText(/Starts at/), "2026-09-12T09:30");
    userEvent.clear(screen.getByLabelText(/^Reason/));
    userEvent.type(screen.getByLabelText(/^Reason/), "Synthetic conflict");
    userEvent.click(screen.getByRole("button", { name: "Book appointment" }));
    expect(
      await screen.findByText("Current state needs review"),
    ).toBeInTheDocument();
    expect(
      screen.getByText(/slot is no longer available/i),
    ).toBeInTheDocument();
  });
  test("renders signed notes as locked without a signing action and passes basic accessibility", async () => {
    const { container } = renderStaff(
      `/staff/patients/${syntheticPatient.id}/notes`,
    );
    expect(
      await screen.findByText("Synthetic signed note"),
    ).toBeInTheDocument();
    expect(screen.getByText(/SIGNED · locked/)).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Sign note" }),
    ).not.toBeInTheDocument();
    expect(await axe(container)).toHaveNoViolations();
  });
  test("requires confirmation before acknowledging a clinical alert", async () => {
    let calls = 0;
    server.use(
      rest.post(
        `${API_BASE_URL}/api/ehr/safety/alerts/:id/acknowledge`,
        (_req, res, ctx) => {
          calls++;
          return res(ctx.status(200));
        },
      ),
    );
    renderStaff(`/staff/patients/${syntheticPatient.id}/summary`);
    userEvent.click(await screen.findByRole("button", { name: "Acknowledge" }));
    const dialog = screen.getByRole("dialog", {
      name: "Acknowledge clinical alert",
    });
    expect(calls).toBe(0);
    userEvent.click(
      within(dialog).getByRole("button", { name: "Acknowledge alert" }),
    );
    await waitFor(() => expect(calls).toBe(1));
  });
});
