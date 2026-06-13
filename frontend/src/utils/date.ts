// Today as a local ISO `yyyy-mm-dd`. Using the local calendar day (not UTC) keeps
// it consistent with the date pickers and the wire dates, which are plain
// yyyy-mm-dd, so callers can compare them as ordinary strings.
export function todayISO(): string {
  const now = new Date()
  const off = now.getTimezoneOffset()
  return new Date(now.getTime() - off * 60_000).toISOString().slice(0, 10)
}

// ISO yyyy-mm-dd → the dd.mm.yyyy display the request rows, edit modal and status
// labels expect.
export function isoToDisplayDate(iso: string): string {
  const [y, m, d] = iso.split('-')
  return `${d}.${m}.${y}`
}
