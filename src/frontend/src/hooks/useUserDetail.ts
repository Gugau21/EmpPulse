import { useQuery } from '@tanstack/react-query'
import { userService } from '../services/api'
import { userKeys } from '../lib/queryKeys'

export function useUserDetail(id: number | null) {
  return useQuery({
    queryKey: id != null ? userKeys.detail(id) : userKeys.details(),
    queryFn: ({ signal }) => userService.getById(id as number, signal),
    enabled: id != null
  })
}
