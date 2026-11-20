import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  addArtistToEvent,
  createEvent,
  getEvent,
  getEventsByArtist,
  getPublishedEvents,
  publishEvent,
} from './api'

// queryKeys de la tabla 14.1
export const eventKeys = {
  published: ['eventos', 'publicados'] as const,
  detail: (eventCode: string) => ['evento', eventCode] as const,
  byArtistAll: ['eventos', 'artista'] as const,
  byArtist: (stageName: string) => ['eventos', 'artista', stageName] as const,
}

const ONE_MINUTE = 60_000

export function usePublishedEvents() {
  return useQuery({
    queryKey: eventKeys.published,
    queryFn: ({ signal }) => getPublishedEvents(signal),
    staleTime: ONE_MINUTE,
  })
}

export function useEvent(eventCode: string) {
  return useQuery({
    queryKey: eventKeys.detail(eventCode),
    queryFn: ({ signal }) => getEvent(eventCode, signal),
    staleTime: 30_000,
  })
}

export function useEventsByArtist(stageName: string) {
  return useQuery({
    queryKey: eventKeys.byArtist(stageName),
    queryFn: ({ signal }) => getEventsByArtist(stageName, signal),
    enabled: stageName.trim() !== '',
    staleTime: ONE_MINUTE,
  })
}

// ---- Mutaciones ADMIN (sección 14.2) ----

export function useCreateEvent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createEvent,
    // "siembra" la caché: la página de gestión no necesita volver a pedir el evento
    onSuccess: (event) => queryClient.setQueryData(eventKeys.detail(event.eventCode), event),
  })
}

export function usePublishEvent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: publishEvent,
    onSuccess: (event) => {
      queryClient.setQueryData(eventKeys.detail(event.eventCode), event)
      return queryClient.invalidateQueries({ queryKey: eventKeys.published })
    },
  })
}

export function useAddArtistToEvent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ eventCode, artistId }: { eventCode: string; artistId: number }) =>
      addArtistToEvent(eventCode, artistId),
    onSuccess: (event) => {
      queryClient.setQueryData(eventKeys.detail(event.eventCode), event)
      return queryClient.invalidateQueries({ queryKey: eventKeys.byArtistAll })
    },
  })
}