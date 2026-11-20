import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import VenueCard from '../features/venues/components/VenueCard'
import { useActiveVenues } from '../features/venues/hooks'
import { getErrorMessage } from '../lib/http'

export default function VenueList() {
  const { data: venues, isPending, isError, error, refetch } = useActiveVenues()

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold">Sedes</h1>

      {isPending ? (
        <Loading label="Cargando sedes…" />
      ) : isError ? (
        <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
      ) : venues.length === 0 ? (
        <EmptyState message="No hay sedes activas por ahora." />
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {venues.map((venue) => (
            <li key={venue.code}>
              <VenueCard venue={venue} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
