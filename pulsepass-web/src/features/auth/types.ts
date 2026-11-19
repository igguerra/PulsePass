import { z } from 'zod'

export const RoleSchema = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Role = z.infer<typeof RoleSchema>

// Formulario de login (= LoginRequest)
export const LoginSchema = z.object({
  identifier: z.string().trim().min(1, 'Ingresa tu usuario o correo'),
  password: z.string().min(1, 'Ingresa tu contraseña'),
})
export type LoginRequest = z.infer<typeof LoginSchema>

// Respuesta de POST /api/auth/login
export const AuthResponseSchema = z.object({
  accessToken: z.string().min(1),
  tokenType: z.literal('Bearer'),
  expiresIn: z.number().positive(), // segundos (900)
})
export type AuthResponse = z.infer<typeof AuthResponseSchema>

// Respuesta de GET /api/auth/me. OJO: el backend NO devuelve email.
export const AuthenticatedUserResponseSchema = z.object({
  id: z.number().int(),
  username: z.string(),
  roles: z.array(RoleSchema),
})
export type AuthenticatedUserResponse = z.infer<typeof AuthenticatedUserResponseSchema>

// Lo que guarda la sesión: /me + email y nombre sacados de GET /api/users/by-username
export interface AuthenticatedUser extends AuthenticatedUserResponse {
  email: string
  firstName: string
}

// Estado de sesión como unión discriminada (FE-006)
export type Session =
  | { status: 'loading' }
  | { status: 'anonymous'; reason?: 'expired' }
  | { status: 'authenticated'; token: string; expiresAt: number; user: AuthenticatedUser }