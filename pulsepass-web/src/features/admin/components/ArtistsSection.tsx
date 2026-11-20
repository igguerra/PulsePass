import { useState } from 'react'
import Button from '../../../components/Button'
import Select from '../../../components/Select'
import { useToast } from '../../../components/toastContext'
import { getErrorMessage } from '../../../lib/http'
import { useActiveArtists } from '../../artists/hooks'
import { useAddArtistToEvent } from '../../events/hooks'
import type { EventResponse } from '../../events/types'

export default function ArtistsSection({ event }: { event: EventResponse }) {
  const artists = useActiveArtists()
  const addArtist = useAddArtistToEvent()
  const { showToast } = useToast()
  const [artistId, setArtistId] = useState('')

  const associatedIds = new Set(event.artists.map((artist) => artist.id))

  function associate() {
    addArtist.mutate(
      { eventCode: event.eventCode, artistId: Number(artistId) },
      {
        onSuccess: () => {
          setArtistId('')
          showToast('success', 'Artista asociado')
        },
      },
    )
  }

  return (
    <section aria-labelledby="associated-artists" className="flex flex-col gap-3">
      <h2 id="associated-artists" className="text-xl font-semibold">
        Artistas asociados
      </h2>

      {event.artists.length === 0 ? (
        <p className="text-slate-600 dark:text-slate-400">Aún no hay artistas asociados.</p>
      ) : (
        <ul className="flex flex-wrap gap-2">
          {event.artists.map((artist) => (
            <li key={artist.id} className="rounded-full bg-marca-50 px-4 py-2 text-sm font-medium text-marca-700 dark:bg-slate-800 dark:text-marca-500">
              {artist.stageName}
            </li>
          ))}
        </ul>
      )}

      <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
        <div className="flex-1">
          <Select label="Agregar artista" value={artistId} onChange={(e) => setArtistId(e.target.value)}>
            <option value="">Selecciona un artista…</option>
            {artists.data?.map((artist) => (
              // 9.18: los ya asociados se bloquean en la UI (el backend igual respondería 409)
              <option key={artist.id} value={artist.id} disabled={associatedIds.has(artist.id)}>
                {artist.stageName}
                {associatedIds.has(artist.id) ? ' (ya asociado)' : ''}
              </option>
            ))}
          </Select>
        </div>
        <Button onClick={associate} disabled={artistId === ''} loading={addArtist.isPending}>
          Asociar
        </Button>
      </div>

      {addArtist.isError && (
        <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-800 dark:bg-red-950/40 dark:text-red-300">
          {getErrorMessage(addArtist.error)}
        </p>
      )}
    </section>
  )
}
