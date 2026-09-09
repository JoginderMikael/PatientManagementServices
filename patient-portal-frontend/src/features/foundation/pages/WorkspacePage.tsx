import { PageHeader } from '../../../components/PageHeader';
import { Alert } from '../../../components/feedback/Alert';
import { EmptyState } from '../../../components/feedback/EmptyState';

export function WorkspacePage({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return <><PageHeader eyebrow={eyebrow} title={title} description={description} />
    <Alert title="Foundation ready" tone="info">This protected route, shared shell and role policy are ready for its phase-specific workflow.</Alert>
    <div className="surface-card"><EmptyState title="No workflow data loaded" message="Live feature data will be connected in the delivery phase assigned to this route." /></div>
  </>;
}
