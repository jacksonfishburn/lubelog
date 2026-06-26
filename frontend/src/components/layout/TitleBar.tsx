interface TitleBarProps {
  railCollapsed: boolean;
  onToggleRail: () => void;
}

function RailToggleIcon({ collapsed }: { collapsed: boolean }) {
  return (
    <svg
      className="titlebar__toggle-icon"
      viewBox="0 0 14 14"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="square"
      aria-hidden
    >
      {collapsed ? (
        <path d="M4 2 L10 7 L4 12" />
      ) : (
        <path d="M10 2 L4 7 L10 12" />
      )}
    </svg>
  );
}

export function TitleBar({ railCollapsed, onToggleRail }: TitleBarProps) {
  return (
    <header className="titlebar">
      <button
        type="button"
        className="btn btn--ghost titlebar__toggle"
        onClick={onToggleRail}
        aria-label={railCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        aria-expanded={!railCollapsed}
      >
        <RailToggleIcon collapsed={railCollapsed} />
      </button>
      <span className="titlebar__title">Maintenance Operations</span>
    </header>
  );
}
