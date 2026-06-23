export function Spinner() {
  return <span className="spinner" />;
}

export function LoadingBlock({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="loading-block">
      <span className="spinner" />
      {label}…
    </div>
  );
}
