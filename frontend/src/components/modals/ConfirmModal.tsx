import React from 'react'
import type { ModalType } from '../../types'

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
}) => (
  <div className="modal-confirm">
    <h3>
      {activeModal === 'DELETE_EMPLOYEE' && 'Do you really want to delete employee?'}
      {activeModal === 'DELETE_LEAVE' && 'Do you really want to delete a leave?'}
      {activeModal === 'CANCEL_LEAVE' && 'Do you really want to cancel a leave?'}
      {activeModal === 'DELETE_DEPARTMENT' && 'Do you really want to delete a department?'}
      {activeModal === 'LOGOUT' && 'Do you really want to log out?'}
      {activeModal === 'CHANGE_PASSWORD' && 'Do you really want to change password?'}
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
        YES
      </button>
      <button className="btn-outline-primary" onClick={closeModal}>
        NO
      </button>
    </div>
  </div>
)

export default ConfirmModal
