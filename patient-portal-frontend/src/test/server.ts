import { setupServer } from "msw/node";
import { authHandlers } from "./handlers/authHandlers";
import { portalHandlers } from "./handlers/portalHandlers";
import { staffHandlers } from "./handlers/staffHandlers";
import { operationsHandlers } from "./handlers/operationsHandlers";

export const server = setupServer(
  ...authHandlers,
  ...portalHandlers,
  ...staffHandlers,
  ...operationsHandlers,
);
