import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import type { EventSummaryResponse } from '../../features/events/types'
import { cmfSummary } from '../../test/mocks/data'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

const techSummit: EventSummaryResponse = {
  ...cmfSummary,
  id: 2,
  eventCode: 'TECH-2026',
  name: 'Tech Summit Caribe',
  category: 'TECHNOLOGY',
  status: 'SOLD_OUT',
}

describe('Cartelera', () => {
  it('TEST-FE-EVT-001: lista los eventos publicados', async () => {
    server.use(http.get('*/api/events/published', () => HttpResponse.json([cmfSummary, techSummit])))
    renderWithProviders({ route: '/eventos' })
    expect(await screen.findByRole('link', { name: 'Caribbean Music Fest 2026' })).toHaveAttribute(
      'href',
      '/eventos/CMF-2026',
    )
    expect(screen.getByRole('link', { name: 'Tech Summit Caribe' })).toBeInTheDocument()
    expect(screen.getByText('2 eventos')).toBeInTheDocument()
    expect(screen.getByText('Agotado')).toBeInTheDocument()
  })

  it('TEST-FE-EVT-002: lista vacía → estado vacío', async () => {
    server.use(http.get('*/api/events/published', () => HttpResponse.json([])))
    renderWithProviders({ route: '/eventos' })
    expect(await screen.findByText('Aún no hay eventos publicados.')).toBeInTheDocument()
  })

  it('TEST-FE-EVT-003: filtrar por texto y categoría actualiza la URL', async () => {
    const user = userEvent.setup()
    server.use(http.get('*/api/events/published', () => HttpResponse.json([cmfSummary, techSummit])))
    const { router } = renderWithProviders({ route: '/eventos' })
    await screen.findByText('2 eventos')

    await user.type(screen.getByRole('searchbox', { name: 'Buscar' }), 'tech')
    expect(router.state.location.search).toBe('?q=tech')
    expect(screen.getByText('1 evento')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Caribbean Music Fest 2026' })).not.toBeInTheDocument()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Categoría' }), 'MUSIC')
    expect(new URLSearchParams(router.state.location.search).get('categoria')).toBe('MUSIC')
    expect(screen.getByText('Sin resultados para tu búsqueda.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Limpiar' }))
    expect(router.state.location.search).toBe('')
    expect(screen.getByText('2 eventos')).toBeInTheDocument()
  })

  it('los filtros de la URL se aplican al cargar (sobreviven a recargar)', async () => {
    server.use(http.get('*/api/events/published', () => HttpResponse.json([cmfSummary, techSummit])))
    renderWithProviders({ route: '/eventos?categoria=TECHNOLOGY' })
    expect(await screen.findByText('1 evento')).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: 'Categoría' })).toHaveValue('TECHNOLOGY')
  })

  it('categoría inválida en la URL se ignora', async () => {
    server.use(http.get('*/api/events/published', () => HttpResponse.json([cmfSummary, techSummit])))
    renderWithProviders({ route: '/eventos?categoria=HACK' })
    expect(await screen.findByText('2 eventos')).toBeInTheDocument()
  })

  it('con ?artista= usa los eventos de ese artista', async () => {
    renderWithProviders({ route: '/eventos?artista=Solar%20Beat' })
    expect(await screen.findByRole('link', { name: 'Caribbean Music Fest 2026' })).toBeInTheDocument()
    expect(await screen.findByRole('combobox', { name: 'Artista' })).toHaveValue('Solar Beat')
  })

  it('TEST-FE-ART-004: con ?artista= resuelve el artista por nombre y enlaza a su perfil', async () => {
    renderWithProviders({ route: '/eventos?artista=Solar%20Beat' })
    const banner = await screen.findByText(/Eventos de/)
    expect(within(banner).getByText('Solar Beat')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver perfil del artista →' })).toHaveAttribute('href', '/artistas/1')
  })

  it('TEST-FE-ART-005: artista inexistente en ?artista= → aviso "No encontramos al artista"', async () => {
    server.use(http.get('*/api/events/by-artist', () => HttpResponse.json([])))
    renderWithProviders({ route: '/eventos?artista=Artista%20Fantasma' })
    expect(await screen.findByText('No encontramos al artista “Artista Fantasma”.')).toBeInTheDocument()
  })
})

describe('Inicio', () => {
  it('muestra el hero, próximos eventos y artistas destacados', async () => {
    renderWithProviders({ route: '/' })
    expect(screen.getByRole('heading', { name: 'Descubre los mejores eventos' })).toBeInTheDocument()
    const events = await screen.findByRole('region', { name: 'Próximos eventos' })
    expect(await within(events).findByRole('link', { name: 'Caribbean Music Fest 2026' })).toBeInTheDocument()
    const artists = screen.getByRole('region', { name: 'Artistas destacados' })
    expect(await within(artists).findByRole('link', { name: 'Solar Beat' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Crear cuenta' })).toBeInTheDocument()
  })

  it('con sesión iniciada oculta "Crear cuenta"', async () => {
    renderWithProviders({ route: '/', as: 'andrea' })
    expect(await screen.findByRole('button', { name: /Andrea/ })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Crear cuenta' })).not.toBeInTheDocument()
  })
})
