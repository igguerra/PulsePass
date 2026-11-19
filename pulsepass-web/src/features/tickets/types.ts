import { z } from 'zod'
import { DateTimeSchema } from '../../lib/schemas'

export const TicketTypeSchema = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export type TicketType = z.infer<typeof TicketTypeSchema>

export const TicketStatusSchema = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])
export type TicketStatus = z.infer<typeof TicketStatusSchema>

// Espejo de TicketResponse. price es BigDecimal en Java → número en JSON.
export const TicketResponseSchema = z.object({
  id: z.number().int(),
  ticketCode: z.string(),
  type: TicketTypeSchema,
  price: z.number(),
  status: TicketStatusSchema,
  purchaseDate: DateTimeSchema,
  userEmail: z.string(),
  eventCode: z.string(),
  eventName: z.string(),
})
export type TicketResponse = z.infer<typeof TicketResponseSchema>

// Formulario de compra: la persona SOLO elige el tipo (anti-patrón 24.3: el email sale de la sesión)
export const PurchaseFormSchema = z.object({ type: TicketTypeSchema })
export type PurchaseForm = z.infer<typeof PurchaseFormSchema>

// Lo que realmente se envía a POST /api/tickets (PurchaseTicketRequest)
export interface PurchaseTicketRequest {
  userEmail: string
  eventCode: string
  type: TicketType
}

// Formulario de validación de ingreso (STAFF)
export const EntryValidationFormSchema = z.object({
  ticketCode: z.string().trim().min(1, 'Ingresa el código del ticket'),
})
export type EntryValidationForm = z.infer<typeof EntryValidationFormSchema>