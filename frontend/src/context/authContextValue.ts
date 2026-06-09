import { createContext } from 'react'
import type { MeUser } from '../types'

export interface AuthContextValue {
  currentUser: MeUser | null
  isOwner: boolean
  isAdmin: boolean
  isEmployee: boolean
  login: (email: string, password: string) => Promise<MeUser>
  logout: () => Promise<void>
  loginPending: boolean
  loginError: Error | null
}

export const AuthContext = createContext<AuthContextValue | null>(null)
