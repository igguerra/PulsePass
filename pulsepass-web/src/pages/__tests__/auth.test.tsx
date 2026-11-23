import { fireEvent, screen, waitFor } from '@testing-library/react'
import userEvent, { type UserEvent } from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { afterEach, describe, expect, it } from 'vitest'
import { errorResponse } from '../../test/mocks/handlers'
import { USERS } from '../../test/mocks/data'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

afterEach(() => {
  sessionStorage.clear()
})

async function fillLogin(user: UserEvent, identifier: string, password: string) {
  await user.type(await screen.findByLabelText('Usuario o correo'), identifier)
  await user.type(screen.getByLabelText('Contraseña'), password)
  await user.click(screen.getByRole('button', { name: 'Ingresar' }))
}

async function fillRegister(user: UserEvent, password: string, confirmPassword = password) {
  await screen.findByRole('heading', { name: 'Crea tu cuenta' })
  await user.type(screen.getByLabelText('Nombre de usuario'), 'nuevo.usuario')
  await user.type(screen.getByLabelText('Correo electrónico'), 'nuevo@pulsepass.test')
  await user.type(screen.getByLabelText('Nombres'), 'Nuevo')
  await user.type(screen.getByLabelText('Apellidos'), 'Usuario')
  fireEvent.change(screen.getByLabelText('Fecha de nacimiento'), { target: { value: '1998-04-12' } })
  await user.type(screen.getByLabelText('Contraseña (mín. 12 caracteres)'), password)
  await user.type(screen.getByLabelText('Confirmar contraseña'), confirmPassword)
}

describe('Autenticación', () => {
  it('TEST-FE-AUTH-001: login válido → sesión autenticada y vuelve a la ruta de origen', async () => {
    const user = userEvent.setup()
    // Anónimo en ruta protegida → /login con state.from = '/mis-tickets'
    const { router } = renderWithProviders({ route: '/mis-tickets' })

    await screen.findByRole('heading', { name: 'Iniciar sesión' })
    await fillLogin(user, USERS.andrea.profile.username, USERS.andrea.password)

    expect(await screen.findByRole('heading', { name: 'Mis tickets' })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/mis-tickets')
  })

  it('TEST-FE-AUTH-002: login 401 → "Credenciales inválidas"', async () => {
    const user = userEvent.setup()
    renderWithProviders({ route: '/login' })

    await fillLogin(user, USERS.andrea.profile.username, 'contraseña-incorrecta')

    expect(await screen.findByText('Credenciales inválidas')).toBeInTheDocument()
  })

  it('TEST-FE-AUTH-003: login sin campos → errores y 0 peticiones', async () => {
    let calls = 0
    server.use(
      http.post('*/api/auth/login', () => {
        calls += 1
        return errorResponse(401, 'Unauthorized')
      }),
    )
    const user = userEvent.setup()
    renderWithProviders({ route: '/login' })

    await user.click(await screen.findByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByText('Ingresa tu usuario o correo')).toBeInTheDocument()
    expect(screen.getByText('Ingresa tu contraseña')).toBeInTheDocument()
    expect(calls).toBe(0)
  })

  it('TEST-FE-AUTH-004: registro con contraseña corta → error y 0 peticiones', async () => {
    let calls = 0
    server.use(
      http.post('*/api/users', () => {
        calls += 1
        return HttpResponse.json({}, { status: 201 })
      }),
    )
    const user = userEvent.setup()
    renderWithProviders({ route: '/registro' })

    await fillRegister(user, 'corta123')
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))

    expect(
      await screen.findByText('La contraseña debe tener al menos 12 caracteres'),
    ).toBeInTheDocument()
    expect(calls).toBe(0)
  })

  it('registro con contraseñas distintas → error y 0 peticiones', async () => {
    let calls = 0
    server.use(
      http.post('*/api/users', () => {
        calls += 1
        return HttpResponse.json({}, { status: 201 })
      }),
    )
    const user = userEvent.setup()
    renderWithProviders({ route: '/registro' })

    await fillRegister(user, 'contraseña-segura-123', 'otra-contraseña-123')
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))

    expect(await screen.findByText('Las contraseñas no coinciden')).toBeInTheDocument()
    expect(calls).toBe(0)
  })

  it('TEST-FE-AUTH-005: registro 201 → redirige a /login con aviso', async () => {
    const user = userEvent.setup()
    const { router } = renderWithProviders({ route: '/registro' })

    await fillRegister(user, 'contraseña-segura-123')
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))

    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(screen.getByText('Cuenta creada')).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/login')
  })

  it('TEST-FE-AUTH-006: registro 409 → mensaje genérico sin detalles', async () => {
    server.use(http.post('*/api/users', () => errorResponse(409, 'Email already registered')))
    const user = userEvent.setup()
    renderWithProviders({ route: '/registro' })

    await fillRegister(user, 'contraseña-segura-123')
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))

    expect(
      await screen.findByText('No pudimos crear la cuenta con esos datos'),
    ).toBeInTheDocument()
    expect(screen.queryByText('Email already registered')).not.toBeInTheDocument()
  })

  it('TEST-FE-AUTH-010: cerrar sesión limpia token y caché', async () => {
    const user = userEvent.setup()
    const { queryClient } = renderWithProviders({ route: '/cuenta', as: 'andrea' })

    await screen.findByRole('heading', { name: 'Mi cuenta' })
    expect(sessionStorage.getItem('pulsepass.session')).not.toBeNull()

    await user.click(screen.getByRole('button', { name: 'Cerrar sesión' }))

    await waitFor(() => {
      expect(sessionStorage.getItem('pulsepass.session')).toBeNull()
      expect(queryClient.getQueryCache().getAll()).toHaveLength(0)
    })
  })

  it('TEST-FE-AUTH-011: token expirado al restaurar la sesión → anónimo', async () => {
    sessionStorage.setItem(
      'pulsepass.session',
      JSON.stringify({ token: USERS.andrea.token, expiresAt: Date.now() - 1000 }),
    )
    renderWithProviders({ route: '/mis-tickets' })

    // Sin sesión válida, la ruta protegida manda a /login
    expect(await screen.findByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument()
  })
})