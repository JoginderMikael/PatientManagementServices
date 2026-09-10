import { FormEvent, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import {
  Field,
  Select,
  TextArea,
  TextField,
} from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import {
  bookAppointment,
  listAppointments,
  listPatients,
  listSchedules,
} from "../api";
import { MutationStatus, QueryState, StaffSection } from "../components";

export function SchedulePage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [params, setParams] = useSearchParams();
  const day = params.get("day") ?? new Date().toISOString().slice(0, 10);
  const view = params.get("view") ?? "day";
  const [patientId, setPatientId] = useState("");
  const [doctorId, setDoctorId] = useState("");
  const [startsAt, setStartsAt] = useState("");
  const [duration, setDuration] = useState(30);
  const [reason, setReason] = useState("");
  const appointments = useQuery({
    queryKey: ["appointments", patientId],
    queryFn: ({ signal }) =>
      listAppointments(session!.token, patientId || undefined, signal),
  });
  const schedules = useQuery({
    queryKey: ["doctor-schedules"],
    queryFn: ({ signal }) => listSchedules(session!.token, signal),
  });
  const patients = useQuery({
    queryKey: ["staff-patients"],
    queryFn: ({ signal }) => listPatients(session!.token, signal),
  });
  const mutation = useMutation({
    mutationFn: () =>
      bookAppointment(
        { patientId, doctorId, startsAt, reason, durationMinutes: duration },
        session!.token,
      ),
    onSuccess: () => {
      setReason("");
      client.invalidateQueries({ queryKey: ["appointments"] });
    },
    onError: () => client.invalidateQueries({ queryKey: ["appointments"] }),
  });
  const rows = useMemo(
    () =>
      appointments.data
        ?.filter((a) => view === "list" || a.startsAt.slice(0, 10) === day)
        .sort((a, b) => a.startsAt.localeCompare(b.startsAt)) ?? [],
    [appointments.data, day, view],
  );
  function submit(e: FormEvent) {
    e.preventDefault();
    mutation.mutate();
  }
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Staff operations"
        title="Schedule"
        description="Review a local day list and book against server-confirmed clinician availability."
      />
      <StaffSection
        title="Book appointment"
        description="Times are entered in the facility's configured local timezone. The server is authoritative for slot conflicts."
      >
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field id="booking-patient" label="Patient" required>
              <Select
                required
                value={patientId}
                onChange={(e) => setPatientId(e.target.value)}
              >
                <option value="">Select patient</option>
                {patients.data?.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} · {p.mrn}
                  </option>
                ))}
              </Select>
            </Field>
            <Field id="booking-doctor" label="Clinician schedule" required>
              <Select
                required
                value={doctorId}
                onChange={(e) => setDoctorId(e.target.value)}
              >
                <option value="">Select schedule</option>
                {schedules.data
                  ?.filter((s) => s.workDate >= day)
                  .map((s) => (
                    <option key={s.id} value={s.doctorId}>
                      {s.workDate} · {s.startsAt}–{s.endsAt} · {s.location}
                    </option>
                  ))}
              </Select>
            </Field>
            <Field
              id="booking-start"
              label="Starts at (facility time)"
              required
            >
              <TextField
                required
                type="datetime-local"
                value={startsAt}
                onChange={(e) => setStartsAt(e.target.value)}
              />
            </Field>
            <Field id="booking-duration" label="Duration (minutes)" required>
              <TextField
                required
                type="number"
                min="5"
                max="480"
                value={duration}
                onChange={(e) => setDuration(Number(e.target.value))}
              />
            </Field>
          </div>
          <Field id="booking-reason" label="Reason" required>
            <TextArea
              required
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
          </Field>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? "Booking…" : "Book appointment"}
          </Button>
        </form>
        <MutationStatus
          error={mutation.error}
          success={
            mutation.isSuccess
              ? "Appointment booked and the day list refreshed."
              : undefined
          }
          conflict="That slot is no longer available. The current schedule has been refreshed; choose another time."
        />
      </StaffSection>
      <StaffSection title={view === "day" ? "Day view" : "Appointment list"}>
        <div className="toolbar">
          <Field id="schedule-view" label="View">
            <Select
              value={view}
              onChange={(e) =>
                setParams({
                  view: e.target.value,
                  day,
                })
              }
            >
              <option value="day">Day</option>
              <option value="list">All returned appointments</option>
            </Select>
          </Field>
          {view === "day" && (
            <Field id="schedule-day" label="Date">
              <TextField
                type="date"
                value={day}
                onChange={(e) =>
                  setParams({
                    view,
                    day: e.target.value,
                  })
                }
              />
            </Field>
          )}
        </div>
        <QueryState
          loading={appointments.isLoading}
          error={appointments.error}
          empty={!rows.length}
          onRetry={() => appointments.refetch()}
        >
          <Table
            caption={
              view === "day" ? `Appointments for ${day}` : "Appointment list"
            }
            headers={[
              "Date and time",
              "Patient",
              "Clinician",
              "Reason",
              "Status",
            ]}
          >
            {rows.map((a) => (
              <tr key={a.id}>
                <td>
                  {a.startsAt.slice(0, 10)} · {a.startsAt.slice(11, 16)}–
                  {a.endsAt.slice(11, 16)}
                </td>
                <td>
                  {patients.data?.find((p) => p.id === a.patientId)?.name ??
                    a.patientId}
                </td>
                <td>{a.doctorId}</td>
                <td>{a.reason}</td>
                <td>
                  <StatusBadge>{a.status}</StatusBadge>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
