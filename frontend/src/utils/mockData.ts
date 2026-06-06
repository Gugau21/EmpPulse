import type { LeaveRequest } from '../types'

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
  }
]

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

export const DEPARTMENTS: string[] = [
  'Department 1',
  'Department 2',
  'Department 3',
  'Department 4',
  'Department 5',
  'Department 6'
]

export const MOCK_LOGGED_HOURS = [
  { date: '28.05.2026', start: '9:00', end: '17:00', duration: '8 hours' },
  { date: '28.05.2026', start: '9:00', end: '17:00', duration: '8 hours' }
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
