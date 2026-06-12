import { useState } from 'react'
import type { LeaveRequest } from '../types'
import FilterDropdown from '../components/FilterDropdown'
import SortDropdown from '../components/SortDropdown'
import {
  LEAVE_TYPE_FILTERS,
  LEAVE_STATUS_FILTERS,
  LEAVE_SORT_OPTIONS,
  type LeaveSortKey
} from '../utils/leaveOptions'

// Filtering and sorting shared by the two request lists (My requests / Requests).
// Returns the "Filter by"/"Sort by" controls to drop in the page header plus the
// requests left after applying them. Requests are matched by leave type and by
// status; the two filters combine (AND), and an empty selection in either
// dimension shows everything for it. An optional sort then reorders the result.
export function useRequestStatusFilter(requests: LeaveRequest[]) {
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState<string[]>([])
  const [statusFilter, setStatusFilter] = useState<string[]>([])
  // Default to ordering by leave start date; the user can clear or switch it.
  const [sortKey, setSortKey] = useState<LeaveSortKey | null>('startDate')

  // Surname search, mirroring the Employees page: an empty box matches everyone,
  // otherwise the typed text must appear in the request's employee name.
  const query = search.trim().toLowerCase()
  const filtered = requests.filter(
    req =>
      (!query || req.employeeName.toLowerCase().includes(query)) &&
      (!typeFilter.length || typeFilter.includes(req.type)) &&
      (!statusFilter.length || statusFilter.includes(req.status))
  )

  // Copy before sorting so we don't mutate the array react-query hands us. ISO
  // date/timestamp strings compare correctly as text: start date ascending
  // (earliest leave first), last edit descending (most recently touched first).
  const visibleRequests =
    sortKey === 'startDate'
      ? [...filtered].sort((a, b) => a.startDate.localeCompare(b.startDate))
      : sortKey === 'lastEdit'
        ? [...filtered].sort((a, b) => b.lastEditedAt.localeCompare(a.lastEditedAt))
        : filtered

  const filterNode = (
    <div className="header-actions">
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by surname"
          value={search}
          onChange={e => setSearch(e.target.value)}
          maxLength={50}
          style={{ width: '16ch' }}
        />
      </div>
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
      <SortDropdown
        options={LEAVE_SORT_OPTIONS}
        selected={sortKey}
        onChange={value => setSortKey(value as LeaveSortKey | null)}
      />
    </div>
  )

  return { visibleRequests, filterNode }
}
