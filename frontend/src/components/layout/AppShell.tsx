import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TitleBar } from './TitleBar';

const STORAGE_KEY = 'lubelog-rail-collapsed';

// Overall frame: instrument nav rail on the left, titlebar + routed content.
export function AppShell() {
  const [railCollapsed, setRailCollapsed] = useState(() => {
    try {
      return localStorage.getItem(STORAGE_KEY) === '1';
    } catch {
      return false;
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, railCollapsed ? '1' : '0');
    } catch {
      // ignore
    }
  }, [railCollapsed]);

  return (
    <div className={`app-shell${railCollapsed ? ' is-rail-collapsed' : ''}`}>
      <Sidebar collapsed={railCollapsed} />
      <div className="app-main">
        <TitleBar
          railCollapsed={railCollapsed}
          onToggleRail={() => setRailCollapsed((c) => !c)}
        />
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
