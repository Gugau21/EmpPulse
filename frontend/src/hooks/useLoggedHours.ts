import { useQuery } from '@tanstack/react-query'
import { loggedHoursService } from '../services/api'
import { loggedHoursKeys } from '../lib/queryKeys'
import { useAuth } from '../context/useAuth'

// The logged working intervals for one employee (GET /api/employees/{id}/logged-hours),
// scoped server-side: the employee themselves, or an admin overseeing them. The
// caller passes null while the target id is unresolved, which keeps the query idle.
export function useLoggedHours(employeeId: number | null) {
  const { currentUser } = useAuth()
  return useQuery({
    queryKey: loggedHoursKeys.list(employeeId ?? -1),
    queryFn: ({ signal }) => loggedHoursService.list(employeeId as number, signal),
    enabled: employeeId != null && currentUser != null
  })
}
