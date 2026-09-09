export function Skeleton({
  label = "Loading",
  lines = 3,
}: {
  label?: string;
  lines?: number;
}) {
  return (
    <div className="skeleton" role="status" aria-label={label}>
      {Array.from({ length: lines }, (_, index) => (
        <span key={index} />
      ))}
      <span className="sr-only">{label}</span>
    </div>
  );
}
