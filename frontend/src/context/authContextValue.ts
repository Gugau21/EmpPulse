import { createContext } from 'react'
import type { MeUser } from '../types'

export interface AuthContextValue {
  currentUser: MeUser | null
  // True while the on-mount GET /api/me session-restore query is in flight. The
  // auth gate waits on this so a refresh never flash-redirects to /login.
  isBootstrapping: boolean
  isOwner: boolean
  isAdmin: boolean
  isEmployee: boolean
  login: (email: string, password: string) => Promise<MeUser>
  logout: () => Promise<void>
  loginPending: boolean
  loginError: Error | null
}

export const AuthContext = createContext<AuthContextValue | null>(null)
