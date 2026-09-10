import { FormEvent, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createIdempotencyKey } from "../../../api/client";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { Field, TextField } from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { ConfirmAction } from "../../../components/safety/ConfirmAction";
import {
  createBillingAccount,
  createInvoice,
  listBillingAccounts,
  listPatientInvoices,
  postPayment,
} from "../api";
import {
  formatMoney,
  MutationStatus,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";

export function BillingOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const key = useRef("");
  const [patientId, setPatientId] = useState("");
  const [invoicePatient, setInvoicePatient] = useState("");
  const [invoice, setInvoice] = useState({
    reference: "",
    amount: "",
    currency: "USD",
  });
  const [posting, setPosting] = useState({
    invoiceId: "",
    reference: "",
    amount: "",
  });
  const accounts = useQuery({
    queryKey: ["operations", "billing", "accounts"],
    queryFn: ({ signal }) => listBillingAccounts(session!.token, signal),
  });
  const invoices = useQuery({
    queryKey: ["operations", "billing", "invoices", invoicePatient],
    queryFn: ({ signal }) =>
      listPatientInvoices(invoicePatient, session!.token, signal),
    enabled: Boolean(invoicePatient),
  });
  const account = useMutation({
    mutationFn: () => {
      if (!key.current) key.current = createIdempotencyKey();
      return createBillingAccount(patientId, key.current, session!.token);
    },
    onSuccess: () => {
      key.current = "";
      setPatientId("");
      client.invalidateQueries({ queryKey: ["operations", "billing"] });
    },
  });
  const create = useMutation({
    mutationFn: () =>
      createInvoice(
        {
          patientId: invoicePatient,
          reference: invoice.reference,
          amount: Number(invoice.amount),
          currency: invoice.currency.toUpperCase(),
        },
        session!.token,
      ),
    onSuccess: () => {
      setInvoice({ reference: "", amount: "", currency: "USD" });
      client.invalidateQueries({
        queryKey: ["operations", "billing", "invoices", invoicePatient],
      });
    },
  });
  const post = useMutation({
    mutationFn: () =>
      postPayment(
        posting.invoiceId,
        {
          reference: posting.reference,
          amount: Number(posting.amount),
          kind: "PAYMENT",
        },
        session!.token,
      ),
    onSuccess: () => {
      setPosting({ invoiceId: "", reference: "", amount: "" });
      client.invalidateQueries({ queryKey: ["operations", "billing"] });
    },
    onError: () =>
      client.invalidateQueries({ queryKey: ["operations", "billing"] }),
  });
  const submit = (e: FormEvent, action: () => void) => {
    e.preventDefault();
    action();
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Billing"
        description="Review accounts, create referenced invoices and post payments against authoritative balances."
      />
      <StaffSection title="Billing accounts">
        <form
          className="toolbar"
          onSubmit={(e) => submit(e, () => account.mutate())}
        >
          <Field id="billing-patient" label="Patient UUID" required>
            <TextField
              required
              value={patientId}
              onChange={(e) => setPatientId(e.target.value)}
            />
          </Field>
          <Button type="submit" disabled={account.isPending}>
            Create account
          </Button>
        </form>
        <MutationStatus
          error={account.error}
          success={account.isSuccess ? "Billing account confirmed." : undefined}
        />
        <QueryState
          loading={accounts.isLoading}
          error={accounts.error}
          empty={!accounts.data?.length}
          onRetry={() => accounts.refetch()}
        >
          <Table
            caption="Billing accounts"
            headers={["Patient", "Status", "Balance", "Updated"]}
          >
            {accounts.data?.map((a) => (
              <tr key={a.id}>
                <td>
                  <button
                    className="link-button"
                    onClick={() => setInvoicePatient(a.patientId)}
                  >
                    {a.patientId}
                  </button>
                </td>
                <td>
                  <StatusBadge tone={statusTone(a.status)}>
                    {a.status}
                  </StatusBadge>
                </td>
                <td>{formatMoney(a.balance, a.currency)}</td>
                <td>{a.updatedAt?.slice(0, 10) ?? "—"}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection
        title="Patient invoices"
        description="Select an account above or enter a patient UUID. Invoice references are the retry/idempotency boundary."
      >
        <Field id="invoice-patient" label="Patient UUID">
          <TextField
            value={invoicePatient}
            onChange={(e) => setInvoicePatient(e.target.value)}
          />
        </Field>
        {invoicePatient && (
          <form
            className="form-grid"
            onSubmit={(e) => submit(e, () => create.mutate())}
          >
            <Field id="invoice-reference" label="Invoice reference" required>
              <TextField
                required
                value={invoice.reference}
                onChange={(e) =>
                  setInvoice({ ...invoice, reference: e.target.value })
                }
              />
            </Field>
            <Field id="invoice-amount" label="Amount" required>
              <TextField
                required
                type="number"
                min="0.01"
                step="0.01"
                value={invoice.amount}
                onChange={(e) =>
                  setInvoice({ ...invoice, amount: e.target.value })
                }
              />
            </Field>
            <Field id="invoice-currency" label="Currency" required>
              <TextField
                required
                pattern="[A-Z]{3}"
                maxLength={3}
                value={invoice.currency}
                onChange={(e) =>
                  setInvoice({
                    ...invoice,
                    currency: e.target.value.toUpperCase(),
                  })
                }
              />
            </Field>
            <div>
              <Button type="submit" disabled={create.isPending}>
                Create invoice
              </Button>
            </div>
          </form>
        )}
        <MutationStatus
          error={create.error}
          success={
            create.isSuccess ? "Invoice confirmed by billing." : undefined
          }
        />
        <QueryState
          loading={invoices.isLoading}
          error={invoices.error}
          empty={Boolean(invoicePatient && !invoices.data?.length)}
          onRetry={() => invoices.refetch()}
        >
          {invoices.data && (
            <Table
              caption="Patient invoices"
              headers={["Reference", "Amount", "Paid", "Status", "Invoice ID"]}
            >
              {invoices.data.map((i) => (
                <tr key={i.id}>
                  <td>{i.reference}</td>
                  <td>{formatMoney(i.amount, i.currency)}</td>
                  <td>{formatMoney(i.paid, i.currency)}</td>
                  <td>
                    <StatusBadge
                      tone={i.paid >= i.amount ? "success" : "warning"}
                    >
                      {i.paid >= i.amount ? "PAID" : "OPEN"}
                    </StatusBadge>
                  </td>
                  <td>
                    <button
                      className="link-button"
                      onClick={() =>
                        setPosting({ ...posting, invoiceId: i.id })
                      }
                    >
                      Post payment
                    </button>
                  </td>
                </tr>
              ))}
            </Table>
          )}
        </QueryState>
      </StaffSection>
      {posting.invoiceId && (
        <StaffSection
          title="Post payment"
          description={`Invoice ${posting.invoiceId}`}
        >
          <form className="form-grid" onSubmit={(e) => e.preventDefault()}>
            <Field id="posting-reference" label="Payment reference" required>
              <TextField
                required
                value={posting.reference}
                onChange={(e) =>
                  setPosting({ ...posting, reference: e.target.value })
                }
              />
            </Field>
            <Field id="posting-amount" label="Amount" required>
              <TextField
                required
                type="number"
                min="0.01"
                step="0.01"
                value={posting.amount}
                onChange={(e) =>
                  setPosting({ ...posting, amount: e.target.value })
                }
              />
            </Field>
            <div>
              <ConfirmAction
                triggerLabel="Review payment"
                title="Post payment"
                description={`Post ${posting.amount || "the entered amount"} to invoice ${posting.invoiceId} using reference ${posting.reference || "not entered"}. Overpayment and reused mismatched references are rejected.`}
                confirmLabel="Post payment"
                disabled={
                  !posting.reference ||
                  Number(posting.amount) <= 0 ||
                  post.isPending
                }
                onConfirm={() => post.mutate()}
              />
            </div>
          </form>
          <MutationStatus
            error={post.error}
            success={post.isSuccess ? "Payment posting confirmed." : undefined}
            conflict="The payment reference, invoice state or remaining balance conflicts with the server. Review the refreshed invoice."
          />
        </StaffSection>
      )}
    </div>
  );
}
