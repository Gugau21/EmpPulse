import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { userService, type BonusVacationDayPayload } from '../services/api'
import { userKeys } from '../lib/queryKeys'

// GET /api/users/{userId}/bonus-vacation-days — the bonus days granted to the
// employee for the current year, used to pre-fill the bonus-days modal.
export function useBonusVacationDays(userId: number | null) {
  return useQuery({
    queryKey: userKeys.bonusVacationDays(userId as number),
    queryFn: ({ signal }) => userService.getBonusVacationDays(userId as number, signal),
    enabled: userId != null
  })
}

// Updating the bonus changes the employee's computed vacation balance, so
// invalidate their profile detail (the profile shows vacationBalance) alongside
// the bonus query itself.
export function useUpdateBonusVacationDays() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, payload }: { userId: number; payload: BonusVacationDayPayload }) =>
      userService.updateBonusVacationDays(userId, payload),
    onSuccess: (_data, { userId }) => {
      qc.invalidateQueries({ queryKey: userKeys.bonusVacationDays(userId) })
      qc.invalidateQueries({ queryKey: userKeys.detail(userId) })
    }
  })
}
