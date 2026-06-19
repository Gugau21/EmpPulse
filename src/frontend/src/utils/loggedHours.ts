import type { LoggedHours } from '../types'

// One day's worth of logged intervals as the profile table renders it: a box per
// day. `intervals` always holds at least one entry (days with nothing logged are
// not shown); `durationMinutes` is the summed length of the day.
export interface LoggedHoursDay {
  date: string
  intervals: LoggedHours[]
  durationMinutes: number
}

// "HH:mm" → minutes since midnight.
function timeToMinutes(time: string): number {
  const [h, m] = time.split(':').map(Number)
  return h * 60 + m
}

// Groups flat interval rows into one entry per day that has logged hours, ordered
// newest day first. Days with no logged hours are omitted entirely. Intervals
// within a day are ordered ascending by start time. ISO dates compare correctly
// as plain strings.
export function buildLoggedHoursDays(entries: LoggedHours[]): LoggedHoursDay[] {
  const byDate = new Map<string, LoggedHours[]>()
  for (const entry of entries) {
    const list = byDate.get(entry.date)
    if (list) list.push(entry)
    else byDate.set(entry.date, [entry])
  }

  return [...byDate.keys()]
    .sort((a, b) => (a < b ? 1 : -1))
    .map(date => {
      const intervals = (byDate.get(date) as LoggedHours[])
        .slice()
        .sort((a, b) => timeToMinutes(a.startTime) - timeToMinutes(b.startTime))
      const durationMinutes = intervals.reduce(
        (sum, i) => sum + (timeToMinutes(i.endTime) - timeToMinutes(i.startTime)),
        0
      )
      return { date, intervals, durationMinutes }
    })
}

// "09:00" → "9:00" so a leading-zero hour matches the table's compact style.
function trimLeadingZeroHour(time: string): string {
  return time.replace(/^0/, '')
}

// "09:00", "13:00" → "9:00 - 13:00".
export function formatInterval(startTime: string, endTime: string): string {
  return `${trimLeadingZeroHour(startTime)} - ${trimLeadingZeroHour(endTime)}`
}

// Minutes → "N hours", with a decimal only when the total isn't whole hours.
export function formatDuration(minutes: number): string {
  const hours = minutes / 60
  const label = Number.isInteger(hours) ? String(hours) : hours.toFixed(2).replace(/\.?0+$/, '')
  return `${label} hours`
}
