import { Link } from "react-router-dom";
import { startRouteFor } from "../../../app/routePolicy";
import { useAuth } from "../../../auth/AuthProvider";

export function ForbiddenPage() {
  const { session, logout } = useAuth();
  return (
    <main className="centered-state" id="main-content">
      <section className="state-card">
        <span className="state-mark" aria-hidden="true">
          !
        </span>
        <p className="eyebrow">Access denied · 403</p>
        <h1>You do not have access to this page</h1>
        <p>
          Your session is still active. This page requires a role that is not
          assigned to your account.
        </p>
        <div className="state-actions">
          {session && (
            <Link
              className="button button--primary"
              to={startRouteFor(session.user.role)}
            >
              Go to my start page
            </Link>
          )}
          <button
            className="button button--secondary"
            type="button"
            onClick={() => logout()}
          >
            Sign out
          </button>
        </div>
      </section>
    </main>
  );
}
