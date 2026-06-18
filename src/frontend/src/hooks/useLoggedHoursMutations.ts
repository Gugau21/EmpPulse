import { useMutation, useQueryClient } from '@tanstack/react-query'
import { loggedHoursService, type LoggedHoursPayload } from '../services/api'
import { loggedHoursKeys } from '../lib/queryKeys'

// All three mutations invalidate the whole loggedHours tree so the profile table
// refetches — necessary because the server merges overlapping/adjacent intervals,
// so the stored result can differ from what was submitted.

// Logs a new interval for the employee (admin only); see loggedHoursService.create.
export function useCreateLoggedHours() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { employeeId: number; payload: LoggedHoursPayload }) =>
      loggedHoursService.create(vars.employeeId, vars.payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: loggedHoursKeys.all })
  })
}

// Updates an interval's times in place (admin only); see loggedHoursService.update.
export function useUpdateLoggedHours() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: {
      employeeId: number
      loggedHoursId: number
      payload: LoggedHoursPayload
    }) => loggedHoursService.update(vars.employeeId, vars.loggedHoursId, vars.payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: loggedHoursKeys.all })
  })
}

// Removes an interval (admin only); see loggedHoursService.delete.
export function useDeleteLoggedHours() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { employeeId: number; loggedHoursId: number }) =>
      loggedHoursService.delete(vars.employeeId, vars.loggedHoursId),
    onSuccess: () => qc.invalidateQueries({ queryKey: loggedHoursKeys.all })
  })
}
