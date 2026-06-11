import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  leaveRequestService,
  type LeaveRequestCreatePayload,
  type LeaveRequestUpdatePayload
} from '../services/api'
import { leaveRequestKeys } from '../lib/queryKeys'

export function useCreateLeaveRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (payload: LeaveRequestCreatePayload) => leaveRequestService.create(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveRequestKeys.all })
  })
}

export function useUpdateLeaveRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { id: number; payload: LeaveRequestUpdatePayload }) =>
      leaveRequestService.update(vars.id, vars.payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveRequestKeys.all })
  })
}
