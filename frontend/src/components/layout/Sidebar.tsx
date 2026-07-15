import { NavLink } from 'react-router-dom';
import { logout } from '../../auth/keycloak';
import { useAppState } from '../../context/AppState';
import { useCurrentUser } from '../../hooks/useCurrentUser';
import { Button } from '../shared/Button';

interface NavItem {
  to: string;
  label: string;
  icon: string;
}

const NAV: NavItem[] = [
  { to: '/dashboard', label: 'Dashboard', icon: '▣' },
  { to: '/fleet', label: 'Fleet', icon: '⊞' },
  { to: '/ai-find-parts', label: 'AI Find Parts', icon: '◈' },
  { to: '/service-types', label: 'Service Types', icon: '▤' },
];

interface SidebarProps {
  collapsed: boolean;
}

export function Sidebar({ collapsed }: SidebarProps) {
  const { fleet } = useAppState();
  const { user } = useCurrentUser();
  const overdueTotal = fleet.overviews.reduce((sum, o) => sum + o.overdueCount, 0);

  return (
    <nav className="app-rail" aria-label="Main navigation">
      <div className="rail-brand">
        <span className="rail-brand__mark">L</span>
        <span className="rail-brand__name">
          LUBE<span>LOG</span>
        </span>
      </div>

      <div className="rail-nav">
        <div className="rail-section label-mono">Terminal</div>
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            title={collapsed ? item.label : undefined}
            className={({ isActive }) => `rail-link${isActive ? ' is-active' : ''}`}
          >
            <span className="rail-link__icon">{item.icon}</span>
            <span className="rail-link__label">{item.label}</span>
            {item.to === '/fleet' && overdueTotal > 0 && (
              <span className="rail-link__badge">{overdueTotal}</span>
            )}
          </NavLink>
        ))}
      </div>

      <div className="rail-footer">
        <span className="rail-footer__avatar" title={user?.email ?? undefined}>
          {(user?.email ?? '?')[0].toUpperCase()}
        </span>
        <div className="rail-footer__meta grow">
          <div className="label-mono">Signed in</div>
          <div className="rail-footer__email">{user?.email ?? 'loading…'}</div>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="rail-footer__logout"
          onClick={logout}
          title="Sign out"
          aria-label="Sign out"
        >
          {collapsed ? '↪' : 'Sign out'}
        </Button>
      </div>
    </nav>
  );
}
