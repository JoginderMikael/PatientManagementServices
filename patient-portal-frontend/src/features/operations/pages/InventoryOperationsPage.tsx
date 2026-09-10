import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { Field, TextField } from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import {
  listMedications,
  listSupplies,
  receiveBatch,
  upsertSupply,
} from "../api";
import {
  formatMoney,
  MutationStatus,
  QueryState,
  StaffSection,
} from "../components";
export function InventoryOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [supply, setSupply] = useState({
    name: "",
    quantityOnHand: "",
    reorderThreshold: "",
    unitCost: "",
  });
  const [batch, setBatch] = useState({
    medicationId: "",
    lot: "",
    expiresOn: "",
    quantity: "",
    reference: "",
  });
  const supplies = useQuery({
    queryKey: ["operations", "inventory", "supplies"],
    queryFn: ({ signal }) => listSupplies(session!.token, signal),
  });
  const meds = useQuery({
    queryKey: ["operations", "inventory", "medications"],
    queryFn: ({ signal }) => listMedications(session!.token, signal),
  });
  const refresh = () =>
    client.invalidateQueries({ queryKey: ["operations", "inventory"] });
  const saveSupply = useMutation({
    mutationFn: () =>
      upsertSupply(
        {
          name: supply.name,
          quantityOnHand: Number(supply.quantityOnHand),
          reorderThreshold: Number(supply.reorderThreshold),
          unitCost: Number(supply.unitCost),
        },
        session!.token,
      ),
    onSuccess: () => {
      setSupply({
        name: "",
        quantityOnHand: "",
        reorderThreshold: "",
        unitCost: "",
      });
      refresh();
    },
  });
  const receive = useMutation({
    mutationFn: () =>
      receiveBatch(
        {
          medicationId: batch.medicationId,
          lot: batch.lot,
          expiresOn: batch.expiresOn,
          quantity: Number(batch.quantity),
          reference: batch.reference,
        },
        session!.token,
      ),
    onSuccess: () => {
      setBatch({
        medicationId: "",
        lot: "",
        expiresOn: "",
        quantity: "",
        reference: "",
      });
      refresh();
    },
  });
  const submit = (e: FormEvent, fn: () => void) => {
    e.preventDefault();
    fn();
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Inventory"
        description="Monitor reorder exposure and receive medication batches with an idempotent external reference."
      />
      <StaffSection title="Medical supplies">
        <form
          className="form-grid"
          onSubmit={(e) => submit(e, () => saveSupply.mutate())}
        >
          <Field id="supply-name" label="Supply name" required>
            <TextField
              required
              value={supply.name}
              onChange={(e) => setSupply({ ...supply, name: e.target.value })}
            />
          </Field>
          <Field id="supply-quantity" label="Quantity on hand" required>
            <TextField
              required
              type="number"
              min="0"
              value={supply.quantityOnHand}
              onChange={(e) =>
                setSupply({ ...supply, quantityOnHand: e.target.value })
              }
            />
          </Field>
          <Field id="supply-threshold" label="Reorder threshold" required>
            <TextField
              required
              type="number"
              min="0"
              value={supply.reorderThreshold}
              onChange={(e) =>
                setSupply({ ...supply, reorderThreshold: e.target.value })
              }
            />
          </Field>
          <Field id="supply-cost" label="Unit cost" required>
            <TextField
              required
              type="number"
              min="0"
              step="0.01"
              value={supply.unitCost}
              onChange={(e) =>
                setSupply({ ...supply, unitCost: e.target.value })
              }
            />
          </Field>
          <div>
            <Button type="submit" disabled={saveSupply.isPending}>
              Save supply
            </Button>
          </div>
        </form>
        <MutationStatus
          error={saveSupply.error}
          success={
            saveSupply.isSuccess ? "Supply balance confirmed." : undefined
          }
        />
        <QueryState
          loading={supplies.isLoading}
          error={supplies.error}
          empty={!supplies.data?.length}
          onRetry={() => supplies.refetch()}
        >
          <Table
            caption="Supply inventory"
            headers={["Item", "On hand", "Threshold", "Unit cost", "State"]}
          >
            {supplies.data?.map((s) => (
              <tr
                key={s.id}
                className={
                  s.quantityOnHand <= s.reorderThreshold ? "row-overdue" : ""
                }
              >
                <td>{s.name}</td>
                <td>{s.quantityOnHand}</td>
                <td>{s.reorderThreshold}</td>
                <td>{formatMoney(s.unitCost)}</td>
                <td>
                  <StatusBadge
                    tone={
                      s.quantityOnHand <= s.reorderThreshold
                        ? "warning"
                        : "success"
                    }
                  >
                    {s.quantityOnHand <= s.reorderThreshold
                      ? "REORDER"
                      : "AVAILABLE"}
                  </StatusBadge>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection title="Medication batches">
        <form
          className="form-grid"
          onSubmit={(e) => submit(e, () => receive.mutate())}
        >
          <Field id="batch-medication" label="Medication UUID" required>
            <TextField
              required
              value={batch.medicationId}
              onChange={(e) =>
                setBatch({ ...batch, medicationId: e.target.value })
              }
            />
          </Field>
          <Field id="batch-lot" label="Lot" required>
            <TextField
              required
              value={batch.lot}
              onChange={(e) => setBatch({ ...batch, lot: e.target.value })}
            />
          </Field>
          <Field id="batch-expiry" label="Expiry" required>
            <TextField
              required
              type="date"
              value={batch.expiresOn}
              onChange={(e) =>
                setBatch({ ...batch, expiresOn: e.target.value })
              }
            />
          </Field>
          <Field id="batch-quantity" label="Quantity" required>
            <TextField
              required
              type="number"
              min="1"
              value={batch.quantity}
              onChange={(e) => setBatch({ ...batch, quantity: e.target.value })}
            />
          </Field>
          <Field id="batch-reference" label="Receipt reference" required>
            <TextField
              required
              value={batch.reference}
              onChange={(e) =>
                setBatch({ ...batch, reference: e.target.value })
              }
            />
          </Field>
          <div>
            <Button type="submit" disabled={receive.isPending}>
              Receive batch
            </Button>
          </div>
        </form>
        <MutationStatus
          error={receive.error}
          success={receive.isSuccess ? "Batch receipt confirmed." : undefined}
          conflict="That receipt reference was already used with different batch details."
        />
        <QueryState
          loading={meds.isLoading}
          error={meds.error}
          empty={!meds.data?.length}
          onRetry={() => meds.refetch()}
        >
          <Table
            caption="Medication inventory"
            headers={["Medication", "NDC", "On hand", "Unit cost"]}
          >
            {meds.data?.map((m) => (
              <tr key={m.id}>
                <td>{m.name}</td>
                <td>{m.ndcCode}</td>
                <td>{m.quantityOnHand}</td>
                <td>{formatMoney(m.unitCost)}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
