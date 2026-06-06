import React, { useCallback, useMemo, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { MeUser } from '../types'
import { deriveRole } from '../types'
import { authService } from '../services/api'
import { AuthContext, type AuthContextValue } from './authContextValue'

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<MeUser | null>(null)
  const qc = useQueryClient()

  const loginMutation = useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      authService.login(email, password),
    onSuccess: user => setCurrentUser(user)
  })

  const logoutMutation = useMutation({
    mutationFn: () => authService.logout(),
    // onSettled (not onSuccess) matches the old `finally` semantics: clear the
    // local session and wipe all cached server data even if logout fails, so a
    // different user signing in next never sees stale departments/admins.
    onSettled: () => {
      setCurrentUser(null)
      qc.clear()
    }
  })

  const login = useCallback(
    (email: string, password: string) => loginMutation.mutateAsync({ email, password }),
    [loginMutation]
  )

  const logout = useCallback(() => logoutMutation.mutateAsync(), [logoutMutation])

  const value = useMemo<AuthContextValue>(
    () => ({
      currentUser,
      userRole: currentUser ? deriveRole(currentUser) : null,
      login,
      logout,
      loginPending: loginMutation.isPending,
      loginError: loginMutation.error
    }),
    [currentUser, login, logout, loginMutation.isPending, loginMutation.error]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
