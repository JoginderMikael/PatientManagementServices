import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return <main className="centered-state"><section className="state-card"><span className="state-mark" aria-hidden="true">?</span><p className="eyebrow">Not found · 404</p><h1>This page does not exist</h1><p>Check the address or return to sign in.</p><Link className="button button--primary" to="/login">Return to sign in</Link></section></main>;
}
