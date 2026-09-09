export function ErrorState({ title, message, requestId, onRetry }: { title: string; message: string; requestId?: string; onRetry?(): void }) {
  return <section className="error-state" role="alert"><span className="error-mark" aria-hidden="true">!</span><h1>{title}</h1><p>{message}</p>{requestId && <p className="request-id">Request ID: <code>{requestId}</code></p>}{onRetry && <button className="button button--primary" type="button" onClick={onRetry}>Try again</button>}</section>;
}
