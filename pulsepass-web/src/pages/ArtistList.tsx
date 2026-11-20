import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import ArtistCard from '../features/artists/components/ArtistCard'
import { useActiveArtists } from '../features/artists/hooks'
import { getErrorMessage } from '../lib/http'

export default function ArtistList() {
  const { data: artists, isPending, isError, error, refetch } = useActiveArtists()

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold">Artistas</h1>

      {isPending ? (
        <Loading label="Cargando artistas…" />
      ) : isError ? (
        <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
      ) : artists.length === 0 ? (
        <EmptyState message="No hay artistas activos por ahora." />
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {artists.map((artist) => (
            <li key={artist.id}>
              <ArtistCard artist={artist} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
