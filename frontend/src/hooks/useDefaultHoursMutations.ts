import { useMutation, useQueryClient } from '@tanstack/react-query'
import { defaultHoursService, type DefaultHoursDay } from '../services/api'
import { defaultHoursKeys } from '../lib/queryKeys'

// Replaces an employee's weekly default hours (admin only); see
// defaultHoursService.setForEmployee. Invalidates that employee's cached schedule
// so the editor and any profile display refetch the saved result.
export function useSetEmployeeDefaultHours() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { employeeId: number; days: DefaultHoursDay[] }) =>
      defaultHoursService.setForEmployee(vars.employeeId, vars.days),
    onSuccess: (_data, vars) =>
      qc.invalidateQueries({ queryKey: defaultHoursKeys.employee(vars.employeeId) })
  })
}

// Replaces a department's weekly default hours (admin only); see
// defaultHoursService.setForDepartment.
export function useSetDepartmentDefaultHours() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { departmentId: number; days: DefaultHoursDay[] }) =>
      defaultHoursService.setForDepartment(vars.departmentId, vars.days),
    onSuccess: (_data, vars) =>
      qc.invalidateQueries({ queryKey: defaultHoursKeys.department(vars.departmentId) })
  })
}
