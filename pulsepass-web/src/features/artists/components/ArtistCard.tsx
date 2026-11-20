import { Link } from 'react-router'
import Card from '../../../components/Card'
import { ROUTES } from '../../../lib/routes'
import type { ArtistResponse } from '../types'

interface ArtistCardProps {
  artist: ArtistResponse
}

export default function ArtistCard({ artist }: ArtistCardProps) {
  // "Electrónica · Colombia", omitiendo lo que venga null
  const details = [artist.genre, artist.country].filter(Boolean).join(' · ')

  return (
    <Card className="flex flex-col gap-1">
      <h2 className="text-lg font-semibold">
        <Link
          to={ROUTES.artist(artist.id)}
          className="hover:text-marca-600 focus-visible:outline-2 focus-visible:outline-marca-600"
        >
          {artist.stageName}
        </Link>
      </h2>
      {details && <p className="text-sm text-slate-600 dark:text-slate-400">{details}</p>}
    </Card>
  )
}