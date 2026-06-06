import { useQuery } from '@tanstack/react-query'
import { adminService } from '../services/api'
import { adminKeys } from '../lib/queryKeys'

// All admins (OWNER only), used to assign admins to departments. The caller
// passes `enabled` so the query only runs while it's needed (e.g. the Edit
// Administrators modal is open).
export function useAdmins(enabled: boolean) {
  return useQuery({
    queryKey: adminKeys.lists(),
    queryFn: () => adminService.getAll(),
    enabled
  })
}
