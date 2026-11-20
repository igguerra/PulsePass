import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import Badge from '../components/Badge'
import Button from '../components/Button'
import Card from '../components/Card'
import TextField from '../components/TextField'
import { readCreatedEvents, type CreatedEvent } from '../features/admin/createdEvents'
import { useAuth } from '../features/auth/hooks'
import { useEvent } from '../features/events/hooks'
import { EVENT_STATUS_LABEL } from '../lib/format'
import { ROUTES } from '../lib/routes'

const SearchSchema = z.object({ eventCode: z.string().trim().min(1, 'Ingresa un código de evento') })
type SearchForm = z.infer<typeof SearchSchema>

const PRIMARY_LINK =
  'flex min-h-11 items-center rounded-lg bg-marca-600 px-4 text-sm font-semibold text-white hover:bg-marca-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600'

export default function AdminEvents() {
  const navigate = useNavigate()
  const { session } = useAuth()
  // Se lee una vez al entrar: la lista solo cambia cuando se crea un evento (en otra página)
  const [created] = useState(() =>
    session.status === 'authenticated' ? readCreatedEvents(session.user.username) : [],
  )

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SearchForm>({ resolver: zodResolver(SearchSchema), defaultValues: { eventCode: '' } })

  return (
    <section className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-bold">Administrar eventos</h1>
        <Link to={ROUTES.adminCreateEvent} className={PRIMARY_LINK}>
          + Nuevo evento
        </Link>
      </div>

      <Card>
        <form
          role="search"
          noValidate
          onSubmit={handleSubmit(({ eventCode }) => navigate(ROUTES.adminEvent(eventCode.trim())))}
          className="flex flex-col gap-3 sm:flex-row sm:items-start"
        >
          <div className="flex-1">
            <TextField
              label="Buscar evento por código"
              placeholder="CMF-2026"
              {...register('eventCode')}
              error={errors.eventCode?.message}
            />
          </div>
          <Button type="submit" className="sm:mt-6">
            Abrir
          </Button>
        </form>
      </Card>

      <section aria-labelledby="created-events" className="flex flex-col gap-3">
        <h2 id="created-events" className="text-xl font-semibold">
          Creados en esta sesión
        </h2>
        {created.length === 0 ? (
          <p className="text-slate-600 dark:text-slate-400">Aún no has creado eventos en esta sesión.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {created.map((event) => (
              <CreatedEventRow key={event.eventCode} event={event} />
            ))}
          </ul>
        )}
        <p className="text-sm text-slate-500">Nota: la API no ofrece un listado completo de eventos (GAP-01).</p>
      </section>
    </section>
  )
}

// Cada fila consulta su evento para mostrar el estado actual (sale de la caché si ya se cargó)
function CreatedEventRow({ event }: { event: CreatedEvent }) {
  const { data } = useEvent(event.eventCode)

  return (
    <li>
      <Card className="flex flex-wrap items-center justify-between gap-3">
        <span>
          <span className="font-semibold">{event.eventCode}</span> · {event.name}
        </span>
        <span className="flex items-center gap-3">
          {data && <Badge tone={data.status}>{EVENT_STATUS_LABEL[data.status]}</Badge>}
          <Link to={ROUTES.adminEvent(event.eventCode)} className="text-sm font-semibold text-marca-600 hover:underline">
            Gestionar
          </Link>
        </span>
      </Card>
    </li>
  )
}
