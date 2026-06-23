import type { ReactNode } from 'react';

interface BannerProps {
  variant?: 'error' | 'warn';
  children: ReactNode;
}

export function Banner({ variant = 'error', children }: BannerProps) {
  return (
    <div className={`banner banner--${variant}`}>
      <span className="banner__icon">{variant === 'error' ? '⚠' : '◆'}</span>
      <span>{children}</span>
    </div>
  );
}
