import { Link } from 'react-router'
import Card from '../../../components/Card'
import { ROUTES } from '../../../lib/routes'
import type { VenueResponse } from '../types'

interface VenueCardProps {
  venue: VenueResponse
}

export default function VenueCard({ venue }: VenueCardProps) {
  return (
    <Card className="flex flex-col gap-1">
      <h2 className="text-lg font-semibold">
        <Link
          to={ROUTES.venue(venue.code)}
          className="hover:text-marca-600 focus-visible:outline-2 focus-visible:outline-marca-600"
        >
          {venue.name}
        </Link>
      </h2>
      <p className="text-sm text-slate-600 dark:text-slate-400">{venue.city}</p>
      <p className="text-sm">Capacidad: {venue.capacity.toLocaleString('es-CO')} personas</p>
    </Card>
  )
}