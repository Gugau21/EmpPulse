import { useState } from 'react'
import type { LeaveRequest } from '../types'
import FilterDropdown from '../components/FilterDropdown'
import { LEAVE_TYPE_FILTERS, LEAVE_STATUS_FILTERS } from '../utils/leaveOptions'

// Filtering shared by the two request lists (My requests / Requests). Returns the
// "Filter by" controls to drop in the page header plus the requests left after
// applying them. Requests are matched by leave type and by status; the two filters
// combine (AND), and an empty selection in either dimension shows everything for it.
export function useRequestStatusFilter(requests: LeaveRequest[]) {
  const [typeFilter, setTypeFilter] = useState<string[]>([])
  const [statusFilter, setStatusFilter] = useState<string[]>([])

  const visibleRequests = requests.filter(
    req =>
      (!typeFilter.length || typeFilter.includes(req.type)) &&
      (!statusFilter.length || statusFilter.includes(req.status))
  )

  const filterNode = (
    <div className="header-actions">
      <FilterDropdown
        label="Filter by type"
        options={LEAVE_TYPE_FILTERS}
        selected={typeFilter}
        onChange={setTypeFilter}
      />
      <FilterDropdown
        label="Filter by status"
        options={LEAVE_STATUS_FILTERS}
        selected={statusFilter}
        onChange={setStatusFilter}
      />
    </div>
  )

  return { visibleRequests, filterNode }
}
