import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../auth/hooks'
import {
  cancelTicket,
  getEventPaidTickets,
  getTicket,
  getTicketsByUser,
  markTicketUsed,
  purchaseTicket,
} from './api'

// Email del usuario con sesión iniciada (undefined si no hay sesión)
function useSessionEmail(): string | undefined {
  const { session } = useAuth()
  return session.status === 'authenticated' ? session.user.email : undefined
}

export function usePurchaseTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: purchaseTicket,
    onSuccess: async (_ticket, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['tickets', 'usuario', variables.userEmail] }),
        queryClient.invalidateQueries({ queryKey: ['evento', variables.eventCode] }),
      ])
    },
  })
}

export function useTicket(ticketCode: string) {
  return useQuery({
    queryKey: ['ticket', ticketCode],
    queryFn: () => getTicket(ticketCode),
    enabled: ticketCode.length > 0,
    staleTime: 15_000,
  })
}

export function useMyTickets() {
  const email = useSessionEmail()
  return useQuery({
    queryKey: ['tickets', 'usuario', email],
    queryFn: () => getTicketsByUser(email ?? ''),
    enabled: Boolean(email),
    staleTime: 15_000,
  })
}

export function useEventPaidTickets(eventCode: string) {
  return useQuery({
    queryKey: ['tickets', 'pagados', eventCode],
    queryFn: () => getEventPaidTickets(eventCode),
    enabled: eventCode.length > 0,
    staleTime: 15_000,
  })
}

interface TicketRef {
  ticketCode: string
  eventCode: string
}

export function useCancelTicket() {
  const queryClient = useQueryClient()
  const email = useSessionEmail()
  return useMutation({
    mutationFn: ({ ticketCode }: TicketRef) => cancelTicket(ticketCode),
    onSuccess: async (_data, { ticketCode, eventCode }) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['tickets', 'usuario', email] }),
        queryClient.invalidateQueries({ queryKey: ['ticket', ticketCode] }),
        queryClient.invalidateQueries({ queryKey: ['tickets', 'pagados', eventCode] }),
      ])
    },
  })
}

export function useMarkTicketUsed() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ ticketCode }: TicketRef) => markTicketUsed(ticketCode),
    onSuccess: async (_data, { ticketCode, eventCode }) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['ticket', ticketCode] }),
        queryClient.invalidateQueries({ queryKey: ['tickets', 'pagados', eventCode] }),
      ])
    },
  })
}