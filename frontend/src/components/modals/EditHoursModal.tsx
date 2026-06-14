import React from 'react'
import type { Employee } from '../../types'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  closeModal: () => void
  selectedEmployee: Employee | null
}

const EditHoursModal: React.FC<Props> = ({ closeModal, selectedEmployee }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  return (
    <div className="modal-form">
      <h2>{t.editHoursTitle}</h2>
      <h4 className="log-hours-name">
        {selectedEmployee?.name || t.employee} {selectedEmployee?.surname || ''}
      </h4>

      <label>
        {t.fromLabel}
        <input type="time" defaultValue="09:00" />
      </label>

      <label>
        {t.tillLabel}
        <input type="time" defaultValue="17:00" />
      </label>

      <button className="primary-btn full-width" onClick={closeModal}>
        {t.editHoursBtn}
      </button>
    </div>
  )
}

export default EditHoursModal
