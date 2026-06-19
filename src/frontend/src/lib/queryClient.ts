import { QueryClient } from '@tanstack/react-query'
import { ApiError } from '../services/api'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // Retry up to 2 times (3 attempts total), but never on a 4xx — a client error
      // (bad input, unauthorized, not found) won't fix itself by retrying.
      retry: (count, error) =>
        !(error instanceof ApiError && error.status >= 400 && error.status < 500) && count < 2
    },
    mutations: { retry: false }
  }
})
