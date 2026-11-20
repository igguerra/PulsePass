import { Link, useParams } from 'react-router'
import Badge from '../components/Badge'
import Card from '../components/Card'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import MessageScreen from '../components/MessageScreen'
import ArtistsSection from '../features/admin/components/ArtistsSection'
import PublishSection from '../features/admin/components/PublishSection'
import { useEvent } from '../features/events/hooks'
import { CATEGORY_LABEL, EVENT_STATUS_LABEL, formatDateTime } from '../lib/format'
import { ApiError, getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function AdminManageEvent() {
  const { eventCode = '' } = useParams()
  // ADMIN ve también los borradores (FR-FE-ADM-003)
  const { data: event, isPending, isError, error, refetch } = useEvent(eventCode)

  if (isPending) return <Loading label="Cargando evento…" />

  if (isError) {
    if (error instanceof ApiError && error.status === 404) {
      return <MessageScreen code="404" title="No encontramos ese evento" />
    }
    return <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
  }

  return (
    <section className="flex flex-col gap-6">
      <Link to={ROUTES.adminEvents} className="text-sm text-marca-600 hover:underline">
        ← Eventos
      </Link>

      <Card className="flex flex-col gap-2">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-bold">
            {event.name} · <span className="text-slate-500">{event.eventCode}</span>
          </h1>
          <Badge tone={event.status}>{EVENT_STATUS_LABEL[event.status]}</Badge>
        </div>
        <p className="text-sm text-slate-600 dark:text-slate-400">
          {CATEGORY_LABEL[event.category]} · {formatDateTime(event.eventDate)} · Mín. {event.minimumAge} ·{' '}
          {event.venueName}
        </p>
      </Card>

      <Card>
        <PublishSection event={event} />
      </Card>

      <Card>
        <ArtistsSection event={event} />
      </Card>

      <Link to={ROUTES.staffPaidTickets(event.eventCode)} className="text-sm font-semibold text-marca-600 hover:underline">
        Ver tickets pagados →
      </Link>
    </section>
  )
}
