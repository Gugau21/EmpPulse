import React, { useCallback, useEffect, useMemo } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { authService } from '../services/api'
import { applyLanguage, applyTheme, languageFromApi, themeFromApi } from '../utils/preferences'
import { AuthContext, type AuthContextValue } from './authContextValue'

// The /api/me query cache is the single source of truth for the session: the
// on-mount probe restores it, and login/logout write to it via setQueryData. A
// `null` value means "known signed-out"; `undefined` (pending) means "not yet
// determined" — which is what `isBootstrapping` reflects.
const ME_KEY = ['me'] as const

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const qc = useQueryClient()

  // One-shot session restore on mount: GET /api/me re-hydrates the session when a
  // cookie is still valid (null on 401 = no session). `isBootstrapping` stays true
  // until this settles so the auth gate doesn't flash to /login on a refresh.
  // Disabled retry: a single attempt is enough to decide signed-in or not.
  const meQuery = useQuery({
    queryKey: ME_KEY,
    queryFn: ({ signal }) => authService.me(signal),
    retry: false,
    staleTime: Infinity
  })

  const currentUser = meQuery.data ?? null
  const isBootstrapping = meQuery.isPending

  // Make the server the source of truth for theme/language: whenever the session
  // resolves (login or on-refresh restore), apply the user's saved preferences
  // over the localStorage cache that gave the first paint. This way a choice
  // follows the user to a new browser, and a different user signing in on a
  // shared browser doesn't inherit the previous one. Runs once per session
  // because currentUser only changes identity on login/restore/logout (a null
  // user — signed out — is left untouched so the next login can re-hydrate).
  useEffect(() => {
    const prefs = currentUser?.preferences
    if (!prefs) return
    applyTheme(themeFromApi(prefs.theme))
    applyLanguage(languageFromApi(prefs.language))
  }, [currentUser])

  const loginMutation = useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      authService.login(email, password),
    onSuccess: user => qc.setQueryData(ME_KEY, user)
  })

  const logoutMutation = useMutation({
    mutationFn: () => authService.logout(),
    // onSettled (not onSuccess) matches the old `finally` semantics: clear the
    // session and wipe all cached server data even if logout fails, so a
    // different user signing in next never sees stale departments/admins. Reset
    // the /api/me cache to null *after* clear() so the signed-out state sticks.
    onSettled: () => {
      qc.clear()
      qc.setQueryData(ME_KEY, null)
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
      isBootstrapping,
      isOwner: currentUser?.owner ?? false,
      isAdmin: currentUser?.adminProfile != null,
      isEmployee: currentUser?.employeeProfile != null,
      login,
      logout,
      loginPending: loginMutation.isPending,
      loginError: loginMutation.error
    }),
    [currentUser, isBootstrapping, login, logout, loginMutation.isPending, loginMutation.error]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
