import { Link } from 'react-router'
import { ApiError } from '../../../lib/http'
import { ROUTES } from '../../../lib/routes'
import { useArtistByStageName } from '../hooks'

interface ArtistFilterBannerProps {
  stageName: string
}

// FR-FE-ART-003: resuelve el ?artista= de la cartelera a un artista real
// con GET /api/artists/by-stage-name (operación 10 de la tabla de la sección 10).
export default function ArtistFilterBanner({ stageName }: ArtistFilterBannerProps) {
  const query = useArtistByStageName(stageName)

  if (query.isPending) return null

  if (query.isError) {
    const notFound = query.error instanceof ApiError && query.error.status === 404
    if (!notFound) return null // otros errores ya los muestra la lista de eventos
    return (
      <p role="status" className="rounded-lg bg-amber-100 p-3 text-sm text-amber-900 dark:bg-amber-950 dark:text-amber-200">
        No encontramos al artista “{stageName}”.
      </p>
    )
  }

  const artist = query.data
  return (
    <p
      role="status"
      className="flex flex-wrap items-center gap-x-2 rounded-lg bg-marca-50 p-3 text-sm text-slate-800 dark:bg-slate-800 dark:text-slate-100"
    >
      <span>
        Eventos de <strong>{artist.stageName}</strong>
      </span>
      <Link to={ROUTES.artist(artist.id)} className="font-medium text-marca-600 hover:underline dark:text-marca-500">
        Ver perfil del artista →
      </Link>
    </p>
  )
}