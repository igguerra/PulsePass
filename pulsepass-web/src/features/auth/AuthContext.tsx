import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { z } from 'zod'
import { registerUnauthorizedHandler, setToken } from '../../lib/http'
import { loadAuthenticatedUser, login } from './api'
import { AuthContext, type AuthValue } from './context'
import type { Role, Session } from './types'

const STORAGE_KEY = 'pulsepass.session'

// Lo guardado en sessionStorage es dato externo → se valida (FE-002)
const StoredTokenSchema = z.object({ token: z.string().min(1), expiresAt: z.number() })
type StoredToken = z.infer<typeof StoredTokenSchema>

function readStoredToken(): StoredToken | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const result = StoredTokenSchema.safeParse(JSON.parse(raw))
    if (result.success && result.data.expiresAt > Date.now()) return result.data
  } catch {
    // JSON corrupto: se descarta abajo
  }
  sessionStorage.removeItem(STORAGE_KEY)
  return null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()

  // Se lee UNA vez al montar. Sin token arrancamos ya como anónimos (sin parpadeo de "loading")
  const [initialToken] = useState(readStoredToken)
  const [session, setSession] = useState<Session>(() =>
    initialToken ? { status: 'loading' } : { status: 'anonymous' },
  )

  const clear = useCallback(() => {
    sessionStorage.removeItem(STORAGE_KEY)
    setToken(null)
    queryClient.clear() // FR-FE-AUTH-006: borra la caché del usuario anterior
  }, [queryClient])

  const signOut = useCallback(() => {
    clear()
    setSession({ status: 'anonymous' })
  }, [clear])

  const expireSession = useCallback(() => {
    clear()
    setSession((previous) =>
      previous.status === 'authenticated' ? { status: 'anonymous', reason: 'expired' } : { status: 'anonymous' },
    )
  }, [clear])

  // 1) Restaurar sesión al recargar la pestaña (FR-FE-AUTH-004)
  useEffect(() => {
    if (!initialToken) return
    let active = true // evita actualizar estado si el componente se desmontó
    setToken(initialToken.token)
    loadAuthenticatedUser(initialToken.token)
      .then((user) => {
        if (active) setSession({ status: 'authenticated', ...initialToken, user })
      })
      .catch(() => {
        if (active) signOut()
      })
    return () => {
      active = false
    }
  }, [initialToken, signOut])

  // 2) Cualquier 401 de la API cierra la sesión local (GAP-08)
  useEffect(() => registerUnauthorizedHandler(expireSession), [expireSession])

  // 3) Cierre programado cuando vence el token (15 min)
  useEffect(() => {
    if (session.status !== 'authenticated') return
    const timer = setTimeout(expireSession, session.expiresAt - Date.now())
    return () => clearTimeout(timer)
  }, [session, expireSession])

  const signIn = useCallback(async (identifier: string, password: string) => {
    const { accessToken, expiresIn } = await login({ identifier, password })
    const expiresAt = Date.now() + expiresIn * 1000
    const user = await loadAuthenticatedUser(accessToken)
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ token: accessToken, expiresAt }))
    setToken(accessToken)
    setSession({ status: 'authenticated', token: accessToken, expiresAt, user })
  }, [])

  const hasRole = useCallback(
    (...roles: Role[]) => session.status === 'authenticated' && roles.some((role) => session.user.roles.includes(role)),
    [session],
  )

  const value = useMemo<AuthValue>(
    () => ({ session, signIn, signOut, hasRole }),
    [session, signIn, signOut, hasRole],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}