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
