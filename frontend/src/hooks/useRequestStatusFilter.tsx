import { useState } from 'react'
import type { LeaveRequest } from '../types'
import FilterDropdown from '../components/FilterDropdown'
import { LEAVE_TYPE_FILTERS } from '../utils/leaveOptions'

// Leave-type filtering shared by the two request lists (My requests / Requests).
// Returns the "Filter by" control to drop in the page header plus the requests
// left after applying it. Requests are matched by their leave type, and an empty
// selection shows everything.
export function useRequestStatusFilter(requests: LeaveRequest[]) {
  const [statusFilter, setStatusFilter] = useState<string[]>([])

  const visibleRequests = statusFilter.length
    ? requests.filter(req => statusFilter.includes(req.type))
    : requests

  const filterNode = (
    <FilterDropdown options={LEAVE_TYPE_FILTERS} selected={statusFilter} onChange={setStatusFilter} />
  )

  return { visibleRequests, filterNode }
}
