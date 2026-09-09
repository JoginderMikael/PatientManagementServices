import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRef } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { createIdempotencyKey } from "../../../api/client";
import { queryKeys } from "../../../api/queryKeys";
import { PageHeader } from "../../../components/PageHeader";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import {
  Field,
  TextField,
  ValidationSummary,
} from "../../../components/forms/FormControls";
import { createPortalPayment } from "../api";
import { MutationError, NoActivity, PortalSection } from "../components";
import { formatPortalDate, formatPortalMoney, PortalStatus } from "../format";
import { PortalBoundary, usePortalOverview } from "../portalState";
import { PortalOverview } from "../types";

const uuid =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const schema = z.object({
  invoiceId: z
    .string()
    .trim()
    .regex(uuid, "Enter the invoice ID exactly as shown on your statement."),
  amount: z
    .string()
    .trim()
    .regex(
      /^\d{1,17}(\.\d{1,2})?$/,
      "Enter a positive amount with no more than two decimal places.",
    )
    .refine((value) => Number(value) > 0, "Enter an amount greater than zero."),
});
type Values = z.infer<typeof schema>;

export function BillingPage() {
  const state = usePortalOverview();
  const queryClient = useQueryClient();
  const attempt = useRef<{ signature: string; key: string } | null>(null);
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { invoiceId: "", amount: "" },
  });
  const key = state.patientId
    ? queryKeys.portalOverview(state.patientId)
    : ["portal-overview"];
  const payment = useMutation({
    mutationFn: (values: Values) => {
      const signature = `${state.patientId}:${values.invoiceId}:${values.amount}`;
      if (!attempt.current || attempt.current.signature !== signature)
        attempt.current = { signature, key: createIdempotencyKey() };
      return createPortalPayment(
        {
          patientId: state.patientId!,
          invoiceId: values.invoiceId,
          amount: Number(values.amount),
        },
        state.token,
        attempt.current.key,
      );
    },
    onSuccess: (created) => {
      queryClient.setQueryData<PortalOverview>(key, (current) =>
        current
          ? { ...current, payments: [created, ...current.payments] }
          : current,
      );
      attempt.current = null;
      form.reset();
    },
  });
  const summary = Object.entries(form.formState.errors).map(
    ([fieldId, error]) => ({
      fieldId,
      message: error?.message ?? "Review this field.",
    }),
  );
  return (
    <>
      <PageHeader
        eyebrow="Patient portal"
        title="Bills and payments"
        description="Review portal payment activity and submit a payment against an invoice statement."
      />
      <PortalBoundary state={state}>
        {state.data && (
          <div className="portal-stack">
            <PortalSection
              title="Make a payment"
              description="The portal service validates that the invoice belongs to this patient before settlement."
            >
              <Alert title="Invoice details required">
                A patient-readable invoice listing is not yet exposed by the
                gateway. Enter the opaque invoice ID from your synthetic
                statement.
              </Alert>
              {payment.isSuccess && (
                <Alert title="Payment submitted" tone="success">
                  Your payment intent was recorded without creating a duplicate
                  submission.
                </Alert>
              )}
              <MutationError
                error={payment.error}
                conflictMessage="This payment reference was already used for different details. Review the current payment activity before resubmitting."
              />
              <form
                onSubmit={form.handleSubmit((values) => payment.mutate(values))}
                noValidate
              >
                <ValidationSummary errors={summary} />
                <div className="form-grid">
                  <Field
                    id="invoiceId"
                    label="Invoice ID"
                    required
                    error={form.formState.errors.invoiceId?.message}
                  >
                    <TextField
                      autoComplete="off"
                      {...form.register("invoiceId")}
                    />
                  </Field>
                  <Field
                    id="amount"
                    label="Amount (KES)"
                    required
                    error={form.formState.errors.amount?.message}
                  >
                    <TextField
                      inputMode="decimal"
                      autoComplete="off"
                      {...form.register("amount")}
                    />
                  </Field>
                </div>
                <button
                  className="button button--primary"
                  disabled={payment.isPending}
                  type="submit"
                >
                  {payment.isPending ? "Submitting…" : "Submit payment"}
                </button>
              </form>
            </PortalSection>
            <PortalSection title="Payment activity">
              {!state.data.payments.length ? (
                <NoActivity
                  title="No payment activity"
                  message="Submitted payment intents will appear here."
                />
              ) : (
                <Table
                  caption="Your portal payment activity"
                  headers={["Invoice ID", "Amount", "Submitted", "Status"]}
                >
                  {state.data.payments.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <code>{item.invoiceId}</code>
                      </td>
                      <td>{formatPortalMoney(item.amount)}</td>
                      <td>{formatPortalDate(item.createdAt)}</td>
                      <td>
                        <PortalStatus value={item.status} />
                      </td>
                    </tr>
                  ))}
                </Table>
              )}
            </PortalSection>
          </div>
        )}
      </PortalBoundary>
    </>
  );
}
