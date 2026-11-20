import { Link } from 'react-router'
import Badge from '../../../components/Badge'
import Card from '../../../components/Card'
import { CATEGORY_LABEL, EVENT_STATUS_LABEL, formatDateTime } from '../../../lib/format'
import { ROUTES } from '../../../lib/routes'
import type { EventSummaryResponse } from '../types'

interface EventCardProps {
  event: EventSummaryResponse
}

// Tarjeta de la cartelera (FR-FE-EVT-001): nombre, fecha, categoría, estado y sede
export default function EventCard({ event }: EventCardProps) {
  return (
    <Card className="flex h-full flex-col gap-2">
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-semibold tracking-wide text-marca-600 uppercase">
          {CATEGORY_LABEL[event.category]}
        </span>
        <Badge tone={event.status}>{EVENT_STATUS_LABEL[event.status]}</Badge>
      </div>
      <h2 className="text-lg font-semibold">
        <Link
          to={ROUTES.event(event.eventCode)}
          className="hover:text-marca-600 focus-visible:outline-2 focus-visible:outline-marca-600"
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