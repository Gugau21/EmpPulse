import type { LeaveRequest } from '../types'

// The three leave types are real domain values (not mock data): the request
// lists filter their rows by leave type, so the "Filter by" dropdown offers one
// tag per type. Kept here so both request pages share a single source.
export const LEAVE_TYPE_FILTERS: { value: LeaveRequest['type']; label: string }[] = [
  { value: 'Vacation', label: 'Vacation' },
  { value: 'Sick', label: 'Sick' },
  { value: 'Personal', label: 'Personal' }
]

// The request lifecycle states, offered as a second "Filter by status" dropdown on
// the request lists.
export const LEAVE_STATUS_FILTERS: { value: LeaveRequest['status']; label: string }[] = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'CANCELLED', label: 'Cancelled' }
]
