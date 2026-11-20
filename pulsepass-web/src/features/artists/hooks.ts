import { useQuery } from '@tanstack/react-query'
import { getActiveArtists, getArtist, getArtistByStageName } from './api'

export const artistKeys = {
  active: ['artistas', 'activos'] as const,
  detail: (id: number) => ['artista', id] as const,
  byStageName: (stageName: string) => ['artista', 'nombre', stageName] as const,
}

const FIVE_MINUTES = 5 * 60_000

export function useActiveArtists() {
  return useQuery({
    queryKey: artistKeys.active,
    queryFn: ({ signal }) => getActiveArtists(signal),
    staleTime: FIVE_MINUTES,
  })
}

// id = null cuando el parámetro de la URL no es un número: no se llama a la API (TEST-FE-ART-003)
export function useArtist(id: number | null) {
  return useQuery({
    queryKey: artistKeys.detail(id ?? -1),
    queryFn: ({ signal }) => getArtist(id!, signal),
    enabled: id !== null,
    staleTime: FIVE_MINUTES,
  })
}

// Solo consulta cuando hay un nombre escrito
export function useArtistByStageName(stageName: string) {
  return useQuery({
    queryKey: artistKeys.byStageName(stageName),
    queryFn: ({ signal }) => getArtistByStageName(stageName, signal),
    enabled: stageName.trim() !== '',
    staleTime: FIVE_MINUTES,
  })
}