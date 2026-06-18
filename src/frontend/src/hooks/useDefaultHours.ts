import { useQuery } from '@tanstack/react-query'
import { defaultHoursService } from '../services/api'
import { defaultHoursKeys } from '../lib/queryKeys'
import { useAuth } from '../context/useAuth'

// An employee's weekly default working hours (GET /api/employees/{id}/default-hours).
// The endpoint is admin-only, so the query stays idle for non-admins; callers also
// pass null while the target id is unresolved. Returns [] when none have been set.
export function useEmployeeDefaultHours(employeeId: number | null) {
  const { isOwner, isAdmin } = useAuth()
  return useQuery({
    queryKey: defaultHoursKeys.employee(employeeId ?? -1),
    queryFn: ({ signal }) => defaultHoursService.getForEmployee(employeeId as number, signal),
    enabled: employeeId != null && (isOwner || isAdmin)
  })
}

// A department's weekly default working hours (GET /api/departments/{id}/default-hours).
// Same access rules as the employee variant. Returns [] when none have been set.
export function useDepartmentDefaultHours(departmentId: number | null) {
  const { isOwner, isAdmin } = useAuth()
  return useQuery({
    queryKey: defaultHoursKeys.department(departmentId ?? -1),
    queryFn: ({ signal }) => defaultHoursService.getForDepartment(departmentId as number, signal),
    enabled: departmentId != null && (isOwner || isAdmin)
  })
}
