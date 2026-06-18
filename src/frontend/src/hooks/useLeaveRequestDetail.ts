import { useQuery } from '@tanstack/react-query'
import { leaveRequestService } from '../services/api'
import { leaveRequestKeys } from '../lib/queryKeys'

// A single leave request (GET /api/leave-requests/{id}), scoped server-side by
// role exactly as the list is. The view/edit modals read from this so they always
// open on the server's current state rather than the possibly-stale list row.
export function useLeaveRequestDetail(id: number | null) {
  return useQuery({
    queryKey: id != null ? leaveRequestKeys.detail(id) : leaveRequestKeys.details(),
    queryFn: ({ signal }) => leaveRequestService.getById(id as number, signal),
    enabled: id != null
  })
}
