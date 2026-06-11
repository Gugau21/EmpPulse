import type { Employee, LeaveRequest } from '../types'

// ─────────────────────────────────────────────────────────────────────────────
// Status filtering (test-only)
//
// The tag set the "Filter by" dropdown offers on the Employees, My requests and
// Requests pages. There is no "Working" status: a person is "working" simply when
// they carry no leave, so it's a filter tag rather than a stored value. The
// leave/status API is not wired up yet, so these tags exist purely to exercise
// the filtering UI against mock data.
// ─────────────────────────────────────────────────────────────────────────────
export type LeaveStatus = NonNullable<Employee['status']>

// The pseudo-tag for "no active leave".
export const WORKING_TAG = 'Working'

export const STATUS_FILTERS: { value: string; label: string }[] = [
  { value: WORKING_TAG, label: 'Working' },
  { value: 'Sick', label: 'Sick' },
  { value: 'Vacation', label: 'Vacation' },
  { value: 'Personal', label: 'Personal' }
]

// Employees come from the real API (GET /api/employees), which carries no status
// field. To test status filtering we assign each employee a deterministic mock
// status from their id, so the same person always lands in the same bucket.
// `undefined` means the employee is working (no leave).
const STATUS_CYCLE: (LeaveStatus | undefined)[] = [undefined, 'Sick', 'Vacation', 'Personal']

export function mockEmployeeStatus(id: string): LeaveStatus | undefined {
  const n = Number(id)
  return STATUS_CYCLE[(Number.isFinite(n) ? n : 0) % STATUS_CYCLE.length]
}

// ─────────────────────────────────────────────────────────────────────────────
// Leave requests (test-only)
//
// There is no leave/requests API yet, so the Requests and My requests pages
// render this static data to exercise their layout and the status filter. Each
// list intentionally covers all three leave types so filtering is observable.
// Replace with real queries once the leave feature is wired up.
// ─────────────────────────────────────────────────────────────────────────────

// Requests page (admin/owner inbox) — all pending, mixed types.
export const PENDING_REQUESTS: LeaveRequest[] = [
  {
    id: '1',
    employeeName: 'Nazar Bezmenov',
    type: 'Sick',
    dateRange: '20.05.2026 - 21.05.2026',
    status: 'PENDING'
  },
  {
    id: '2',
    employeeName: 'Maryia Stralchonak',
    type: 'Vacation',
    dateRange: '10.05.2026 - 18.05.2026',
    status: 'PENDING'
  },
  {
    id: '3',
    employeeName: 'Oleh Petrenko',
    type: 'Personal',
    dateRange: '02.06.2026 - 03.06.2026',
    status: 'PENDING'
  },
  {
    id: '4',
    employeeName: 'Iryna Kovalenko',
    type: 'Vacation',
    dateRange: '15.06.2026 - 25.06.2026',
    status: 'PENDING'
  }
]

// My requests page — the signed-in user's own history, mixed types and statuses.
export const MY_REQUESTS: LeaveRequest[] = [
  {
    id: '1',
    employeeName: 'Me',
    type: 'Vacation',
    dateRange: '20.06.2026 - 30.06.2026',
    status: 'PENDING'
  },
  {
    id: '2',
    employeeName: 'Me',
    type: 'Personal',
    dateRange: '28.05.2026 - 30.05.2026',
    status: 'REJECTED'
  },
  {
    id: '3',
    employeeName: 'Me',
    type: 'Sick',
    dateRange: '17.03.2026 - 21.03.2026',
    status: 'APPROVED'
  },
  {
    id: '4',
    employeeName: 'Me',
    type: 'Vacation',
    dateRange: '20.12.2025 - 26.12.2025',
    status: 'CANCELLED'
  }
]

// Default-working-hours placeholder, grouped into the three grid columns the
// profile card renders. Drives the day/shift cells until working hours are
// wired to the API, replacing the previously copy-pasted markup.
export interface WorkingHoursShift {
  start: string
  end: string
}

export interface WorkingHoursDay {
  label: string
  shifts: WorkingHoursShift[]
}

export const MOCK_DEFAULT_WORKING_HOURS: WorkingHoursDay[][] = [
  [
    {
      label: 'Monday',
      shifts: [
        { start: '9:00', end: '17:00' },
        { start: '17:00', end: '20:00' }
      ]
    },
    { label: 'Tuesday', shifts: [{ start: '9:00', end: '17:00' }] }
  ],
  [
    { label: 'Wednesday', shifts: [{ start: '9:00', end: '17:00' }] },
    { label: 'Thursday', shifts: [{ start: '9:00', end: '17:00' }] }
  ],
  [{ label: 'Friday', shifts: [{ start: '9:00', end: '17:00' }] }]
]
