import { ApiError } from '../../lib/http'

// 403 (ticket ajeno) y 404 (no existe) se tratan igual: "Ticket no disponible"
export function isUnavailableError(error: unknown): boolean {
  return error instanceof ApiError && (error.status === 403 || error.status === 404)
}