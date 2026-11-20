import { z } from 'zod'
import { http } from '../../lib/http'
import {
  EventResponseSchema,
  EventSummaryResponseSchema,
  type CreateEventRequest,
  type EventResponse,
  type EventSummaryResponse,
} from './types'

const enc = encodeURIComponent

export function getPublishedEvents(signal?: AbortSignal): Promise<EventSummaryResponse[]> {
  return http('/events/published', z.array(EventSummaryResponseSchema), { signal })
}

export function getEvent(eventCode: string, signal?: AbortSignal): Promise<EventResponse> {
  return http(`/events/${enc(eventCode)}`, EventResponseSchema, { signal })
}

export function getEventsByArtist(stageName: string, signal?: AbortSignal): Promise<EventSummaryResponse[]> {
  const query = new URLSearchParams({ stageName })
  return http(`/events/by-artist?${query}`, z.array(EventSummaryResponseSchema), { signal })
}

// ---- ADMIN ----

export function createEvent(data: CreateEventRequest): Promise<EventResponse> {
  return http('/events', EventResponseSchema, { method: 'POST', body: data })
}

export function publishEvent(eventCode: string): Promise<EventResponse> {
  return http(`/events/${enc(eventCode)}/publish`, EventResponseSchema, { method: 'PATCH' })
}

export function addArtistToEvent(eventCode: string, artistId: number): Promise<EventResponse> {
  return http(`/events/${enc(eventCode)}/artists/${artistId}`, EventResponseSchema, { method: 'POST' })
}