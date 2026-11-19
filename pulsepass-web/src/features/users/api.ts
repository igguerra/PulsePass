import { http } from '../../lib/http'
import { UserResponseSchema, type UserResponse } from './types'

// La base solo trae esta función porque AuthContext la necesita para conocer el email.
// Quien haga Register/Account agrega aquí registerUser y getUserByEmail.
export function getUserByUsername(username: string, token?: string): Promise<UserResponse> {
  const query = new URLSearchParams({ username })
  return http(`/users/by-username?${query}`, UserResponseSchema, token ? { token } : {})
}