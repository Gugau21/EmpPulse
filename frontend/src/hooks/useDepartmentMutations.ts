import { useMutation, useQueryClient } from '@tanstack/react-query'
import { departmentService, type DepartmentCreatePayload } from '../services/api'
import { departmentKeys } from '../lib/queryKeys'

export function useCreateDepartment() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (payload: DepartmentCreatePayload) => departmentService.create(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: departmentKeys.all })
  })
}

export function useUpdateDepartment() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (vars: { id: number; payload: { name?: string; adminIds?: number[] } }) =>
      departmentService.update(vars.id, vars.payload),
    onSuccess: (_data, vars) => {
      // Refresh both the list and the open detail view.
      qc.invalidateQueries({ queryKey: departmentKeys.lists() })
      qc.invalidateQueries({ queryKey: departmentKeys.detail(vars.id) })
    }
  })
}

export function useDeleteDepartment() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => departmentService.delete(id),
    onSuccess: (_data, id) => {
      qc.removeQueries({ queryKey: departmentKeys.detail(id) })
      qc.invalidateQueries({ queryKey: departmentKeys.lists() })
    }
  })
}
