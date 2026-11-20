import { screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

describe('Artistas', () => {
  it('TEST-FE-ART-001: lista de artistas activos', async () => {
    renderWithProviders({ route: '/artistas' })
    expect(await screen.findByRole('link', { name: 'Solar Beat' })).toHaveAttribute('href', '/artistas/1')
    expect(screen.getByRole('link', { name: 'Neon Waves' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Caribbean Sound' })).toBeInTheDocument()
  })

  it('lista vacía → estado vacío', async () => {
    server.use(http.get('*/api/artists/active', () => HttpResponse.json([])))
    renderWithProviders({ route: '/artistas' })
    expect(await screen.findByText('No hay artistas activos por ahora.')).toBeInTheDocument()
  })

  it('TEST-FE-ART-002: detalle de artista con sus eventos', async () => {
    renderWithProviders({ route: '/artistas/1' })
    expect(await screen.findByRole('heading', { name: 'Solar Beat', level: 1 })).toBeInTheDocument()
    expect(screen.getByText('Electrónica · Colombia')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'Caribbean Music Fest 2026' })).toHaveAttribute(
      'href',
      '/eventos/CMF-2026',
    )
  })

  it('artista sin eventos → estado vacío en la sección de eventos', async () => {
    server.use(http.get('*/api/events/by-artist', () => HttpResponse.json([])))
    renderWithProviders({ route: '/artistas/1' })
    expect(await screen.findByText('Este artista no tiene eventos publicados.')).toBeInTheDocument()
  })

  it('TEST-FE-ART-003: id no numérico → 404 sin llamar a la API', async () => {
    let calls = 0
    server.use(
      http.get('*/api/artists/:id', () => {
        calls += 1
        return HttpResponse.json({})
      }),
    )
    renderWithProviders({ route: '/artistas/abc' })
    expect(await screen.findByText('No encontramos este artista')).toBeInTheDocument()
    expect(calls).toBe(0)
  })

  it('id inexistente → 404 de la API', async () => {
    renderWithProviders({ route: '/artistas/999' })
    expect(await screen.findByText('No encontramos este artista')).toBeInTheDocument()
  })
})
