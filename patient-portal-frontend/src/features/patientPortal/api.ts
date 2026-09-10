import { apiRequest } from "../../api/client";
import {
  AppointmentCommand,
  PaymentCommand,
  PortalAppointmentRequest,
  PortalOverview,
  PortalPayment,
  RecordAccessRequest,
  RecordCommand,
} from "./types";

export function getPortalOverview(
  token: string,
): Promise<PortalOverview> {
  return apiRequest(
    "/api/portal/me",
    { token },
  );
}

export function createAppointmentRequest(
  command: AppointmentCommand,
  token: string,
): Promise<PortalAppointmentRequest> {
  return apiRequest("/api/portal/appointment-requests", {
    method: "POST",
    body: command,
    token,
  });
}

export function cancelAppointmentRequest(
  id: string,
  token: string,
): Promise<void> {
  return apiRequest(
    `/api/portal/appointment-requests/${encodeURIComponent(id)}/cancel`,
    { method: "POST", responseType: "empty", token },
  );
}

export function createRecordRequest(
  command: RecordCommand,
  token: string,
): Promise<RecordAccessRequest> {
  return apiRequest("/api/portal/record-requests", {
    method: "POST",
    body: command,
    token,
  });
}

export function downloadRecordRequest(
  id: string,
  token: string,
): Promise<string> {
  return apiRequest(
    `/api/portal/record-requests/${encodeURIComponent(id)}/download`,
    { accept: "text/plain", responseType: "text", token },
  );
}

export function createPortalPayment(
  command: PaymentCommand,
  token: string,
  idempotencyKey: string,
): Promise<PortalPayment> {
  return apiRequest("/api/portal/payments", {
    method: "POST",
    body: command,
    idempotencyKey,
    token,
  });
}
