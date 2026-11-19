import type { ArtistResponse } from '../../features/artists/types'
import type { Role } from '../../features/auth/types'
import type { EventResponse, EventSummaryResponse } from '../../features/events/types'
import type { TicketResponse } from '../../features/tickets/types'
import type { UserResponse } from '../../features/users/types'
import type { VenueResponse } from '../../features/venues/types'

// Escenario de referencia del PRD (sección 22). Tipados con los tipos reales:
// si un esquema zod cambia, TypeScript avisa aquí.

export const marinaVenue: VenueResponse = {
  id: 1,
  code: 'VEN-SMR-01',
  name: 'Marina Convention Center',
  city: 'Santa Marta',
  address: 'Calle 1 # 2-3',
  capacity: 3,
  active: true,
}

export const artists: ArtistResponse[] = [
  { id: 1, stageName: 'Solar Beat', country: 'Colombia', genre: 'Electrónica', active: true },
  { id: 2, stageName: 'Neon Waves', country: 'Colombia', genre: 'Synthpop', active: true },
  { id: 3, stageName: 'Caribbean Sound', country: 'Colombia', genre: 'Champeta', active: true },
]

export const cmfEvent: EventResponse = {
  id: 1,
  eventCode: 'CMF-2026',
  name: 'Caribbean Music Fest 2026',
  description: 'Festival de música del Caribe colombiano.',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  minimumAge: 18,
  venueCode: marinaVenue.code,
  venueName: marinaVenue.name,
  artists: [artists[0]!, artists[1]!],
}

export const cmfSummary: EventSummaryResponse = {
  id: cmfEvent.id,
  eventCode: cmfEvent.eventCode,
  name: cmfEvent.name,
  category: cmfEvent.category,
  status: cmfEvent.status,
  eventDate: cmfEvent.eventDate,
  venueName: cmfEvent.venueName,
}

interface DemoUser {
  token: string
  password: string
  roles: Role[]
  profile: UserResponse
}

const profile = (id: number, username: string, firstName: string, birthDate: string): UserResponse => ({
  id,
  username,
  email: `${username}@pulsepass.test`,
  active: true,
  firstName,
  lastName: 'Demo',
  phone: null,
  city: 'Santa Marta',
  birthDate,
})

export const USERS = {
  andrea: { token: 'token-andrea', password: 'ClaveSegura123', roles: ['USER'], profile: profile(1, 'andrea', 'Andrea', '1998-03-10') },
  carlos: { token: 'token-carlos', password: 'ClaveSegura123', roles: ['USER'], profile: profile(2, 'carlos', 'Carlos', '1995-07-21') },
  laura: { token: 'token-laura', password: 'ClaveSegura123', roles: ['STAFF'], profile: profile(3, 'laura', 'Laura', '2009-01-15') },
  admin: { token: 'token-admin', password: 'ClaveSegura123', roles: ['ADMIN'], profile: profile(4, 'admin', 'Admin', '1990-01-01') },
} satisfies Record<string, DemoUser>

export type DemoUserName = keyof typeof USERS

export const andreaTicket: TicketResponse = {
  id: 1,
  ticketCode: 'TKT-0001',
  type: 'VIP',
  price: 250000,
  status: 'PAID',
  purchaseDate: '2026-11-10T10:00:00',
  userEmail: USERS.andrea.profile.email,
  eventCode: cmfEvent.eventCode,
  eventName: cmfEvent.name,
}

export const carlosTicket: TicketResponse = {
  ...andreaTicket,
  id: 2,
  ticketCode: 'TKT-0002',
  type: 'GENERAL',
  price: 120000,
  userEmail: USERS.carlos.profile.email,
}