import { useMutation, useQueryClient } from '@tanstack/react-query'
import { leaveRequestService, type LeaveRequestUpdatePayload } from '../services/api'
import { leaveRequestKeys } from '../lib/queryKeys'

export function useUpdateLeaveRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { id: number; payload: LeaveRequestUpdatePayload }) =>
      leaveRequestService.update(vars.id, vars.payload),
    // No leave-request query exists yet (the list page still renders mock data),
    // so this is a no-op today — but it keeps the cache honest once one is wired.
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveRequestKeys.all })
  })
}
