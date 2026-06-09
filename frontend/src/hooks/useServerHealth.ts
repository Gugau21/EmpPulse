import { useRef } from 'react'
import { useQuery } from '@tanstack/react-query'

// How many probes in a row must fail before we declare the server down. Requiring
// more than one absorbs a single slow/blipped probe so the UI doesn't flash the
// offline screen over a transient hiccup. At a 5s interval, this means a real
// outage surfaces within ~10s.
const FAILURES_BEFORE_OFFLINE = 2

// Poll cadence and per-probe abort, both 5s. The interval sets how often we recheck;
// the timeout caps a single probe so a *hung* server (connection accepted, no
// response) counts as a failure instead of leaving the fetch pending forever.
const PROBE_INTERVAL_MS = 5000
const PROBE_TIMEOUT_MS = 5000

// Probes the backend health endpoint and reports whether the system can serve.
// A thrown fetch (connection refused / network error) means the backend is gone;
// a non-2xx response means it's reachable but unhealthy — notably Actuator returns
// 503 when its DB indicator is DOWN, so a Postgres outage counts as offline too.
// The actuator endpoint is permitAll, so this works whether or not a user is signed in.
async function probeServer(signal?: AbortSignal): Promise<boolean> {
  // Combined with React Query's own signal so an unmount still cancels the request.
  const timeout = AbortSignal.timeout(PROBE_TIMEOUT_MS)
  const merged = signal ? AbortSignal.any([signal, timeout]) : timeout
  try {
    const res = await fetch('/actuator/health', { signal: merged, cache: 'no-store' })
    return res.ok
  } catch {
    return false
  }
}

// Polls the backend on a fixed interval and reports whether it's currently
// reachable. Recovers on its own: once the server answers again, the next poll
// flips this back to true. `initialData: true` keeps the app visible during the
// very first probe so it doesn't flash the offline screen on load.
export function useServerHealth(): boolean {
  // Counts consecutive failed probes; reset to 0 the moment one succeeds, so the
  // threshold only trips on a sustained outage, not a single slow sample.
  const failures = useRef(0)
  const { data } = useQuery({
    queryKey: ['server-health'],
    queryFn: async ({ signal }) => {
      const reachable = await probeServer(signal)
      failures.current = reachable ? 0 : failures.current + 1
      return failures.current < FAILURES_BEFORE_OFFLINE
    },
    refetchInterval: PROBE_INTERVAL_MS,
    refetchIntervalInBackground: true,
    retry: false,
    staleTime: 0,
    initialData: true
  })
  return data
}
