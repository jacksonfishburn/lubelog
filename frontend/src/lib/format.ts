// Display formatting helpers. Pure functions, no side effects.

export function formatMileage(miles: number | null | undefined): string {
  if (miles == null) return '—';
  return `${miles.toLocaleString('en-US')} mi`;
}

export function formatCurrency(amount: number | null | undefined): string {
  if (amount == null) return '—';
  return amount.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

// Renders a "YYYY-MM-DD" LocalDate without timezone drift (parse as local).
export function formatDate(isoDate: string | null | undefined): string {
  if (!isoDate) return '—';
  const [y, m, d] = isoDate.split('-').map(Number);
  if (!y || !m || !d) return isoDate;
  return new Date(y, m - 1, d).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function formatVehicleName(v: {
  nickname: string | null;
  year: number | null;
  make: string | null;
  model: string | null;
}): string {
  if (v.nickname) return v.nickname;
  const parts = [v.year, v.make, v.model].filter(Boolean);
  return parts.length ? parts.join(' ') : 'Unnamed vehicle';
}

export function formatVehicleSubtitle(v: {
  year: number | null;
  make: string | null;
  model: string | null;
  trim: string | null;
}): string {
  const parts = [v.year, v.make, v.model, v.trim].filter(Boolean);
  return parts.length ? parts.join(' ') : 'Details not set';
}

export function formatInterval(
  intervalMiles: number | null,
  intervalMonths: number | null,
): string {
  const parts: string[] = [];
  if (intervalMiles != null) parts.push(`${intervalMiles.toLocaleString('en-US')} mi`);
  if (intervalMonths != null) parts.push(`${intervalMonths} mo`);
  return parts.length ? `every ${parts.join(' / ')}` : 'no interval set';
}
