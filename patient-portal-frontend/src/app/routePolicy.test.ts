import {
  navigationFor,
  patientNavigation,
  staffNavigation,
} from "./routePolicy";
import { USER_ROLES } from "../auth/tokenClaims";

describe("route presentation policy", () => {
  test.each(USER_ROLES)(
    "%s sees only navigation explicitly assigned to that role",
    (role) => {
      const visible = [
        ...navigationFor(role, "patient"),
        ...navigationFor(role, "staff"),
      ];
      expect(visible.every((item) => item.roles.includes(role))).toBe(true);
    },
  );

  test("patient and specialist navigation stay separated", () => {
    expect(
      navigationFor("PATIENT", "patient").map((item) => item.label),
    ).toEqual(patientNavigation.map((item) => item.label));
    expect(navigationFor("PATIENT", "staff")).toHaveLength(0);
    expect(
      navigationFor("PHARMACIST", "staff").map((item) => item.label),
    ).toEqual(["Work queue", "Pharmacy", "Inventory"]);
    expect(navigationFor("AUDITOR", "staff").map((item) => item.label)).toEqual(
      ["Audit evidence"],
    );
    expect(
      navigationFor("REGISTRATION_STAFF", "staff").map((item) => item.label),
    ).toEqual(["Patients", "Schedule", "Notifications"]);
  });

  test("every navigation route is unique", () => {
    const routes = [...patientNavigation, ...staffNavigation].map(
      (item) => item.to,
    );
    expect(new Set(routes).size).toBe(routes.length);
  });
});
