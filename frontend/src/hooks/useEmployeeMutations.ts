import { useMutation, useQueryClient } from '@tanstack/react-query'
import { userService, type UserUpdatePayload } from '../services/api'
import { employeeKeys, departmentKeys, adminKeys, userKeys } from '../lib/queryKeys'

export function useDeleteEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (userId: number) => userService.delete(userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.lists() })
  })
}

// Editing a user can change their name (employee list), department membership and
// admin assignments, so invalidate those caches plus this user's detail.
export function useUpdateUser() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, payload }: { userId: number; payload: UserUpdatePayload }) =>
      userService.update(userId, payload),
    onSuccess: (_data, { userId }) => {
      qc.invalidateQueries({ queryKey: employeeKeys.lists() })
      qc.invalidateQueries({ queryKey: departmentKeys.all })
      qc.invalidateQueries({ queryKey: adminKeys.lists() })
      qc.invalidateQueries({ queryKey: userKeys.detail(userId) })
    }
  })
}
