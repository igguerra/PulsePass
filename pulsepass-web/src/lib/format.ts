import type { EventCategory, EventStatus } from '../features/events/types'
import type { TicketStatus, TicketType } from '../features/tickets/types'

const currency = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})
const dateTime = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
const longDate = new Intl.DateTimeFormat('es-CO', { dateStyle: 'long' })

export const formatCOP = (value: number): string => currency.format(value)

// "2026-12-12T20:00:00" sin zona → new Date lo interpreta en hora local (lo que queremos)
export const formatDateTime = (iso: string): string => dateTime.format(new Date(iso))

// "2008-05-20" → se arma con hora 00:00 local para que no se corra un día por UTC
export const formatDate = (isoDate: string): string => longDate.format(new Date(`${isoDate}T00:00:00`))

export const CATEGORY_LABEL: Record<EventCategory, string> = {
  MUSIC: 'Música',
  SPORTS: 'Deportes',
  TECHNOLOGY: 'Tecnología',
  EDUCATION: 'Educación',
  CULTURE: 'Cultura',
  ENTERTAINMENT: 'Entretenimiento',
}

export const EVENT_STATUS_LABEL: Record<EventStatus, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  SOLD_OUT: 'Agotado',
  CANCELLED: 'Cancelado',
  FINISHED: 'Finalizado',
}

export const TICKET_TYPE_LABEL: Record<TicketType, string> = {
  GENERAL: 'General',
  VIP: 'VIP',
  BACKSTAGE: 'Backstage',
  STUDENT: 'Estudiante',
}

export const TICKET_STATUS_LABEL: Record<TicketStatus, string> = {
  RESERVED: 'Reservado',
  PAID: 'Pagado',
  CANCELLED: 'Cancelado',
  USED: 'Usado',
}