import { http as mswHttp, HttpResponse } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { z } from 'zod'
import { errorResponse } from '../../test/mocks/handlers'
import { server } from '../../test/mocks/server'
import { formatCOP } from '../format'
import { ApiError, http, registerUnauthorizedHandler, setToken } from '../http'

const Anything = z.unknown()

describe('lib/http', () => {
  it('TEST-FE-LIB-001: añade Authorization Bearer cuando hay sesión', async () => {
    let header: string | null = null
    server.use(
      mswHttp.get('*/api/test', ({ request }) => {
        header = request.headers.get('Authorization')
        return HttpResponse.json({})
      }),
    )
    setToken('abc.def.ghi')
    await http('/test', Anything)
    expect(header).toBe('Bearer abc.def.ghi')
  })

  it('TEST-FE-LIB-002: ErrorResponse válido → ApiError con status y details', async () => {
    server.use(mswHttp.get('*/api/test', () => errorResponse(400, 'Validation failed', { name: 'Name is required' })))
    const error = await http('/test', Anything).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 400, message: 'Validation failed', details: { name: 'Name is required' } })
  })

  it('TEST-FE-LIB-003: cuerpo de error inválido → ApiError genérico', async () => {
    server.use(mswHttp.get('*/api/test', () => HttpResponse.text('<html>boom</html>', { status: 500 })))
    const error = await http('/test', Anything).catch((e: unknown) => e)
    expect(error).toMatchObject({ status: 500, message: 'Ocurrió un error inesperado. Inténtalo más tarde.' })
  })

  it('TEST-FE-LIB-004: 401 → dispara cierre de sesión', async () => {
    const handler = vi.fn()
    const unregister = registerUnauthorizedHandler(handler)
    server.use(mswHttp.get('*/api/test', () => errorResponse(401, 'Unauthorized')))
    await http('/test', Anything).catch(() => undefined)
    expect(handler).toHaveBeenCalledOnce()
    unregister()
  })

  it('red caída → ApiError status 0', async () => {
    server.use(mswHttp.get('*/api/test', () => HttpResponse.error()))
    const error = await http('/test', Anything).catch((e: unknown) => e)
    expect(error).toMatchObject({ status: 0, message: 'No pudimos conectar con el servidor.' })
  })

  it('TEST-FE-LIB-005: formatea 250000 como COP', () => {
    // Intl usa espacio duro (U+00A0) entre $ y el número
    expect(formatCOP(250000).replace(/\s/g, ' ')).toBe('$ 250.000')
  })
})