import { Link, useParams } from 'react-router'
import Badge from '../components/Badge'
import Card from '../components/Card'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import MessageScreen from '../components/MessageScreen'
import PurchasePanel from '../features/events/components/PurchasePanel'
import { useEvent } from '../features/events/hooks'
import type { EventResponse } from '../features/events/types'
import { useVenue } from '../features/venues/hooks'
import { CATEGORY_LABEL, EVENT_STATUS_LABEL, formatDateTime } from '../lib/format'
import { ApiError, getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function EventDetail() {
  const { eventCode = '' } = useParams()
  const { data: event, isPending, isError, error, refetch } = useEvent(eventCode)

  if (isPending) return <Loading label="Cargando evento…" />

  if (isError) {
    // FR-FE-EVT-003: el backend responde 404 tanto si no existe como si es un borrador
    if (error instanceof ApiError && error.status === 404) {
      return <MessageScreen code="404" title="Evento no disponible" />
    }
    return <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
  }

  return (
    <div className="flex flex-col gap-6">
      <Link to={ROUTES.events} className="text-sm text-marca-600 hover:underline">
        ← Volver a la cartelera
      </Link>

      <div className="grid gap-6 lg:grid-cols-[2fr_1fr] lg:items-start">
        <article className="flex flex-col gap-6">
          <EventHeader event={event} />
          <EventArtists event={event} />
          <EventVenue venueCode={event.venueCode} venueName={event.venueName} />
        </article>

        <aside className="lg:sticky lg:top-20">
          <PurchasePanel event={event} />
        </aside>
      </div>
    </div>
  )
}

function EventHeader({ event }: { event: EventResponse }) {
  return (
    <Card className="flex flex-col gap-3">
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-xs font-semibold tracking-wide text-marca-600 uppercase">
          {CATEGORY_LABEL[event.category]}
        </span>
        <Badge tone={event.status}>{EVENT_STATUS_LABEL[event.status]}</Badge>
      </div>
      <h1 className="text-3xl font-bold">{event.name}</h1>
      <dl className="flex flex-wrap gap-x-6 gap-y-1 text-sm">
        <div className="flex gap-1">
          <dt className="sr-only">Fecha</dt>
          <dd>
            <span aria-hidden="true">📅 </span>
            <time dateTime={event.eventDate}>{formatDateTime(event.eventDate)}</time>
          </dd>
        </div>
        <div className="flex gap-1">
          <dt>Edad mínima:</dt>
          <dd className="font-medium">{event.minimumAge > 0 ? `${event.minimumAge} años` : 'Todas las edades'}</dd>
        </div>
      </dl>
      {event.description && <p className="text-slate-700 dark:text-slate-300">{event.description}</p>}
    </Card>
  )
}

function EventArtists({ event }: { event: EventResponse }) {
  return (
    <section aria-labelledby="event-artists" className="flex flex-col gap-3">
      <h2 id="event-artists" className="text-xl font-semibold">
        Artistas
      </h2>
      {event.artists.length === 0 ? (
        <p className="text-slate-600 dark:text-slate-400">Aún no hay artistas confirmados.</p>
      ) : (
        <ul className="flex flex-wrap gap-3">
          {event.artists.map((artist) => (
            <li key={artist.id}>
              <Link
                to={ROUTES.artist(artist.id)}
                className="flex min-h-11 items-center rounded-full bg-marca-50 px-4 text-sm font-medium text-marca-700 hover:bg-marca-500 hover:text-white dark:bg-slate-800 dark:text-marca-500"
              >
                {artist.stageName}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

// El evento solo trae código y nombre de la sede; ciudad y capacidad salen de useVenue
function EventVenue({ venueCode, venueName }: { venueCode: string; venueName: string }) {
  const { data: venue } = useVenue(venueCode)

  return (
    <section aria-labelledby="event-venue" className="flex flex-col gap-3">
      <h2 id="event-venue" className="text-xl font-semibold">
        Sede
      </h2>
      <Card className="flex flex-col gap-1">
        <Link to={ROUTES.venue(venueCode)} className="font-semibold hover:text-marca-600">
          {venueName}
        </Link>
        {venue && (
          <p className="text-sm text-slate-600 dark:text-slate-400">
            {venue.city} · Capacidad {venue.capacity.toLocaleString('es-CO')}
          </p>
        )}
      </Card>
    </section>
  )
}
