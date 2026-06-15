import React from 'react'
import type { ModalType } from '../../types'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  activeModal: ModalType
  closeModal: () => void
  confirmModal: () => void
  confirmError?: string | null
  onConfirmErrorClear?: () => void
}

const ConfirmModal: React.FC<Props> = ({
  activeModal,
  closeModal,
  confirmModal,
  confirmError,
  onConfirmErrorClear
}) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  return (
    <div className="modal-confirm">
      <h3>
        {activeModal === 'DELETE_EMPLOYEE' && t.confirmDeleteEmp}
        {activeModal === 'DELETE_LEAVE' && t.confirmDeleteLeave}
        {activeModal === 'CANCEL_LEAVE' && t.confirmCancelLeave}
        {activeModal === 'DELETE_DEPARTMENT' && t.confirmDeleteDept}
        {activeModal === 'LOGOUT' && t.confirmLogout}
        {activeModal === 'CHANGE_PASSWORD' && t.confirmChangePwd}
      </h3>
      {confirmError && <p className="form-error">{confirmError}</p>}
      <div className="modal-buttons">
        <button
          className="btn-secondary"
          onClick={() => {
            onConfirmErrorClear?.()
            confirmModal()
          }}
        >
          {t.yes}
        </button>
        <button className="btn-outline-primary" onClick={closeModal}>
          {t.no}
        </button>
      </div>
    </div>
  )
}

export default ConfirmModal
