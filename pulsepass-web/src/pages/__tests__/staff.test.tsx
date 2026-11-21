import { screen } from '@testing-library/react'
import userEvent, { type UserEvent } from '@testing-library/user-event'
import { http } from 'msw'
import { afterEach, describe, expect, it } from 'vitest'
import { ROUTES } from '../../lib/routes'
import { andreaTicket, carlosTicket, cmfEvent } from '../../test/mocks/data'
import { errorResponse } from '../../test/mocks/handlers'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

afterEach(() => {
  sessionStorage.clear()
})

async function searchTicket(user: UserEvent, code: string) {
  await user.type(await screen.findByLabelText('Código de ticket'), code)
  await user.click(screen.getByRole('button', { name: 'Buscar' }))
}

describe('STAFF · validación de ingreso', () => {
  it('TEST-FE-STF-001: buscar y marcar como usado → éxito', async () => {
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTES.staffValidation, as: 'admin' })

    await searchTicket(user, andreaTicket.ticketCode)
    await user.click(await screen.findByRole('button', { name: 'Marcar como usado' }))

    expect(await screen.findByText('Ingreso registrado.')).toBeInTheDocument()
  })

  it('TEST-FE-STF-002: segunda vez → el 409 se ve en pantalla', async () => {
    const message = 'El ticket ya fue utilizado.'
    server.use(http.patch('*/api/tickets/:ticketCode/use', () => errorResponse(409, message)))
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTES.staffValidation, as: 'admin' })

    await searchTicket(user, andreaTicket.ticketCode)
    await user.click(await screen.findByRole('button', { name: 'Marcar como usado' }))

    expect(await screen.findByText(message)).toBeInTheDocument()
  })

  it('TEST-FE-STF-003: ticket inexistente → "Ticket no encontrado"', async () => {
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTES.staffValidation, as: 'admin' })

    await searchTicket(user, 'TKT-NO-EXISTE')

    expect(await screen.findByText('Ticket no encontrado')).toBeInTheDocument()
  })
})

describe('STAFF · tickets pagados por evento', () => {
  it('TEST-FE-STF-004: muestra la tabla con las filas', async () => {
    renderWithProviders({ route: ROUTES.staffPaidTickets(cmfEvent.eventCode), as: 'admin' })

    expect(await screen.findByRole('table')).toBeInTheDocument()
    expect(screen.getByText(andreaTicket.ticketCode)).toBeInTheDocument()
    expect(screen.getByText(carlosTicket.ticketCode)).toBeInTheDocument()
    // encabezado + 2 filas
    expect(screen.getAllByRole('row')).toHaveLength(3)
  })
})