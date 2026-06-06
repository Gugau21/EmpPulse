import { useMutation, useQueryClient } from '@tanstack/react-query'
import { userService, type UserCreatePayload } from '../services/api'
import { employeeKeys, departmentKeys, adminKeys } from '../lib/queryKeys'

// User creation. A new user can be an employee and/or an admin, so invalidate
// the employee list (where they appear) plus departments/admins (membership and
// admin counts change) to force a refetch instead of showing the stale cache.
export function useCreateUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: UserCreatePayload) => userService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: employeeKeys.lists() })
      queryClient.invalidateQueries({ queryKey: departmentKeys.all })
      queryClient.invalidateQueries({ queryKey: adminKeys.lists() })
    }
  })
}
