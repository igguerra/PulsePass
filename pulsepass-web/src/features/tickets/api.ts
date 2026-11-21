import { z } from 'zod'
import { http } from '../../lib/http'
import { TicketResponseSchema, type PurchaseTicketRequest, type TicketResponse } from './types'

const TicketListSchema = z.array(TicketResponseSchema)

export function purchaseTicket(body: PurchaseTicketRequest): Promise<TicketResponse> {
  return http('/tickets', TicketResponseSchema, { method: 'POST', body })
}

export function getTicket(ticketCode: string): Promise<TicketResponse> {
  return http(`/tickets/${encodeURIComponent(ticketCode)}`, TicketResponseSchema)
}

export function getTicketsByUser(email: string): Promise<TicketResponse[]> {
  const query = new URLSearchParams({ email })
  return http(`/tickets/by-user?${query}`, TicketListSchema)
}

export function getEventPaidTickets(eventCode: string): Promise<TicketResponse[]> {
  return http(`/events/${encodeURIComponent(eventCode)}/tickets/paid`, TicketListSchema)
}

export function cancelTicket(ticketCode: string): Promise<TicketResponse> {
  return http(`/tickets/${encodeURIComponent(ticketCode)}/cancel`, TicketResponseSchema, {
    method: 'PATCH',
  })
}

export function markTicketUsed(ticketCode: string): Promise<TicketResponse> {
  return http(`/tickets/${encodeURIComponent(ticketCode)}/use`, TicketResponseSchema, {
    method: 'PATCH',
  })
}