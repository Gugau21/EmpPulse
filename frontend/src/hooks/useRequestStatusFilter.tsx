import { useState } from 'react'
import type { LeaveRequest } from '../types'
import FilterDropdown from '../components/FilterDropdown'
import { STATUS_FILTERS } from '../utils/mockData'

// Status-tag filtering shared by the two request lists (My requests / Requests).
// Returns the "Filter by" control to drop in the page header plus the requests
// left after applying it. Requests are matched by their leave type, so the
// "Working" tag never matches one (and an empty selection shows everything).
export function useRequestStatusFilter(requests: LeaveRequest[]) {
  const [statusFilter, setStatusFilter] = useState<string[]>([])

  const visibleRequests = statusFilter.length
    ? requests.filter(req => statusFilter.includes(req.type))
    : requests

  const filterNode = (
    <FilterDropdown options={STATUS_FILTERS} selected={statusFilter} onChange={setStatusFilter} />
  )

  return { visibleRequests, filterNode }
}
