import React, { useState } from 'react'
import type { Department } from '../../types'
import { useUpdateDepartment } from '../../hooks/useDepartmentMutations'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  closeModal: () => void
  selectedDepartment: Department | null
}

const EditDepartmentModal: React.FC<Props> = ({ closeModal, selectedDepartment }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  const [deptName, setDeptName] = useState(selectedDepartment?.name || '')
  const [validationError, setValidationError] = useState<string | null>(null)

  const updateDept = useUpdateDepartment()

  const handleUpdateDepartment = () => {
    setValidationError(null)
    if (!deptName.trim()) {
      setValidationError(t.errDeptNameRequired)
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
      <h2>{t.editDepartment}</h2>
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

      {(validationError || updateDept.error) && (
        <p className="form-error">{validationError ?? updateDept.error?.message}</p>
      )}

      <button
        className="primary-btn full-width auth-submit-btn"
        onClick={handleUpdateDepartment}
        disabled={updateDept.isPending}
      >
        {t.saveChanges}
      </button>
    </div>
  )
}

export default EditDepartmentModal