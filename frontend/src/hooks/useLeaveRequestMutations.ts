import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  leaveRequestService,
  type LeaveRequestCreatePayload,
  type LeaveRequestUpdatePayload,
  type LeaveResponsePayload
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

// Deletes a PENDING request outright (the row disappears); see leaveRequestService.delete.
export function useDeleteLeaveRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => leaveRequestService.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveRequestKeys.all })
  })
}

// Approves or rejects a PENDING request, admin only (it moves to APPROVED/REJECTED);
// see leaveRequestService.respond.
export function useRespondToLeaveRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { id: number; payload: LeaveResponsePayload }) =>
      leaveRequestService.respond(vars.id, vars.payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveRequestKeys.all })
  })
}

// Cancels an APPROVED request (it moves to CANCELLED); see leaveRequestService.cancel.
export function useCancelLeaveRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => leaveRequestService.cancel(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveRequestKeys.all })
  })
}
