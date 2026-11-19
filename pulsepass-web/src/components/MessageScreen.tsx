import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { ROUTES } from '../lib/routes'

interface MessageScreenProps {
  code?: string
  title: string
  description?: string
  actions?: ReactNode
}

// Base visual de 403, 404 y error de ruta (9.19)
export default function MessageScreen({ code, title, description, actions }: MessageScreenProps) {
  return (
    <section className="mx-auto flex max-w-md flex-col items-center gap-4 py-16 text-center">
      {code && <p className="font-titulo text-6xl font-bold text-marca-600">{code}</p>}
      <h1 className="text-2xl font-bold">{title}</h1>
      {description && <p className="text-slate-600 dark:text-slate-400">{description}</p>}
      <div className="flex flex-wrap justify-center gap-3">
        {actions}
        <Link
          to={ROUTES.home}
          className="flex min-h-11 items-center rounded-lg bg-marca-600 px-4 text-sm font-semibold text-white hover:bg-marca-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600"
        >
          Volver al inicio
        </Link>
      </div>
    </section>
  )
}