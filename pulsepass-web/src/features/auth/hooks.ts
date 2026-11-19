import { use } from 'react'
import { AuthContext, type AuthValue } from './context'

export function useAuth(): AuthValue {
  const value = use(AuthContext)
  if (!value) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return value
}