import { createContext } from 'react'
import type { Role, Session } from './types'

export interface AuthValue {
  session: Session
  signIn: (identifier: string, password: string) => Promise<void>
  signOut: () => void
  hasRole: (...roles: Role[]) => boolean
}

export const AuthContext = createContext<AuthValue | null>(null)