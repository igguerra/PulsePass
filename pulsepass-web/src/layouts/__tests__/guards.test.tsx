import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderWithProviders } from '../../test/utils'

describe('Route guards', () => {
  it('TEST-FE-AUTH-007: anónimo en ruta protegida → /login con state.from', async () => {
    const { router } = renderWithProviders({ route: '/mis-tickets' })
    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/login')
    expect(router.state.location.state).toMatchObject({ from: '/mis-tickets' })
  })

  it('TEST-FE-AUTH-008: USER en /admin → /403', async () => {
    const { router } = renderWithProviders({ route: '/admin/eventos', as: 'andrea' })
    expect(await screen.findByText('No tienes permisos para acceder a esta sección.')).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/403')
  })

  it('TEST-FE-AUTH-009: ADMIN en /admin → renderiza', async () => {
    renderWithProviders({ route: '/admin/eventos', as: 'admin' })
    expect(await screen.findByRole('heading', { name: 'Administrar eventos' })).toBeInTheDocument()
  })

  it('TEST-FE-GEN-001: ruta inexistente → 404', async () => {
    renderWithProviders({ route: '/no-existe' })
    expect(await screen.findByText('No encontramos esta página')).toBeInTheDocument()
  })

  it('TEST-FE-GEN-003: navbar muestra enlaces según rol', async () => {
    renderWithProviders({ as: 'laura' })
    const nav = await screen.findByRole('navigation', { name: 'Principal' })
    expect(await screen.findByRole('button', { name: /Laura/ })).toBeInTheDocument()
    expect(nav).toHaveTextContent('Validar ingreso')
    expect(nav).not.toHaveTextContent('Mis tickets')
    expect(nav).not.toHaveTextContent('Administrar eventos')
  })
})