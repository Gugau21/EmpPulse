import { useQuery } from '@tanstack/react-query'
import { employeeService } from '../services/api'
import { employeeKeys } from '../lib/queryKeys'
import { useAuth } from '../context/useAuth'

// Employees list. Gated on access: nothing fetches before login, and only
// owners/admins may load employees (filtered server-side).
export function useEmployeesList() {
  const { isOwner, isAdmin } = useAuth()
  return useQuery({
    queryKey: employeeKeys.lists(),
    queryFn: ({ signal }) => employeeService.getAll(signal),
    enabled: isOwner || isAdmin
  })
}
