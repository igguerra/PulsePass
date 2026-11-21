import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError } from '../../lib/http'

/** Si el error es un 400 con details, asigna cada clave a su campo. Devuelve true si lo manejó. */
export function applyFieldErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
): boolean {
  if (!(error instanceof ApiError) || error.status !== 400) return false
  const entries = Object.entries(error.details)
  if (entries.length === 0) return false
  for (const [key, message] of entries) {
    setError(key as Path<T>, { message })
  }
  return true
}