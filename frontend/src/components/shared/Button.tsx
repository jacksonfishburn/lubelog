import type { ButtonHTMLAttributes, ReactNode } from 'react';

type Variant = 'default' | 'primary' | 'danger' | 'ghost';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: 'sm' | 'md';
  block?: boolean;
  loading?: boolean;
  icon?: ReactNode;
}

const VARIANT_CLASS: Record<Variant, string> = {
  default: '',
  primary: ' btn--primary',
  danger: ' btn--danger',
  ghost: ' btn--ghost',
};

export function Button({
  variant = 'default',
  size = 'md',
  block,
  loading,
  icon,
  children,
  className,
  disabled,
  ...rest
}: ButtonProps) {
  const cls =
    'btn' +
    VARIANT_CLASS[variant] +
    (size === 'sm' ? ' btn--sm' : '') +
    (block ? ' btn--block' : '') +
    (className ? ` ${className}` : '');
  return (
    <button className={cls} disabled={disabled || loading} {...rest}>
      {loading ? <span className="spinner" /> : icon && <span className="btn__icon">{icon}</span>}
      {children}
    </button>
  );
}
