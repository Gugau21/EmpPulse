import { useQuery } from '@tanstack/react-query'
import { departmentService } from '../services/api'
import { departmentKeys } from '../lib/queryKeys'

// Single department detail. The signal from the queryFn context is forwarded to
// getById, so React Query aborts the previous request automatically when `id`
// changes — reproducing the old manual AbortController stale-selection guard.
export function useDepartmentDetail(id: number | null) {
  return useQuery({
    queryKey: id != null ? departmentKeys.detail(id) : departmentKeys.details(),
    queryFn: ({ signal }) => departmentService.getById(id as number, signal),
    enabled: id != null
  })
}
