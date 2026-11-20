import { Link } from 'react-router'
import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import ArtistCard from '../features/artists/components/ArtistCard'
import { useActiveArtists } from '../features/artists/hooks'
import { useAuth } from '../features/auth/hooks'
import EventCard from '../features/events/components/EventCard'
import { usePublishedEvents } from '../features/events/hooks'
import { getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

const HERO_LINK =
  'flex min-h-11 items-center rounded-lg px-5 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white'

export default function Home() {
  const { session } = useAuth()

  return (
    <div className="flex flex-col gap-12">
      {/* Hero (DES: degradado marca-600 → acento-500) */}
      <section className="flex flex-col gap-4 rounded-2xl bg-linear-to-r from-marca-600 to-acento-500 px-6 py-12 text-white sm:px-10">
        <h1 className="text-3xl font-bold sm:text-4xl">Descubre los mejores eventos</h1>
        <p className="max-w-xl text-white/90">Música, cultura, deporte y tecnología en un solo lugar.</p>
        <div className="flex flex-wrap gap-3">
          <Link to={ROUTES.events} className={`${HERO_LINK} bg-white text-marca-700 hover:bg-marca-50`}>
            Ver cartelera
          </Link>
          {/* 9.3: "Crear cuenta" se oculta con sesión */}
          {session.status !== 'authenticated' && (
            <Link to={ROUTES.register} className={`${HERO_LINK} border border-white/70 hover:bg-white/10`}>
              Crear cuenta
            </Link>
          )}
        </div>
      </section>

      <UpcomingEvents />
      <FeaturedArtists />
    </div>
  )
}

function SectionHeader({ id, title, to }: { id: string; title: string; to: string }) {
  return (
    <div className="flex items-center justify-between gap-4">
      <h2 id={id} className="text-xl font-semibold">
        {title}
      </h2>
      <Link to={to} className="text-sm font-medium text-marca-600 hover:underline">
        Ver todos →
      </Link>
    </div>
  )
}

function UpcomingEvents() {
  const { data: events, isPending, isError, error, refetch } = usePublishedEvents()

  return (
    <section aria-labelledby="upcoming-events" className="flex flex-col gap-4">
      <SectionHeader id="upcoming-events" title="Próximos eventos" to={ROUTES.events} />
      {isPending ? (
        <Loading label="Cargando eventos…" />
      ) : isError ? (
        <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
      ) : events.length === 0 ? (
        <EmptyState message="Aún no hay eventos publicados." />
      ) : (
        <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {events.slice(0, 3).map((event) => (
            <li key={event.eventCode}>
              <EventCard event={event} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function FeaturedArtists() {
  const { data: artists, isPending, isError, error, refetch } = useActiveArtists()

  return (
    <section aria-labelledby="featured-artists" className="flex flex-col gap-4">
      <SectionHeader id="featured-artists" title="Artistas destacados" to={ROUTES.artists} />
      {isPending ? (
        <Loading label="Cargando artistas…" lines={2} />
      ) : isError ? (
        <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
      ) : artists.length === 0 ? (
        <EmptyState message="Aún no hay artistas destacados." />
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {artists.slice(0, 6).map((artist) => (
            <li key={artist.id}>
              <ArtistCard artist={artist} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
