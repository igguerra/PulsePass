import { z } from 'zod'

const enc = encodeURIComponent

// FE-003: ningún string de ruta suelto en componentes; todo sale de aquí
export const ROUTES = {
  home: '/',
  events: '/eventos',
  event: (eventCode: string) => `/eventos/${enc(eventCode)}`,
  artists: '/artistas',
  artist: (id: number) => `/artistas/${id}`,
  venues: '/sedes',
  venue: (code: string) => `/sedes/${enc(code)}`,
  login: '/login',
  register: '/registro',
  account: '/cuenta',
  myTickets: '/mis-tickets',
  ticket: (ticketCode: string) => `/mis-tickets/${enc(ticketCode)}`,
  purchaseConfirmation: (ticketCode: string) => `/compra/confirmacion/${enc(ticketCode)}`,
  staffValidation: '/staff/validacion',
  staffPaidTickets: (eventCode: string) => `/staff/eventos/${enc(eventCode)}/tickets`,
  adminEvents: '/admin/eventos',
  adminCreateEvent: '/admin/eventos/nuevo',
  adminEvent: (eventCode: string) => `/admin/eventos/${enc(eventCode)}`,
  forbidden: '/403',
} as const

// location.state viene "de afuera" → se valida (FE-002)
const NavigationStateSchema = z.object({
  from: z.string().startsWith('/').optional(),
  reason: z.literal('expired').optional(),
  registered: z.boolean().optional(),
})
export type NavigationState = z.infer<typeof NavigationStateSchema>

export function readNavigationState(state: unknown): NavigationState {
  const result = NavigationStateSchema.safeParse(state)
  return result.success ? result.data : {}
}