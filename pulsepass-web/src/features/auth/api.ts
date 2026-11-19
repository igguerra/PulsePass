import { http } from '../../lib/http'
import { getUserByUsername } from '../users/api'
import {
  AuthenticatedUserResponseSchema,
  AuthResponseSchema,
  type AuthenticatedUser,
  type AuthResponse,
  type LoginRequest,
} from './types'

export function login(data: LoginRequest): Promise<AuthResponse> {
  return http('/auth/login', AuthResponseSchema, {
    method: 'POST',
    body: data,
    skipSignOutOn401: true, // aquí 401 = "Credenciales inválidas"
  })
}

// GAP-03: /me no trae email, así que se completa con GET /api/users/by-username
// (el backend permite que cada usuario consulte su propio perfil).
export async function loadAuthenticatedUser(token: string): Promise<AuthenticatedUser> {
  const me = await http('/auth/me', AuthenticatedUserResponseSchema, { token })
  const profile = await getUserByUsername(me.username, token)
  return { ...me, email: profile.email, firstName: profile.firstName }
}