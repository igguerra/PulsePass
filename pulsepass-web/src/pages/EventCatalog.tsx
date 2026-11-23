import { useSearchParams } from 'react-router'
import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import EventCard from '../features/events/components/EventCard'
import EventFiltersBar from '../features/events/components/EventFiltersBar'
import { filterEvents, readEventFilters } from '../features/events/filters'
import { useEventsByArtist, usePublishedEvents } from '../features/events/hooks'
import { getErrorMessage } from '../lib/http'
import ArtistFilterBanner from '../features/artists/components/ArtistFilterBanner'

export default function EventCatalog() {
  // FR-FE-EVT-002: los filtros viven en la URL (?q=&categoria=&artista=) → compartibles y sobreviven a recargar
  const [searchParams, setSearchParams] = useSearchParams()
  const filters = readEventFilters(searchParams)

  // Los hooks se llaman siempre; se usa uno u otro según haya artista elegido
  const published = usePublishedEvents()
  const byArtist = useEventsByArtist(filters.artist)
  const query = filters.artist ? byArtist : published

  function updateFilter(name: 'q' | 'categoria' | 'artista', value: string) {
    setSearchParams(
      (previous) => {
        const next = new URLSearchParams(previous)
        if (value) next.set(name, value)
        else next.delete(name)
        return next
      },
      { replace: true }, // escribir en el buscador no llena el historial del navegador
    )
  }

  const visibleEvents = query.data ? filterEvents(query.data, filters) : []

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold">Cartelera</h1>

      <EventFiltersBar filters={filters} onChange={updateFilter} onClear={() => setSearchParams({}, { replace: true })} />

      {filters.artist && <ArtistFilterBanner stageName={filters.artist} />}

      {query.isPending ? (
        <Loading label="Cargando eventos…" />
      ) : query.isError ? (
        <ErrorState message={getErrorMessage(query.error)} onRetry={() => void query.refetch()} />
      ) : query.data.length === 0 && !filters.artist ? (
        <EmptyState message="Aún no hay eventos publicados." />
      ) : visibleEvents.length === 0 ? (
        <EmptyState message="Sin resultados para tu búsqueda." />
      ) : (
        <>
          <p aria-live="polite" className="text-sm text-slate-600 dark:text-slate-400">
            {visibleEvents.length} {visibleEvents.length === 1 ? 'evento' : 'eventos'}
          </p>
          <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {visibleEvents.map((event) => (
              <li key={event.eventCode}>
                <EventCard event={event} />
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  )
}
