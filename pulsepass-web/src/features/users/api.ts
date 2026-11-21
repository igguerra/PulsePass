import { http } from '../../lib/http'
import {
  UserResponseSchema,
  type RegisterUserRequest,
  type UserResponse,
} from './types'

// AuthContext necesita esta función para conocer el email. No borrar.
export function getUserByUsername(username: string, token?: string): Promise<UserResponse> {
  const query = new URLSearchParams({ username })
  return http(`/users/by-username?${query}`, UserResponseSchema, token ? { token } : {})
}

export function registerUser(body: RegisterUserRequest): Promise<UserResponse> {
  return http('/users', UserResponseSchema, { method: 'POST', body })
}

export function getUserByEmail(email: string): Promise<UserResponse> {
  const query = new URLSearchParams({ email })
  return http(`/users/by-email?${query}`, UserResponseSchema)
}