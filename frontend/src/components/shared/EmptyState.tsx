import type { ReactNode } from 'react';

interface EmptyStateProps {
  icon?: string;
  title: string;
  message?: string;
  action?: ReactNode;
}

export function EmptyState({ icon = '▢', title, message, action }: EmptyStateProps) {
  return (
    <div className="empty">
      <div className="empty__icon">{icon}</div>
      <div className="empty__title">{title}</div>
      {message && <div className="muted" style={{ maxWidth: 360 }}>{message}</div>}
      {action}
    </div>
  );
}
