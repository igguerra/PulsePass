import { z } from 'zod'
import { http } from '../../lib/http'
import { ArtistResponseSchema, type ArtistResponse } from './types'

export function getActiveArtists(signal?: AbortSignal): Promise<ArtistResponse[]> {
  return http('/artists/active', z.array(ArtistResponseSchema), { signal })
}

export function getArtist(id: number, signal?: AbortSignal): Promise<ArtistResponse> {
  return http(`/artists/${id}`, ArtistResponseSchema, { signal })
}

export function getArtistByStageName(stageName: string, signal?: AbortSignal): Promise<ArtistResponse> {
  const query = new URLSearchParams({ stageName })
  return http(`/artists/by-stage-name?${query}`, ArtistResponseSchema, { signal })
}