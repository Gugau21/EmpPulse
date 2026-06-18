import type { LeaveRequest } from '../types'
import { todayISO } from './date'

// Where today falls relative to a leave's [startDate, endDate]: `current` spans
// today, `future` hasn't started, `past` has ended. The request lists group rows
// into one accordion box per timeframe.
export type Timeframe = 'current' | 'future' | 'past'

// ISO yyyy-mm-dd strings compare correctly as plain text, so no Date parsing is
// needed. The range is inclusive on both ends.
export function timeframeOf(request: LeaveRequest, today: string): Timeframe {
  if (request.endDate < today) return 'past'
  if (request.startDate > today) return 'future'
  return 'current'
}

// Splits requests into the three timeframe buckets, preserving the incoming order
// within each one so a pre-applied sort carries through to every box.
export function partitionByTimeframe(requests: LeaveRequest[]): Record<Timeframe, LeaveRequest[]> {
  const today = todayISO()
  const buckets: Record<Timeframe, LeaveRequest[]> = { current: [], future: [], past: [] }
  for (const request of requests) buckets[timeframeOf(request, today)].push(request)
  return buckets
}
