import { z } from 'zod'

// Contrato uniforme de errores del backend (ErrorResponse de la Fase 3)
const ErrorResponseSchema = z.object({
  timestamp: z.string(),
  status: z.number(),
  error: z.string(),
  message: z.string(),
  details: z.record(z.string(), z.string()).nullish(),
})

// status 0 = no hubo respuesta (sin red / API caída)
export class ApiError extends Error {
  readonly status: number
  readonly details: Record<string, string>

  constructor(status: number, message: string, details: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

// --- Conexión con la sesión (la configura AuthProvider) ---
let currentToken: string | null = null
let unauthorizedHandler: (() => void) | null = null

export function setToken(token: string | null): void {
  currentToken = token
}

export function registerUnauthorizedHandler(handler: () => void): () => void {
  unauthorizedHandler = handler
  return () => {
    if (unauthorizedHandler === handler) unauthorizedHandler = null
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH'
  body?: unknown
  signal?: AbortSignal
  /** Token explícito (login → /me, antes de que exista la sesión). */
  token?: string
  /** En el login un 401 significa "credenciales inválidas", no "sesión expirada". */
  skipSignOutOn401?: boolean
}

export async function http<T>(
  path: string,
  schema: z.ZodType<T>,
  options: RequestOptions = {},
): Promise<T> {
  const { method = 'GET', body, signal, skipSignOutOn401 = false } = options
  const token = options.token ?? currentToken

  const headers = new Headers({ Accept: 'application/json' })
  if (body !== undefined) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const url = new URL(`${BASE_URL}${path}`, window.location.origin)

  let response: Response
  try {
    response = await fetch(url, {
      method,
      headers,
      body: body === undefined ? null : JSON.stringify(body),
      signal: signal ?? null,
    })
  } catch (error: unknown) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError(0, 'No pudimos conectar con el servidor.')
  }

  const data = await readJson(response)

  if (!response.ok) {
    if (response.status === 401 && !skipSignOutOn401) unauthorizedHandler?.()
    const error = ErrorResponseSchema.safeParse(data)
    if (error.success) {
      throw new ApiError(response.status, error.data.message, error.data.details ?? {})
    }
    throw new ApiError(response.status, 'Ocurrió un error inesperado. Inténtalo más tarde.')
  }

  const result = schema.safeParse(data)
  if (!result.success) {
    throw new ApiError(500, 'La respuesta del servidor no tiene el formato esperado.')
  }
  return result.data
}

async function readJson(response: Response): Promise<unknown> {
  const text = await response.text()
  if (!text) return undefined
  try {
    return JSON.parse(text)
  } catch {
    return undefined
  }
}

// Mapeo HTTP → mensaje de interfaz (sección 13). Siempre se pinta como texto, nunca HTML.
export function getErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) return 'Ocurrió un error inesperado. Inténtalo más tarde.'
  switch (error.status) {
    case 0:
      return 'No pudimos conectar con el servidor.'
    case 400:
    case 409:
      return error.message
    case 401:
      return 'Tu sesión expiró. Inicia sesión de nuevo.'
    case 403:
      return 'No tienes permisos para realizar esta acción.'
    case 404:
      return 'No encontramos lo que buscas.'
    default:
      return 'Ocurrió un error inesperado. Inténtalo más tarde.'
  }
}