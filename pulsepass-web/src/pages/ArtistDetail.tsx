import { Link, useParams } from 'react-router'
import Card from '../components/Card'
import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import MessageScreen from '../components/MessageScreen'
import { useArtist } from '../features/artists/hooks'
import EventCard from '../features/events/components/EventCard'
import { useEventsByArtist } from '../features/events/hooks'
import { ApiError, getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

// "/artistas/7" → 7 · "/artistas/abc" → null (FE-002: los parámetros de URL se validan)
function parseId(raw: string | undefined): number | null {
  if (!raw || !/^\d+$/.test(raw)) return null
  const id = Number(raw)
  return id > 0 ? id : null
}

export default function ArtistDetail() {
  const params = useParams()
  const id = parseId(params.id)
  const { data: artist, isPending, isError, error, refetch } = useArtist(id)

  // Id inválido: 404 sin llamar a la API (TEST-FE-ART-003)
  if (id === null) return <MessageScreen code="404" title="No encontramos este artista" />

  if (isPending) return <Loading label="Cargando artista…" />

  if (isError) {
    if (error instanceof ApiError && error.status === 404) {
      return <MessageScreen code="404" title="No encontramos este artista" />
    }
    return <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
  }

  const details = [artist.genre, artist.country].filter(Boolean).join(' · ')

  return (
    <section className="flex flex-col gap-6">
      <Link to={ROUTES.artists} className="text-sm text-marca-600 hover:underline">
        ← Volver a artistas
      </Link>

      <Card>
        <h1 className="text-2xl font-bold">{artist.stageName}</h1>
        {details && <p className="text-slate-600 dark:text-slate-400">{details}</p>}
      </Card>

      <ArtistEvents stageName={artist.stageName} />
    </section>
  )
}

// Componente aparte: su propia consulta y sus propios 4 estados
function ArtistEvents({ stageName }: { stageName: string }) {
  const { data: events, isPending, isError, error, refetch } = useEventsByArtist(stageName)

  return (
    <section aria-labelledby="artist-events" className="flex flex-col gap-4">
      <h2 id="artist-events" className="text-xl font-semibold">
        Eventos
      </h2>
      {isPending ? (
        <Loading label="Cargando eventos…" lines={2} />
      ) : isError ? (
        <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
      ) : events.length === 0 ? (
        <EmptyState message="Este artista no tiene eventos publicados." />
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {events.map((event) => (
            <li key={event.eventCode}>
              <EventCard event={event} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
