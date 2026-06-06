import React, { useState } from 'react'
import type { Department } from '../../types'
import { useUpdateDepartment } from '../../hooks/useDepartmentMutations'

interface Props {
  closeModal: () => void
  selectedDepartment: Department | null
}

const EditDepartmentModal: React.FC<Props> = ({ closeModal, selectedDepartment }) => {
  const [deptName, setDeptName] = useState(selectedDepartment?.name || '')
  const [validationError, setValidationError] = useState<string | null>(null)

  const updateDept = useUpdateDepartment()

  const handleUpdateDepartment = () => {
    setValidationError(null)
    if (!deptName.trim()) {
      setValidationError('Department name is required.')
      return
    }

    if (selectedDepartment) {
      updateDept.mutate(
        {
          id: selectedDepartment.id,
          payload: { name: deptName.trim() }
        },
        { onSuccess: () => closeModal() }
      )
    }
  }

  return (
    <div className="modal-form">
      <h2>Edit department</h2>
      <label>
        Name of department
        <input
          type="text"
          placeholder="e.g. Department 7"
          value={deptName}
          onChange={e => setDeptName(e.target.value)}
        />
      </label>

      {(validationError || updateDept.error) && (
        <p className="form-error">{validationError ?? updateDept.error?.message}</p>
      )}

      <button
        className="primary-btn full-width auth-submit-btn"
        onClick={handleUpdateDepartment}
        disabled={updateDept.isPending}
      >
        Save changes
      </button>
    </div>
  )
}

export default EditDepartmentModal
