import React, { useState } from 'react'
import type { Department } from '../../types'
import { useAdmins } from '../../hooks/useAdmins'
import { useUpdateDepartment } from '../../hooks/useDepartmentMutations'

interface Props {
  closeModal: () => void
  selectedDepartment: Department | null
}

const EditAdminsModal: React.FC<Props> = ({ closeModal, selectedDepartment }) => {
  // Working set of admin ids (Admin.id) assigned to the selected department,
  // seeded from the department's current admins. The modal remounts per open,
  // so this lazy initial value is sufficient — no effect needed to re-seed.
  const [editAdminIds, setEditAdminIds] = useState<number[]>(
    () => selectedDepartment?.admins.map(a => a.id) ?? []
  )
  const [adminsError, setAdminsError] = useState<string | null>(null)
  const adminsQuery = useAdmins(!!selectedDepartment)
  const allAdmins = adminsQuery.data ?? []
  const updateDept = useUpdateDepartment()

  const adminName = (id: number) => {
    const fromAll = allAdmins.find(a => a.id === id)
    const fromDept = selectedDepartment?.admins.find(a => a.id === id)
    const a = fromAll ?? fromDept
    return a ? `${a.user.name} ${a.user.surname}` : `Admin #${id}`
  }

  const availableAdmins = allAdmins.filter(a => !editAdminIds.includes(a.id))

  // An admin can't be detached from their only department (they'd be left with none).
  const isLastDepartment = (adminId: number): boolean => {
    const admin =
      allAdmins.find(a => a.id === adminId) ??
      selectedDepartment?.admins.find(a => a.id === adminId)
    return !!admin && admin.departmentIds.length <= 1
  }

  const handleSaveAdmins = () => {
    if (!selectedDepartment) return

    // Run all checks on submit: collect every removed admin who'd be left with no department.
    const originalIds = selectedDepartment.admins.map(a => a.id)
    const blocked = originalIds
      .filter(id => !editAdminIds.includes(id) && isLastDepartment(id))
      .map(adminName)
    if (blocked.length > 0) {
      setAdminsError(
        `${blocked.join(', ')} cannot be removed — this is their only department. ` +
          `Assign them to another department first.`
      )
      return
    }

    setAdminsError(null)
    // The mutation invalidates both the list and this department's detail.
    updateDept.mutate(
      { id: selectedDepartment.id, payload: { adminIds: editAdminIds } },
      { onSuccess: () => closeModal() }
    )
  }

  return (
    <div className="modal-form">
      <h2>Edit administrators</h2>
      <h3 className="edit-admins-title">{selectedDepartment?.name}</h3>

      <div className="edit-admins-list">
        {editAdminIds.length === 0 && (
          <div className="edit-admin-item">
            <div className="edit-admin-pill">No administrators assigned.</div>
          </div>
        )}
        {editAdminIds.map((id, idx) => (
          <div key={id} className="edit-admin-item">
            <span className="index">{idx + 1})</span>
            <div className="edit-admin-pill">{adminName(id)}</div>
            <button
              className="btn-remove"
              onClick={() => setEditAdminIds(prev => prev.filter(x => x !== id))}
              title="Remove administrator"
            >
              —
            </button>
          </div>
        ))}
      </div>

      {availableAdmins.length > 0 && (
        <label>
          <select
            value=""
            onChange={e => {
              const id = Number(e.target.value)
              if (id) setEditAdminIds(prev => (prev.includes(id) ? prev : [...prev, id]))
            }}
          >
            <option value="" disabled>
              add administrator
            </option>
            {availableAdmins.map(a => (
              <option key={a.id} value={a.id}>
                {a.user.name} {a.user.surname}
              </option>
            ))}
          </select>
        </label>
      )}

      {(adminsError || updateDept.error) && (
        <p className="form-error">{adminsError ?? updateDept.error?.message}</p>
      )}

      <button
        className="primary-btn full-width edit-admins-save-btn"
        onClick={handleSaveAdmins}
        disabled={updateDept.isPending}
      >
        save administrators
      </button>
    </div>
  )
}

export default EditAdminsModal
