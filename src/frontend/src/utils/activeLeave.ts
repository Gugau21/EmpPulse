import type { ActiveLeave, Employee } from '../types'
import { isoToDisplayDate } from './date'

// Wire LeaveType (lower-cased) → the display-cased status the badge renders.
const ACTIVE_LEAVE_STATUS: Record<ActiveLeave['type'], NonNullable<Employee['status']>> = {
  vacation: 'Vacation',
  sick: 'Sick',
  personal: 'Personal'
}

// Derives the UI status + display end date from a raw activeLeave. A null/absent
// activeLeave means the employee is working, so both fields stay undefined (no
// badge). Shared by the employee-list mapping and the profile banner so the
// type-casing and date format live in one place.
export function describeActiveLeave(activeLeave: ActiveLeave | null | undefined): {
  status?: Employee['status']
  untilDate?: string
} {
  if (!activeLeave) return {}
  return {
    status: ACTIVE_LEAVE_STATUS[activeLeave.type],
    untilDate: isoToDisplayDate(activeLeave.endDate)
  }
}
