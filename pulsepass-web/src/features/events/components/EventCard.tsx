import { Link } from 'react-router'
import Badge from '../../../components/Badge'
import Card from '../../../components/Card'
import { CATEGORY_LABEL, EVENT_STATUS_LABEL, formatDateTime } from '../../../lib/format'
import { ROUTES } from '../../../lib/routes'
import type { EventSummaryResponse } from '../types'

interface EventCardProps {
  event: EventSummaryResponse
}

// Tarjeta de la cartelera (FR-FE-EVT-001): nombre, fecha, categoría, estado y sede.
// Toda la tarjeta es clicable (9.4): el ::after del enlace la cubre completa.
export default function EventCard({ event }: EventCardProps) {
  return (
    <Card className="relative flex h-full flex-col gap-2 transition-shadow hover:shadow-md">
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-semibold tracking-wide text-marca-600 uppercase">
          {CATEGORY_LABEL[event.category]}
        </span>
        <Badge tone={event.status}>{EVENT_STATUS_LABEL[event.status]}</Badge>
      </div>
      <h2 className="text-lg font-semibold">
        <Link
          to={ROUTES.event(event.eventCode)}
          className="after:absolute after:inset-0 after:rounded-xl focus-visible:outline-none focus-visible:after:outline-2 focus-visible:after:outline-marca-600"
        >
          {event.name}
        </Link>
      </h2>
      <p className="text-sm text-slate-600 dark:text-slate-400">
        <time dateTime={event.eventDate}>{formatDateTime(event.eventDate)}</time> · {event.venueName}
      </p>
    </Card>
  )
}
