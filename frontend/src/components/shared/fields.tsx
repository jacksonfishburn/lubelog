import type { ReactNode } from 'react';

interface FieldShellProps {
  label: string;
  required?: boolean;
  error?: string | null;
  hint?: ReactNode;
  className?: string;
  children: ReactNode;
}

// Shared label + error/hint chrome for every form control.
export function FormField({ label, required, error, hint, className, children }: FieldShellProps) {
  return (
    <label className={`field${className ? ` ${className}` : ''}`}>
      <span className="field__label">
        {label}
        {required && <span className="req">*</span>}
      </span>
      {children}
      {error ? (
        <span className="field__error">⚠ {error}</span>
      ) : (
        hint && <span className="field__hint">{hint}</span>
      )}
    </label>
  );
}

interface TextFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  error?: string | null;
  hint?: ReactNode;
  maxLength?: number;
  className?: string;
}

export function TextField({ label, value, onChange, placeholder, required, error, hint, maxLength, className }: TextFieldProps) {
  return (
    <FormField label={label} required={required} error={error} hint={hint} className={className}>
      <input
        className={`input${error ? ' has-error' : ''}`}
        value={value}
        placeholder={placeholder}
        maxLength={maxLength}
        onChange={(e) => onChange(e.target.value)}
      />
    </FormField>
  );
}

interface NumberFieldProps {
  label: string;
  value: number | null;
  onChange: (value: number | null) => void;
  placeholder?: string;
  required?: boolean;
  error?: string | null;
  hint?: ReactNode;
  min?: number;
  className?: string;
}

export function NumberField({ label, value, onChange, placeholder, required, error, hint, min, className }: NumberFieldProps) {
  return (
    <FormField label={label} required={required} error={error} hint={hint} className={className}>
      <input
        className={`input${error ? ' has-error' : ''}`}
        type="number"
        inputMode="numeric"
        min={min}
        value={value ?? ''}
        placeholder={placeholder}
        onChange={(e) => {
          const raw = e.target.value;
          onChange(raw === '' ? null : Number(raw));
        }}
      />
    </FormField>
  );
}

interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

interface SelectFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: SelectOption[];
  placeholder?: string;
  required?: boolean;
  error?: string | null;
  hint?: ReactNode;
  className?: string;
}

export function SelectField({ label, value, onChange, options, placeholder, required, error, hint, className }: SelectFieldProps) {
  return (
    <FormField label={label} required={required} error={error} hint={hint} className={className}>
      <select
        className={`select${error ? ' has-error' : ''}`}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((o) => (
          <option key={o.value} value={o.value} disabled={o.disabled}>
            {o.label}
          </option>
        ))}
      </select>
    </FormField>
  );
}

interface CheckboxFieldProps {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  hint?: ReactNode;
  className?: string;
}

export function CheckboxField({ label, checked, onChange, hint, className }: CheckboxFieldProps) {
  return (
    <div className={`field${className ? ` ${className}` : ''}`}>
      <label className="checkbox">
        <input
          type="checkbox"
          className="checkbox__input"
          checked={checked}
          onChange={(e) => onChange(e.target.checked)}
        />
        <span className="checkbox__label">{label}</span>
      </label>
      {hint && <span className="field__hint">{hint}</span>}
    </div>
  );
}

interface TextAreaFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  error?: string | null;
  className?: string;
}

export function TextAreaField({ label, value, onChange, placeholder, required, error, className }: TextAreaFieldProps) {
  return (
    <FormField label={label} required={required} error={error} className={className}>
      <textarea
        className="textarea"
        value={value}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
      />
    </FormField>
  );
}
