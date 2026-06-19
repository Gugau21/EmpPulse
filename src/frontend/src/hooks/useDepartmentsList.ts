import { useQuery } from '@tanstack/react-query'
import { departmentService } from '../services/api'
import { departmentKeys } from '../lib/queryKeys'
import { useAuth } from '../context/useAuth'

// Departments list. Gated on access: nothing fetches before login, and only
// owners/admins may load departments.
export function useDepartmentsList() {
  const { isOwner, isAdmin } = useAuth()
  return useQuery({
    queryKey: departmentKeys.lists(),
    queryFn: () => departmentService.getAll(),
    enabled: isOwner || isAdmin
  })
}
