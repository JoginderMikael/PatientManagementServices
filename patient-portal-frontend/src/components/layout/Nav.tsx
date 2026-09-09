import { NavLink } from 'react-router-dom';
import { NavigationItem } from '../../app/routePolicy';

export function Nav({ items, open, onNavigate }: { items: readonly NavigationItem[]; open: boolean; onNavigate(): void }) {
  return <>
    {open && <button className="nav-scrim" type="button" aria-label="Close navigation" onClick={onNavigate} />}
    <aside className={`sidebar${open ? ' sidebar--open' : ''}`} aria-label="Primary navigation">
      <p className="nav-label">Workspace</p>
      <nav><ul className="nav-list">{items.map(item => <li key={item.to}>
        <NavLink to={item.to} onClick={onNavigate} className={({ isActive }) => `nav-link${isActive ? ' nav-link--active' : ''}`}>
          <span className="nav-abbreviation" aria-hidden="true">{item.abbreviation}</span><span>{item.label}</span>
        </NavLink>
      </li>)}</ul></nav>
      <div className="privacy-reminder"><strong>UAT environment</strong><span>Use synthetic information only. Activity may be audited.</span></div>
    </aside>
  </>;
}
