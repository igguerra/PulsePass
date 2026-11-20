import { screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { errorResponse } from '../../test/mocks/handlers'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

describe('TEST-FE-VEN-001: sedes', () => {
  it('lista las sedes activas con enlace a su detalle', async () => {
    renderWithProviders({ route: '/sedes' })
    const link = await screen.findByRole('link', { name: 'Marina Convention Center' })
    expect(link).toHaveAttribute('href', '/sedes/VEN-SMR-01')
    expect(screen.getByText('Santa Marta')).toBeInTheDocument()
  })

  it('lista vacía → estado vacío', async () => {
    server.use(http.get('*/api/venues/active', () => HttpResponse.json([])))
    renderWithProviders({ route: '/sedes' })
    expect(await screen.findByText('No hay sedes activas por ahora.')).toBeInTheDocument()
  })

  it('error del servidor → mensaje y botón Reintentar', async () => {
    server.use(http.get('*/api/venues/active', () => errorResponse(500, 'boom')))
    renderWithProviders({ route: '/sedes' })
    expect(await screen.findByRole('alert')).toHaveTextContent('Ocurrió un error inesperado')
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })

  it('detalle muestra los datos de la sede', async () => {
    renderWithProviders({ route: '/sedes/VEN-SMR-01' })
    expect(await screen.findByRole('heading', { name: 'Marina Convention Center' })).toBeInTheDocument()
    expect(screen.getByText('VEN-SMR-01')).toBeInTheDocument()
    expect(screen.getByText('3 personas')).toBeInTheDocument()
  })

  it('detalle con código inexistente → "No encontramos esta sede"', async () => {
    renderWithProviders({ route: '/sedes/NO-EXISTE' })
    expect(await screen.findByText('No encontramos esta sede')).toBeInTheDocument()
  })
})