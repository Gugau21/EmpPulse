import { createContext } from 'react'
import type { MeUser, UserRole } from '../types'

// The auth context object and its value type live here (not in AuthContext.tsx)
// so that file can export only the provider component — required by the
// react-refresh/only-export-components lint rule (Fast Refresh).
export interface AuthContextValue {
  currentUser: MeUser | null
  userRole: UserRole | null
  login: (email: string, password: string) => Promise<MeUser>
  logout: () => Promise<void>
  loginPending: boolean
  loginError: Error | null
}

export const AuthContext = createContext<AuthContextValue | null>(null)
