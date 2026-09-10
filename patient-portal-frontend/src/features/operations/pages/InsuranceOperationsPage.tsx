import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { PageHeader } from "../../../components/PageHeader";
import { ConfirmAction } from "../../../components/safety/ConfirmAction";
import {
  adjudicateClaim,
  listClaims,
  listCoverage,
  reconcileClaim,
} from "../api";
import {
  formatDate,
  formatMoney,
  MutationStatus,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";

export function InsuranceOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const claims = useQuery({
    queryKey: ["operations", "insurance", "claims"],
    queryFn: ({ signal }) => listClaims(session!.token, signal),
  });
  const coverage = useQuery({
    queryKey: ["operations", "insurance", "coverage"],
    queryFn: ({ signal }) => listCoverage(session!.token, signal),
  });
  const refresh = () =>
    client.invalidateQueries({ queryKey: ["operations", "insurance"] });
  const action = useMutation({
    mutationFn: (v: {
      id: string;
      action: "approve" | "deny" | "reconcile";
      amount: number;
    }) => {
      const request = v.action === "reconcile"
        ? reconcileClaim(v.id, session!.token)
        : adjudicateClaim(
            v.id,
            v.action === "approve" ? "APPROVED" : "DENIED",
            v.action === "approve" ? v.amount : 0,
            session!.token,
          );
      return request.then(() => undefined);
    },
    onSuccess: refresh,
    onError: refresh,
  });
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Insurance"
        description="Review coverage evidence, adjudicate submitted claims and reconcile only after remittance is recorded."
      />
      <MutationStatus
        error={action.error}
        success={
          action.isSuccess
            ? "The insurer workflow state was confirmed."
            : undefined
        }
      />
      <StaffSection title="Claims">
        <QueryState
          loading={claims.isLoading}
          error={claims.error}
          empty={!claims.data?.length}
          onRetry={() => claims.refetch()}
        >
          <Table
            caption="Insurance claims"
            headers={["Patient", "Invoice", "Amount", "Status", "Actions"]}
          >
            {claims.data?.map((c) => (
              <tr key={c.id}>
                <td>{c.patientId}</td>
                <td>{c.invoiceId}</td>
                <td>{formatMoney(c.amount)}</td>
                <td>
                  <StatusBadge tone={statusTone(c.status)}>
                    {c.status}
                  </StatusBadge>
                </td>
                <td>
                  <div className="task-actions">
                    {c.status === "SUBMITTED" && (
                      <>
                        <ConfirmAction
                          triggerLabel="Approve"
                          title="Approve insurance claim"
                          description={`Approve claim ${c.id} for ${formatMoney(c.amount)}. The server will reject stale state.`}
                          confirmLabel="Approve claim"
                          onConfirm={() =>
                            action.mutate({
                              id: c.id,
                              action: "approve",
                              amount: c.amount,
                            })
                          }
                        />
                        <ConfirmAction
                          triggerLabel="Deny"
                          title="Deny insurance claim"
                          description={`Deny claim ${c.id}. This records an approved amount of zero.`}
                          confirmLabel="Deny claim"
                          danger
                          onConfirm={() =>
                            action.mutate({
                              id: c.id,
                              action: "deny",
                              amount: 0,
                            })
                          }
                        />
                      </>
                    )}
                    {c.status === "APPROVED" && (
                      <ConfirmAction
                        triggerLabel="Reconcile"
                        title="Reconcile approved claim"
                        description="Reconciliation requires an exact remittance already recorded by the server."
                        confirmLabel="Reconcile claim"
                        onConfirm={() =>
                          action.mutate({
                            id: c.id,
                            action: "reconcile",
                            amount: c.amount,
                          })
                        }
                      />
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection title="Coverage verifications">
        <QueryState
          loading={coverage.isLoading}
          error={coverage.error}
          empty={!coverage.data?.length}
          onRetry={() => coverage.refetch()}
        >
          <Table
            caption="Coverage verification evidence"
            headers={["Service", "Status", "Insurance", "Patient", "Verified"]}
          >
            {coverage.data?.map((v) => (
              <tr key={v.id}>
                <td>{v.serviceCode}</td>
                <td>
                  <StatusBadge tone={statusTone(v.status)}>
                    {v.status}
                  </StatusBadge>
                </td>
                <td>{formatMoney(v.insuranceResponsibility)}</td>
                <td>{formatMoney(v.patientResponsibility)}</td>
                <td>{formatDate(v.verifiedAt)}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
