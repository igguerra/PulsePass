import { screen, waitFor, within } from '@testing-library/react'
import userEvent, { type UserEvent } from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { afterEach, describe, expect, it } from 'vitest'
import { ROUTES } from '../../lib/routes'
import { andreaTicket, carlosTicket, cmfEvent, USERS } from '../../test/mocks/data'
import { errorResponse } from '../../test/mocks/handlers'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

afterEach(() => {
  sessionStorage.clear()
})

const paidTicket = { ...andreaTicket, status: 'PAID' as const }
const usedTicket = { ...andreaTicket, ticketCode: 'TKT-USED-1', status: 'USED' as const }

async function openPurchaseModal(user: UserEvent) {
  await user.click(await screen.findByRole('radio', { name: 'VIP' }))
  await user.click(screen.getByRole('button', { name: 'Comprar entrada' }))
  return screen.getByRole('dialog', { name: 'Confirmar compra' })
}

async function openCancelModal(user: UserEvent) {
  await user.click(await screen.findByRole('button', { name: 'Cancelar' }))
  return screen.getByRole('dialog', { name: 'Cancelar ticket' })
}

describe('Compra de tickets', () => {
  it('TEST-FE-TKT-001: compra válida → 201 y navega a la confirmación', async () => {
    server.use(http.post('*/api/tickets', () => HttpResponse.json(paidTicket, { status: 201 })))
    const user = userEvent.setup()
    const { router } = renderWithProviders({ route: ROUTES.event(cmfEvent.eventCode), as: 'andrea' })

    const dialog = await openPurchaseModal(user)
    await user.click(within(dialog).getByRole('button', { name: 'Confirmar' }))

    expect(
      await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' }),
    ).toBeInTheDocument()
    expect(router.state.location.pathname).toBe(ROUTES.purchaseConfirmation(paidTicket.ticketCode))
  })

  it('TEST-FE-TKT-003: compra 409 → mensaje del backend dentro del modal, sin cerrarlo', async () => {
    const message = 'El usuario no cumple la edad mínima del evento.'
    server.use(http.post('*/api/tickets', () => errorResponse(409, message)))
    const user = userEvent.setup()
    const { router } = renderWithProviders({ route: ROUTES.event(cmfEvent.eventCode), as: 'andrea' })

    const dialog = await openPurchaseModal(user)
    await user.click(within(dialog).getByRole('button', { name: 'Confirmar' }))

    expect(await within(dialog).findByText(message)).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Confirmar compra' })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe(ROUTES.event(cmfEvent.eventCode))
  })

  it('TEST-FE-TKT-004: la compra envía el email de la sesión', async () => {
    let sentBody: unknown
    server.use(
      http.post('*/api/tickets', async ({ request }) => {
        sentBody = await request.json()
        return HttpResponse.json(paidTicket, { status: 201 })
      }),
    )
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTES.event(cmfEvent.eventCode), as: 'andrea' })

    const dialog = await openPurchaseModal(user)
    await user.click(within(dialog).getByRole('button', { name: 'Confirmar' }))

    await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' })
    expect(sentBody).toEqual({
      userEmail: USERS.andrea.profile.email,
      eventCode: cmfEvent.eventCode,
      type: 'VIP',
    })
  })
})

describe('Mis tickets', () => {
  it('TEST-FE-TKT-005: lista los tickets y filtra por estado', async () => {
    server.use(http.get('*/api/tickets/by-user', () => HttpResponse.json([paidTicket, usedTicket])))
    const user = userEvent.setup()
    const { router } = renderWithProviders({ route: ROUTES.myTickets, as: 'andrea' })

    expect(await screen.findByText(paidTicket.ticketCode)).toBeInTheDocument()
    expect(screen.getByText(usedTicket.ticketCode)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Usados' }))

    expect(screen.getByText(usedTicket.ticketCode)).toBeInTheDocument()
    expect(screen.queryByText(paidTicket.ticketCode)).not.toBeInTheDocument()
    expect(router.state.location.search).toBe('?status=USED')
  })

  it('TEST-FE-TKT-006: sin tickets → estado vacío', async () => {
    server.use(http.get('*/api/tickets/by-user', () => HttpResponse.json([])))
    renderWithProviders({ route: ROUTES.myTickets, as: 'andrea' })

    expect(await screen.findByText('Aún no tienes tickets')).toBeInTheDocument()
  })

  it('TEST-FE-TKT-007: cancelar un ticket PAID → 200 y la lista se actualiza', async () => {
    let cancelled = false
    server.use(
      http.get('*/api/tickets/by-user', () =>
        HttpResponse.json([{ ...paidTicket, status: cancelled ? 'CANCELLED' : 'PAID' }]),
      ),
      http.patch('*/api/tickets/:ticketCode/cancel', () => {
        cancelled = true
        return HttpResponse.json({ ...paidTicket, status: 'CANCELLED' })
      }),
    )
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTES.myTickets, as: 'andrea' })

    const dialog = await openCancelModal(user)
    await user.click(within(dialog).getByRole('button', { name: 'Cancelar ticket' }))

    expect(await screen.findByText('Cancelado')).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Cancelar' })).not.toBeInTheDocument()
    })
  })

  it('TEST-FE-TKT-008: cancelar → 409 muestra el mensaje y mantiene el modal', async () => {
    const message = 'El ticket no puede cancelarse en su estado actual.'
    server.use(
      http.get('*/api/tickets/by-user', () => HttpResponse.json([paidTicket])),
      http.patch('*/api/tickets/:ticketCode/cancel', () => errorResponse(409, message)),
    )
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTES.myTickets, as: 'andrea' })

    const dialog = await openCancelModal(user)
    await user.click(within(dialog).getByRole('button', { name: 'Cancelar ticket' }))

    expect(await within(dialog).findByText(message)).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Cancelar ticket' })).toBeInTheDocument()
  })

  it('TEST-FE-TKT-009: ticket ajeno (403) → "Ticket no disponible" sin datos', async () => {
    server.use(http.get('*/api/tickets/:ticketCode', () => errorResponse(403, 'Forbidden')))
    renderWithProviders({ route: ROUTES.ticket(carlosTicket.ticketCode), as: 'andrea' })

    expect(await screen.findByRole('heading', { name: 'Ticket no disponible' })).toBeInTheDocument()
    expect(screen.queryByText(carlosTicket.ticketCode)).not.toBeInTheDocument()
  })

  it('TEST-FE-TKT-010: el botón Cancelar se oculta si el estado no es PAID', async () => {
    server.use(http.get('*/api/tickets/by-user', () => HttpResponse.json([usedTicket])))
    renderWithProviders({ route: ROUTES.myTickets, as: 'andrea' })

    expect(await screen.findByText(usedTicket.ticketCode)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cancelar' })).not.toBeInTheDocument()
  })
})