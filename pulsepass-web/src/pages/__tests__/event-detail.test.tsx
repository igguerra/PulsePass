import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { cmfEvent } from '../../test/mocks/data'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

const ROUTE = '/eventos/CMF-2026'

describe('Detalle de evento', () => {
  it('TEST-FE-EVT-004: muestra evento, artistas y sede', async () => {
    renderWithProviders({ route: ROUTE })
    expect(await screen.findByRole('heading', { name: 'Caribbean Music Fest 2026', level: 1 })).toBeInTheDocument()
    expect(screen.getByText('Música')).toBeInTheDocument()
    expect(screen.getByText('18 años')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Solar Beat' })).toHaveAttribute('href', '/artistas/1')
    expect(screen.getByRole('link', { name: 'Neon Waves' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Marina Convention Center' })).toHaveAttribute('href', '/sedes/VEN-SMR-01')
    expect(await screen.findByText('Santa Marta · Capacidad 3')).toBeInTheDocument()
  })

  it('TEST-FE-EVT-005: 404 → "Evento no disponible"', async () => {
    renderWithProviders({ route: '/eventos/NO-EXISTE' })
    expect(await screen.findByText('Evento no disponible')).toBeInTheDocument()
  })

  it('TEST-FE-EVT-006: evento SOLD_OUT → compra deshabilitada con motivo', async () => {
    server.use(http.get('*/api/events/:eventCode', () => HttpResponse.json({ ...cmfEvent, status: 'SOLD_OUT' })))
    renderWithProviders({ route: ROUTE, as: 'andrea' })
    expect(await screen.findByRole('button', { name: 'Comprar entrada' })).toBeDisabled()
    expect(screen.getByText('Las entradas están agotadas.')).toBeInTheDocument()
  })

  it('TEST-FE-TKT-002: anónimo → "Inicia sesión para comprar" lleva a /login con state.from', async () => {
    const user = userEvent.setup()
    const { router } = renderWithProviders({ route: ROUTE })
    await user.click(await screen.findByRole('link', { name: 'Inicia sesión para comprar' }))
    // /login se carga perezosamente: se espera a que aparezca antes de revisar la URL
    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/login')
  })

    it('USER elige el tipo y abre la confirmación con ese tipo', async () => {
    const user = userEvent.setup()
    renderWithProviders({ route: ROUTE, as: 'andrea' })
    expect(await screen.findByRole('radio', { name: 'General' })).toBeChecked()
    await user.click(screen.getByRole('radio', { name: 'VIP' }))
    await user.click(screen.getByRole('button', { name: 'Comprar entrada' }))
    const dialog = screen.getByRole('dialog', { name: 'Confirmar compra' })
    expect(dialog).toHaveTextContent('Tipo: VIP')
    expect(dialog).toHaveTextContent('Evento: Caribbean Music Fest 2026')
  })

  it('STAFF → el panel de compra no se muestra', async () => {
    renderWithProviders({ route: ROUTE, as: 'laura' })
    expect(await screen.findByRole('heading', { name: 'Caribbean Music Fest 2026', level: 1 })).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: /Laura/ })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Entradas' })).not.toBeInTheDocument()
  })
})
