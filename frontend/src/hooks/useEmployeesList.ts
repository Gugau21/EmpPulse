import { useQuery } from '@tanstack/react-query'
import { employeeService } from '../services/api'
import { employeeKeys } from '../lib/queryKeys'
import { useAuth } from '../context/useAuth'

// Employees list. Gated on role: nothing fetches before login (userRole is
// null), and only OWNER/ADMIN may load employees (filtered server-side).
export function useEmployeesList() {
  const { userRole } = useAuth()
  return useQuery({
    queryKey: employeeKeys.lists(),
    queryFn: () => employeeService.getAll(),
    enabled: userRole === 'OWNER' || userRole === 'ADMIN'
  })
}
