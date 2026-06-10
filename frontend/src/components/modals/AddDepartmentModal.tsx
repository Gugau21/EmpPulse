import React, { useState } from 'react'
import { useCreateDepartment } from '../../hooks/useDepartmentMutations'

interface Props {
  closeModal: () => void
}

const AddDepartmentModal: React.FC<Props> = ({ closeModal }) => {
  const [deptName, setDeptName] = useState('')
  const [validationError, setValidationError] = useState<string | null>(null)
  const createDept = useCreateDepartment()

  const handleCreateDepartment = () => {
    setValidationError(null)
    if (!deptName.trim()) {
      setValidationError('Department name is required.')
      return
    }
    // The mutation invalidates the departments list, so the parent refreshes automatically.
    createDept.mutate({ name: deptName.trim() }, { onSuccess: () => closeModal() })
  }

  return (
    <div className="modal-form">
      <h2>Add department</h2>
      <label>
        Name of department
        <input
          type="text"
          placeholder="e.g. Department 7"
          value={deptName}
          onChange={e => setDeptName(e.target.value)}
          maxLength={100}
        />
      </label>
      {(validationError || createDept.error) && (
        <p className="form-error">{validationError ?? createDept.error?.message}</p>
      )}
      <button
        className="primary-btn full-width"
        onClick={handleCreateDepartment}
        disabled={createDept.isPending}
      >
        + add department
      </button>
    </div>
  )
}

export default AddDepartmentModal
