import { z } from 'zod'
import { http } from '../../lib/http'
import { VenueResponseSchema, type VenueResponse } from './types'

export function getActiveVenues(signal?: AbortSignal): Promise<VenueResponse[]> {
  return http('/venues/active', z.array(VenueResponseSchema), { signal })
}

export function getVenue(code: string, signal?: AbortSignal): Promise<VenueResponse> {
  return http(`/venues/${encodeURIComponent(code)}`, VenueResponseSchema, { signal })
}