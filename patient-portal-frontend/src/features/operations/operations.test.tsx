import { rest } from "msw";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { axe } from "jest-axe";
import { API_BASE_URL } from "../../api/client";
import { AppRoutes } from "../../app/router";
import { writeSession } from "../../auth/session";
import { createSyntheticSession } from "../../test/factories/sessionFactory";
import { renderWithProviders } from "../../test/render";
import { server } from "../../test/server";

function renderOperation(
  route: string,
  role:
    | "BILLING_STAFF"
    | "PHARMACIST"
    | "PRIVACY_OFFICER"
    | "AUDITOR"
    | "ANALYST"
    | "CLINICIAN" = "BILLING_STAFF",
) {
  writeSession(createSyntheticSession(role));
  return renderWithProviders(<AppRoutes />, { route });
}
describe("phase 4 specialized operations", () => {
  test("loads billing accounts and retains the patient context for invoices", async () => {
    renderOperation("/staff/billing");
    userEvent.click(
      await screen.findByRole("button", {
        name: "20000000-0000-4000-8000-000000000001",
      }),
    );
    expect(await screen.findByText("INV-SYN-1")).toBeInTheDocument();
    expect(screen.getByText("$125.50")).toBeInTheDocument();
  });
  test("retains a billing account idempotency key for an unchanged retry", async () => {
    const keys: string[] = [];
    server.use(
      rest.post(`${API_BASE_URL}/api/billing/accounts`, (req, res, ctx) => {
        keys.push(req.headers.get("idempotency-key") ?? "");
        return keys.length === 1
          ? res(ctx.status(503))
          : res(ctx.status(201), ctx.json({ id: "b2" }));
      }),
    );
    renderOperation("/staff/billing");
    const section = (await screen.findByText("Billing accounts")).closest(
      "section",
    )!;
    userEvent.type(
      within(section).getByRole("textbox", { name: /Patient UUID/ }),
      "20000000-0000-4000-8000-000000000001",
    );
    userEvent.click(
      within(section).getByRole("button", { name: "Create account" }),
    );
    expect(await screen.findByText("Update not completed")).toBeInTheDocument();
    userEvent.click(
      within(section).getByRole("button", { name: "Create account" }),
    );
    await waitFor(() => expect(keys).toHaveLength(2));
    expect(keys[0]).toBeTruthy();
    expect(keys[1]).toBe(keys[0]);
  });
  test("adjudicates insurance only after explicit confirmation", async () => {
    let approvals = 0;
    server.use(
      rest.post(
        `${API_BASE_URL}/api/insurance/claims/:id/adjudicate`,
        (_req, res, ctx) => {
          approvals++;
          return res(ctx.json({}));
        },
      ),
    );
    renderOperation("/staff/insurance");
    userEvent.click(await screen.findByRole("button", { name: "Approve" }));
    expect(approvals).toBe(0);
    userEvent.click(
      within(
        screen.getByRole("dialog", { name: "Approve insurance claim" }),
      ).getByRole("button", { name: "Approve claim" }),
    );
    await waitFor(() => expect(approvals).toBe(1));
  });
  test("shows inventory reorder exposure from authoritative balances", async () => {
    renderOperation("/staff/inventory", "PHARMACIST");
    expect(await screen.findByText("Synthetic gloves")).toBeInTheDocument();
    expect(screen.getByText("REORDER")).toBeInTheDocument();
    expect(screen.getByText("Synthetic medicine")).toBeInTheDocument();
  });
  test("requires confirmation and preserves a pharmacy dispensing conflict", async () => {
    let calls = 0;
    server.use(
      rest.post(
        `${API_BASE_URL}/api/inventory-pharmacy/prescriptions/:id/dispense`,
        (_req, res, ctx) => {
          calls++;
          return res(
            ctx.status(409),
            ctx.json({ detail: "Insufficient approved stock" }),
          );
        },
      ),
    );
    renderOperation("/staff/pharmacy", "PHARMACIST");
    const section = (
      await screen.findByText("Safety review and dispensing")
    ).closest("section")!;
    userEvent.type(
      within(section).getByRole("textbox", {
        name: /Pharmacy prescription UUID/,
      }),
      "rx1",
    );
    userEvent.click(within(section).getByRole("button", { name: "Dispense" }));
    expect(calls).toBe(0);
    userEvent.click(
      within(
        screen.getByRole("dialog", { name: "Dispense medication" }),
      ).getByRole("button", { name: "Confirm dispense" }),
    );
    await waitFor(() => expect(calls).toBe(1));
    expect(
      await screen.findByText("Current state needs review"),
    ).toBeInTheDocument();
  });
  test("confirms compliance state transitions and refetches authoritative cases", async () => {
    let transitions = 0;
    server.use(
      rest.post(
        `${API_BASE_URL}/api/compliance/cases/:id/transitions`,
        (_req, res, ctx) => {
          transitions++;
          return res(ctx.status(200));
        },
      ),
    );
    renderOperation("/staff/compliance", "PRIVACY_OFFICER");
    await screen.findByText("Privacy team");
    const section = screen.getByText("Case management").closest("section")!;
    userEvent.type(
      within(section).getByRole("textbox", {
        name: /Evidence reference for next action/,
      }),
      "REVIEW-2",
    );
    userEvent.click(
      within(section).getByRole("button", { name: "Move to INVESTIGATING" }),
    );
    userEvent.click(
      within(
        screen.getByRole("dialog", { name: "Transition compliance case" }),
      ).getByRole("button", { name: "Confirm transition" }),
    );
    await waitFor(() => expect(transitions).toBe(1));
  });
  test("keeps audit evidence read-only", async () => {
    renderOperation("/staff/audit", "AUDITOR");
    expect(await screen.findByText("PATIENT_READ")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: /delete|edit|export/i }),
    ).not.toBeInTheDocument();
  });
  test("suppresses a small cohort and passes basic accessibility", async () => {
    const { container } = renderOperation("/staff/reports", "ANALYST");
    expect(await screen.findByText("Suppressed (<5)")).toBeInTheDocument();
    expect(await axe(container)).toHaveNoViolations();
  });
  test("queues a notification without claiming delivery", async () => {
    renderOperation("/staff/notifications", "CLINICIAN");
    const section = (
      await screen.findByText("Queue operational message")
    ).closest("section")!;
    userEvent.type(
      within(section).getByRole("textbox", { name: /Recipient UUID/ }),
      "20000000-0000-4000-8000-000000000001",
    );
    userEvent.type(
      within(section).getByRole("textbox", { name: /Destination/ }),
      "synthetic@example.test",
    );
    userEvent.type(
      within(section).getByRole("textbox", { name: /Message/ }),
      "Synthetic operational message",
    );
    userEvent.click(
      within(section).getByRole("button", { name: "Queue message" }),
    );
    expect(
      await screen.findByText(/accepted into the delivery workflow/i),
    ).toBeInTheDocument();
  });
});
