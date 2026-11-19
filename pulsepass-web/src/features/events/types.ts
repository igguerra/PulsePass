import { z } from 'zod'
import { DateTimeSchema } from '../../lib/schemas'
import { ArtistResponseSchema } from '../artists/types'

export const EventCategorySchema = z.enum([
  'MUSIC',
  'SPORTS',
  'TECHNOLOGY',
  'EDUCATION',
  'CULTURE',
  'ENTERTAINMENT',
])
export type EventCategory = z.infer<typeof EventCategorySchema>

export const EventStatusSchema = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])
export type EventStatus = z.infer<typeof EventStatusSchema>

// Espejo de EventSummaryResponse (listados: sin descripción ni artistas)
export const EventSummaryResponseSchema = z.object({
  id: z.number().int(),
  eventCode: z.string(),
  name: z.string(),
  category: EventCategorySchema,
  status: EventStatusSchema,
  eventDate: DateTimeSchema,
  venueName: z.string(),
})
export type EventSummaryResponse = z.infer<typeof EventSummaryResponseSchema>

// Espejo de EventResponse (detalle completo)
export const EventResponseSchema = z.object({
  id: z.number().int(),
  eventCode: z.string(),
  name: z.string(),
  description: z.string().nullable(),
  category: EventCategorySchema,
  status: EventStatusSchema,
  eventDate: DateTimeSchema,
  minimumAge: z.number().int(),
  venueCode: z.string(),
  venueName: z.string(),
  artists: z.array(ArtistResponseSchema),
})
export type EventResponse = z.infer<typeof EventResponseSchema>

// Formulario "Crear evento" (= CreateEventRequest). Solo validación estructural (sección 11).
export const CreateEventRequestSchema = z.object({
  eventCode: z.string().trim().min(1, 'Ingresa el código del evento'),
  name: z.string().trim().min(1, 'Ingresa el nombre'),
  description: z.string().max(1000, 'Máximo 1000 caracteres'),
  category: EventCategorySchema,
  // <input type="datetime-local"> entrega "2026-12-12T20:00", que Spring acepta como LocalDateTime
  eventDate: z.string().min(1, 'Selecciona la fecha y hora'),
  minimumAge: z.number({ error: 'Ingresa un número' }).int('Debe ser entero').min(0, 'Debe ser 0 o mayor'),
  venueCode: z.string().min(1, 'Selecciona una sede'),
})
export type CreateEventRequest = z.infer<typeof CreateEventRequestSchema>