import { http, HttpResponse } from 'msw'
import { andreaTicket, artists, carlosTicket, cmfEvent, cmfSummary, marinaVenue, USERS } from './data'

// Respuestas "felices" de los 23 endpoints. En cada prueba se pueden pisar con
// server.use(http.get(..., () => errorResponse(409, 'mensaje')))

export function errorResponse(status: number, message: string, details: Record<string, string> = {}) {
  const reasons: Record<number, string> = {
    400: 'Bad Request',
    401: 'Unauthorized',
    403: 'Forbidden',
    404: 'Not Found',
    409: 'Conflict',
    500: 'Internal Server Error',
  }
  return HttpResponse.json(
    { timestamp: '2026-11-17T10:00:00', status, error: reasons[status] ?? 'Error', message, details },
    { status },
  )
}

const userFromToken = (request: Request) => {
  const token = request.headers.get('Authorization')?.replace('Bearer ', '')
  return Object.values(USERS).find((user) => user.token === token)
}

const API = '*/api'

export const handlers = [
  // ---- Auth (A1, A2) ----
  http.post(`${API}/auth/login`, async ({ request }) => {
    const { identifier, password } = (await request.json()) as { identifier: string; password: string }
    const user = Object.values(USERS).find((u) => u.profile.username === identifier || u.profile.email === identifier)
    if (!user || user.password !== password) return errorResponse(401, 'Invalid credentials')
    return HttpResponse.json({ accessToken: user.token, tokenType: 'Bearer', expiresIn: 900 })
  }),
  http.get(`${API}/auth/me`, ({ request }) => {
    const user = userFromToken(request)
    if (!user) return errorResponse(401, 'Unauthorized')
    return HttpResponse.json({ id: user.profile.id, username: user.profile.username, roles: user.roles })
  }),

  // ---- Venues (1, 2) ----
  http.get(`${API}/venues/active`, () => HttpResponse.json([marinaVenue])),
  http.get(`${API}/venues/:code`, ({ params }) =>
    params.code === marinaVenue.code ? HttpResponse.json(marinaVenue) : errorResponse(404, 'Venue not found'),
  ),

  // ---- Events (3-8) ----
  http.post(`${API}/events`, async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>
    return HttpResponse.json({ ...cmfEvent, ...body, status: 'DRAFT', artists: [] }, { status: 201 })
  }),
  http.get(`${API}/events/published`, () => HttpResponse.json([cmfSummary])),
  http.get(`${API}/events/by-artist`, () => HttpResponse.json([cmfSummary])),
  http.get(`${API}/events/:eventCode/tickets/paid`, () => HttpResponse.json([andreaTicket, carlosTicket])),
  http.get(`${API}/events/:eventCode`, ({ params }) =>
    params.eventCode === cmfEvent.eventCode ? HttpResponse.json(cmfEvent) : errorResponse(404, 'Event not found'),
  ),
  http.patch(`${API}/events/:eventCode/publish`, () => HttpResponse.json({ ...cmfEvent, status: 'PUBLISHED' })),
  http.post(`${API}/events/:eventCode/artists/:artistId`, ({ params }) => {
    const artist = artists.find((a) => String(a.id) === params.artistId)
    if (!artist) return errorResponse(404, 'Artist not found')
    return HttpResponse.json({ ...cmfEvent, artists: [...cmfEvent.artists, artist] })
  }),

  // ---- Artists (9-11) ----
  http.get(`${API}/artists/active`, () => HttpResponse.json(artists)),
  http.get(`${API}/artists/by-stage-name`, ({ request }) => {
    const name = new URL(request.url).searchParams.get('stageName')
    const artist = artists.find((a) => a.stageName === name)
    return artist ? HttpResponse.json(artist) : errorResponse(404, 'Artist not found')
  }),
  http.get(`${API}/artists/:id`, ({ params }) => {
    const artist = artists.find((a) => String(a.id) === params.id)
    return artist ? HttpResponse.json(artist) : errorResponse(404, 'Artist not found')
  }),

  // ---- Users (12-14, A3) ----
  http.post(`${API}/users`, async ({ request }) => {
    const { password: _omitted, ...data } = (await request.json()) as Record<string, unknown>
    return HttpResponse.json({ id: 99, active: true, phone: null, city: null, ...data }, { status: 201 })
  }),
  http.get(`${API}/users/by-username`, ({ request }) => {
    const username = new URL(request.url).searchParams.get('username')
    const user = Object.values(USERS).find((u) => u.profile.username === username)
    return user ? HttpResponse.json(user.profile) : errorResponse(404, 'User not found')
  }),
  http.get(`${API}/users/by-email`, ({ request }) => {
    const email = new URL(request.url).searchParams.get('email')
    const user = Object.values(USERS).find((u) => u.profile.email === email)
    return user ? HttpResponse.json(user.profile) : errorResponse(404, 'User not found')
  }),

  // ---- Tickets (15-20) ----
  http.post(`${API}/tickets`, async ({ request }) => {
    const { userEmail, eventCode, type } = (await request.json()) as Record<string, string>
    return HttpResponse.json({ ...andreaTicket, ticketCode: 'TKT-0100', userEmail, eventCode, type }, { status: 201 })
  }),
  http.get(`${API}/tickets/by-user`, () => HttpResponse.json([andreaTicket])),
  http.get(`${API}/tickets/:ticketCode`, ({ params }) => {
    const ticket = [andreaTicket, carlosTicket].find((t) => t.ticketCode === params.ticketCode)
    return ticket ? HttpResponse.json(ticket) : errorResponse(404, 'Ticket not found')
  }),
  http.patch(`${API}/tickets/:ticketCode/cancel`, () => HttpResponse.json({ ...andreaTicket, status: 'CANCELLED' })),
  http.patch(`${API}/tickets/:ticketCode/use`, () => HttpResponse.json({ ...andreaTicket, status: 'USED' })),
]