import { useAuth } from '../../auth/AuthProvider';

const roleNames: Record<string, string> = {
  ADMIN: 'Administrator', PATIENT: 'Patient', CLINICIAN: 'Clinician', NURSE: 'Nurse',
  REGISTRATION_STAFF: 'Registration staff', RECEPTIONIST: 'Receptionist', BILLING_STAFF: 'Billing staff',
  PHARMACIST: 'Pharmacist', LAB_STAFF: 'Laboratory staff', PRIVACY_OFFICER: 'Privacy officer',
  AUDITOR: 'Auditor', ANALYST: 'Analyst',
};

export function TopBar({ onMenuToggle, menuOpen }: { onMenuToggle(): void; menuOpen: boolean }) {
  const { session, logout } = useAuth();
  const email = session?.user.email ?? '';
  return <header className="topbar">
    <button className="menu-button" type="button" onClick={onMenuToggle} aria-label="Toggle primary navigation" aria-expanded={menuOpen}>☰</button>
    <div className="brand" aria-label="Northstar Health patient management">
      <span className="brand-mark" aria-hidden="true">N+</span>
      <span><strong>Northstar Health</strong><small>Patient management</small></span>
    </div>
    <span className="environment-badge">UAT · SYNTHETIC</span>
    <div className="topbar-spacer" />
    <div className="identity"><span className="avatar" aria-hidden="true">{email.slice(0, 2).toUpperCase()}</span><span><strong>{email}</strong><small>{roleNames[session?.user.role ?? ''] ?? 'Unknown role'}</small></span></div>
    <button className="topbar-action" type="button" onClick={() => logout('manual')}>Sign out</button>
  </header>;
}
