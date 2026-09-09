import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { navigationFor } from '../../app/routePolicy';
import { useAuth } from '../../auth/AuthProvider';
import { Nav } from './Nav';
import { TopBar } from './TopBar';

export function AppShell({ surface }: { surface: 'patient' | 'staff' }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const { session } = useAuth();
  const items = session ? navigationFor(session.user.role, surface) : [];
  return <div className="app-shell">
    <a className="skip-link" href="#main-content">Skip to main content</a>
    <TopBar menuOpen={menuOpen} onMenuToggle={() => setMenuOpen(value => !value)} />
    <div className="shell-body">
      <Nav items={items} open={menuOpen} onNavigate={() => setMenuOpen(false)} />
      <main id="main-content" className="main-content" tabIndex={-1}><Outlet /></main>
    </div>
  </div>;
}
