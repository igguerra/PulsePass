interface LoadingProps {
  label?: string
  lines?: number
}

// Esqueleto genérico (9.20). Cada página puede armar uno con la forma de su contenido.
export default function Loading({ label = 'Cargando…', lines = 3 }: LoadingProps) {
  return (
    <div aria-busy="true" className="flex flex-col gap-3">
      <span className="sr-only">{label}</span>
      {Array.from({ length: lines }, (_, i) => (
        <div key={i} aria-hidden="true" className="h-16 animate-pulse rounded-xl bg-slate-200 dark:bg-slate-800" />
      ))}
    </div>
  )
}