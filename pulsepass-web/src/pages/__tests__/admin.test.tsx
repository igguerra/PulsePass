import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { cmfEvent } from '../../test/mocks/data'
import { errorResponse } from '../../test/mocks/handlers'
import { server } from '../../test/mocks/server'
import { renderWithProviders } from '../../test/utils'

async function fillValidForm(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Código del evento'), 'NEW-2026')
  await user.type(screen.getByLabelText('Nombre'), 'Festival Nuevo')
  await user.selectOptions(screen.getByLabelText('Categoría'), 'CULTURE')
  await user.type(screen.getByLabelText('Fecha y hora'), '2026-12-20T19:00')
  await screen.findByRole('option', { name: 'Marina Convention Center (VEN-SMR-01)' })
  await user.selectOptions(screen.getByLabelText('Sede'), 'VEN-SMR-01')
}

describe('ADMIN · crear evento', () => {
  it('TEST-FE-ADM-001: campos vacíos → errores y 0 peticiones', async () => {
    const user = userEvent.setup()
    let posts = 0
    server.use(
      http.post('*/api/events', () => {
        posts += 1
        return HttpResponse.json({})
      }),
    )
    renderWithProviders({ route: '/admin/eventos/nuevo', as: 'admin' })
    await user.click(await screen.findByRole('button', { name: 'Crear evento' }))
    expect(await screen.findByText('Ingresa el código del evento')).toBeInTheDocument()
    expect(screen.getByText('Ingresa el nombre')).toBeInTheDocument()
    expect(screen.getByText('Selecciona una categoría')).toBeInTheDocument()
    expect(screen.getByText('Selecciona la fecha y hora')).toBeInTheDocument()
    expect(screen.getByText('Selecciona una sede')).toBeInTheDocument()
    expect(posts).toBe(0)
  })

  it('TEST-FE-ADM-002: 201 → navega a gestión y queda en "Creados en esta sesión"', async () => {
    const user = userEvent.setup()
    let body: unknown
    server.use(
      http.post('*/api/events', async ({ request }) => {
        body = await request.json()
        return HttpResponse.json({ ...cmfEvent, eventCode: 'NEW-2026', name: 'Festival Nuevo', status: 'DRAFT', artists: [] }, { status: 201 })
      }),
    )
    const { router } = renderWithProviders({ route: '/admin/eventos/nuevo', as: 'admin' })
    await screen.findByRole('button', { name: 'Crear evento' })
    await fillValidForm(user)
    await user.click(screen.getByRole('button', { name: 'Crear evento' }))

    expect(await screen.findByRole('heading', { name: /Festival Nuevo/ })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/admin/eventos/NEW-2026')
    expect(body).toMatchObject({ eventCode: 'NEW-2026', category: 'CULTURE', minimumAge: 0, venueCode: 'VEN-SMR-01' })

    await user.click(screen.getByRole('link', { name: '← Eventos' }))
    const created = await screen.findByRole('region', { name: 'Creados en esta sesión' })
    expect(within(created).getByText(/Festival Nuevo/)).toBeInTheDocument()
  })

  it('TEST-FE-ADM-003: 400 con details → errores en los campos', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('*/api/events', () =>
        errorResponse(400, 'Validation failed', { eventCode: 'El código ya tiene un formato inválido' }),
      ),
    )
    renderWithProviders({ route: '/admin/eventos/nuevo', as: 'admin' })
    await screen.findByRole('button', { name: 'Crear evento' })
    await fillValidForm(user)
    await user.click(screen.getByRole('button', { name: 'Crear evento' }))
    expect(await screen.findByText('El código ya tiene un formato inválido')).toBeInTheDocument()
    expect(screen.getByLabelText('Código del evento')).toHaveAttribute('aria-invalid', 'true')
  })

  it('409 (código duplicado) → aviso arriba del formulario', async () => {
    const user = userEvent.setup()
    server.use(http.post('*/api/events', () => errorResponse(409, 'Event code already exists')))
    renderWithProviders({ route: '/admin/eventos/nuevo', as: 'admin' })
    await screen.findByRole('button', { name: 'Crear evento' })
    await fillValidForm(user)
    await user.click(screen.getByRole('button', { name: 'Crear evento' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Event code already exists')
  })
})

describe('ADMIN · gestionar evento', () => {
  const draft = { ...cmfEvent, status: 'DRAFT' as const }

  it('TEST-FE-ADM-004: publicar evento → badge Publicado', async () => {
    const user = userEvent.setup()
    server.use(http.get('*/api/events/:eventCode', () => HttpResponse.json(draft)))
    renderWithProviders({ route: '/admin/eventos/CMF-2026', as: 'admin' })
    expect(await screen.findByText('Borrador')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Publicar evento' }))
    await user.click(within(screen.getByRole('dialog', { name: '¿Publicar evento?' })).getByRole('button', { name: 'Publicar' }))

    expect(await screen.findByText('Publicado')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Publicar evento' })).toBeDisabled()
  })

  it('TEST-FE-ADM-005: publicar → 409 visible', async () => {
    const user = userEvent.setup()
    server.use(
      http.get('*/api/events/:eventCode', () => HttpResponse.json(draft)),
      http.patch('*/api/events/:eventCode/publish', () => errorResponse(409, 'Event date must be in the future')),
    )
    renderWithProviders({ route: '/admin/eventos/CMF-2026', as: 'admin' })
    await user.click(await screen.findByRole('button', { name: 'Publicar evento' }))
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Publicar' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Event date must be in the future')
  })

  it('TEST-FE-ADM-006: asociar artista → aparece en la lista; los ya asociados están bloqueados', async () => {
    const user = userEvent.setup()
    renderWithProviders({ route: '/admin/eventos/CMF-2026', as: 'admin' })
    const select = await screen.findByLabelText('Agregar artista')
    expect(await screen.findByRole('option', { name: 'Solar Beat (ya asociado)' })).toBeDisabled()

    await user.selectOptions(select, '3')
    await user.click(screen.getByRole('button', { name: 'Asociar' }))
    const list = screen.getByRole('region', { name: 'Artistas asociados' })
    expect(await within(list).findByText('Caribbean Sound')).toBeInTheDocument()
  })

  it('TEST-FE-ADM-006: asociar artista duplicado → 409 visible', async () => {
    const user = userEvent.setup()
    server.use(
      http.get('*/api/events/:eventCode', () => HttpResponse.json({ ...cmfEvent, artists: [] })),
      http.post('*/api/events/:eventCode/artists/:artistId', () => errorResponse(409, 'Artist already associated')),
    )
    renderWithProviders({ route: '/admin/eventos/CMF-2026', as: 'admin' })
    await screen.findByRole('option', { name: 'Solar Beat' })
    await user.selectOptions(screen.getByLabelText('Agregar artista'), '1')
    await user.click(screen.getByRole('button', { name: 'Asociar' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Artist already associated')
  })

  it('USER no puede entrar a /admin → 403', async () => {
    renderWithProviders({ route: '/admin/eventos/nuevo', as: 'andrea' })
    expect(await screen.findByText('No tienes permisos para acceder a esta sección.')).toBeInTheDocument()
  })
})
