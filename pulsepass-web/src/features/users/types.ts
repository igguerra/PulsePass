import { z } from 'zod'
import { DateSchema } from '../../lib/schemas'

// Espejo de UserResponse (User + UserProfile, sin hash de contraseña)
export const UserResponseSchema = z.object({
  id: z.number().int(),
  username: z.string(),
  email: z.string(),
  active: z.boolean(),
  firstName: z.string(),
  lastName: z.string(),
  phone: z.string().nullable(),
  city: z.string().nullable(),
  birthDate: DateSchema,
})
export type UserResponse = z.infer<typeof UserResponseSchema>

// Formulario de registro (= RegisterUserRequest). Reglas de la sección 11.
export const RegisterUserRequestSchema = z.object({
  username: z.string().trim().min(1, 'Ingresa un nombre de usuario'),
  email: z.email('Ingresa un correo válido'),
  firstName: z.string().trim().min(1, 'Ingresa tus nombres'),
  lastName: z.string().trim().min(1, 'Ingresa tus apellidos'),
  birthDate: DateSchema.refine((date) => date <= new Date().toISOString().slice(0, 10), {
    message: 'La fecha no puede ser futura',
  }),
  password: z
    .string()
    .min(12, 'La contraseña debe tener al menos 12 caracteres')
    // BCrypt solo usa 72 BYTES: una ñ o una tilde ocupan 2 bytes
    .refine((p) => new TextEncoder().encode(p).length <= 72, 'La contraseña es demasiado larga'),
})
export type RegisterUserRequest = z.infer<typeof RegisterUserRequestSchema>