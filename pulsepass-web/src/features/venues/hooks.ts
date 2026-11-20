import { useQuery } from '@tanstack/react-query'
import { getActiveVenues, getVenue } from './api'

// queryKeys de la tabla 14.1 (centralizadas para reutilizarlas al invalidar)
export const venueKeys = {
  active: ['sedes', 'activas'] as const,
  detail: (code: string) => ['sede', code] as const,
}

const FIVE_MINUTES = 5 * 60_000

export function useActiveVenues() {
  return useQuery({
    queryKey: venueKeys.active,
    queryFn: ({ signal }) => getActiveVenues(signal),
    staleTime: FIVE_MINUTES,
  })
}

export function useVenue(code: string) {
  return useQuery({
    queryKey: venueKeys.detail(code),
    queryFn: ({ signal }) => getVenue(code, signal),
    staleTime: FIVE_MINUTES,
  })
}