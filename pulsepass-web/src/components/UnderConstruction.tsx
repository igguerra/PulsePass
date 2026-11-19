// Placeholder temporal: cada página lo reemplaza al implementarse
export default function UnderConstruction({ title }: { title: string }) {
  return (
    <section>
      <h1 className="text-2xl font-bold">{title}</h1>
      <p className="mt-2 text-slate-600 dark:text-slate-400">Página en construcción.</p>
    </section>
  )
}