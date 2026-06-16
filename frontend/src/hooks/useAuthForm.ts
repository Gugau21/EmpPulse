import React, { useState } from 'react'

// Shared submit plumbing for the auth pages (forgot/reset password). Owns the
// `error`/`loading` state and wraps an async action with the common
// preventDefault → clear-error → toggle-loading → try/catch/finally flow.
// Client-side validation lives in `action` and signals failure by throwing an
// Error; its message is surfaced. `fallbackError` covers non-Error throws.
export function useAuthForm(fallbackError: string) {
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = (action: () => Promise<void>) => async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await action()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : fallbackError)
    } finally {
      setLoading(false)
    }
  }

  return { error, loading, handleSubmit }
}
