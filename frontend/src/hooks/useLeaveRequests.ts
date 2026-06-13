import { useQuery } from '@tanstack/react-query'
import { leaveRequestService } from '../services/api'
import { leaveRequestKeys } from '../lib/queryKeys'
import { useAuth } from '../context/useAuth'

// The leave requests visible to the caller (GET /api/leave-requests), scoped
// server-side by role: an employee gets only their own, an admin their own plus
// their overseen departments', the owner everyone's. Both request pages read
// from this single query — "My requests" narrows it to the caller's own rows.
export function useLeaveRequests() {
  const { currentUser } = useAuth()
  return useQuery({
    queryKey: leaveRequestKeys.lists(),
    queryFn: ({ signal }) => leaveRequestService.list(signal),
    enabled: currentUser != null
  })
}
