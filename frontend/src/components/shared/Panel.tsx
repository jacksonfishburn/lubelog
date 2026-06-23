import type { ReactNode } from 'react';

interface PanelProps {
  title?: string;
  actions?: ReactNode;
  accent?: boolean;
  flush?: boolean; // remove body padding (for lists/tables)
  className?: string;
  children: ReactNode;
}

// The core boxy chrome module: titlebar with accent stripe, then a body.
export function Panel({ title, actions, accent, flush, className, children }: PanelProps) {
  return (
    <section className={`panel${accent ? ' panel--accent' : ''}${className ? ` ${className}` : ''}`}>
      {title && (
        <div className="panel__titlebar">
          <span className="panel__title">{title}</span>
          {actions && <span className="right">{actions}</span>}
        </div>
      )}
      <div className={`panel__body${flush ? ' panel__body--flush' : ''}`}>{children}</div>
    </section>
  );
}
