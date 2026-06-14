import React, { useState } from 'react'
import { useCreateDepartment } from '../../hooks/useDepartmentMutations'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  closeModal: () => void
}

const AddDepartmentModal: React.FC<Props> = ({ closeModal }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  const [deptName, setDeptName] = useState('')
  const [validationError, setValidationError] = useState<string | null>(null)
  const createDept = useCreateDepartment()

  const handleCreateDepartment = () => {
    setValidationError(null)
    if (!deptName.trim()) {
      setValidationError(t.errDeptNameRequired)
      return
    }
    // The mutation invalidates the departments list, so the parent refreshes automatically.
    createDept.mutate({ name: deptName.trim() }, { onSuccess: () => closeModal() })
  }

  return (
    <div className="modal-form">
      <h2>{t.addDepartment}</h2>
      <label>
        {t.nameOfDepartment}
        <input
          type="text"
          placeholder={t.deptNamePlaceholder}
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
        {t.addDepartmentBtn}
      </button>
    </div>
  )
}

export default AddDepartmentModal