import type { DefaultHoursDay } from '../services/api'
import { WEEKDAYS } from '../hooks/useWeekdayLabels'

// The working-hours editor keeps one optional shift per weekday, keyed by the
// canonical English weekday name (WEEKDAYS). Times are "HH:mm".
export type Shift = { start: string; end: string }
export type Schedule = Record<string, Shift[]>

// An empty editor schedule: every weekday present with no shift yet.
export function emptySchedule(): Schedule {
  return Object.fromEntries(WEEKDAYS.map(day => [day, []]))
}

// Builds the editor schedule from the saved days. A day's `dayOfWeek` (0-6) indexes
// into WEEKDAYS; days outside the editor's Mon–Sun range are ignored.
export function scheduleFromDays(days: DefaultHoursDay[]): Schedule {
  const schedule = emptySchedule()
  for (const day of days) {
    const weekday = WEEKDAYS[day.dayOfWeek]
    if (weekday) schedule[weekday] = [{ start: day.startTime, end: day.endTime }]
  }
  return schedule
}

// Flattens the editor schedule into the days payload: one entry per weekday that
// has a fully-filled shift (both times set). `dayOfWeek` is the WEEKDAYS index.
export function daysFromSchedule(schedule: Schedule): DefaultHoursDay[] {
  const days: DefaultHoursDay[] = []
  WEEKDAYS.forEach((weekday, index) => {
    const shift = schedule[weekday]?.[0]
    if (shift && shift.start && shift.end) {
      days.push({ dayOfWeek: index, startTime: shift.start, endTime: shift.end })
    }
  })
  return days
}

// True when every present shift has both times set and starts before it ends —
// the same rule the backend enforces, surfaced early so save can be blocked.
export function isScheduleValid(schedule: Schedule): boolean {
  return WEEKDAYS.every(weekday => {
    const shift = schedule[weekday]?.[0]
    if (!shift) return true
    if (!shift.start || !shift.end) return false
    return shift.start < shift.end
  })
}
