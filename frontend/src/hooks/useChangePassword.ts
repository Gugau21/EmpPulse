import { useMutation } from '@tanstack/react-query'
import { authService, type PasswordChangePayload } from '../services/api'

// Self-service password change for the signed-in user. Nothing in the query cache
// depends on the password, so there's nothing to invalidate on success; the
// caller shows a confirmation and the (still-valid) session carries on unchanged.
export function useChangePassword() {
  return useMutation({
    mutationFn: (payload: PasswordChangePayload) => authService.changePassword(payload)
  })
}
