import { useMutation, useQueryClient } from '@tanstack/react-query'
import { userService } from '../services/api'
import { employeeKeys } from '../lib/queryKeys'

export function useDeleteEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (userId: number) => userService.delete(userId),
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.lists() })
  })
}
