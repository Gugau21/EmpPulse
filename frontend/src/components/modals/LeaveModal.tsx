import React, { useState } from 'react'
import type { ModalType } from '../../types'

interface Props {
  activeModal: ModalType
  closeModal: () => void
}

const REASON_MAX = 300

const LeaveModal: React.FC<Props> = ({ activeModal, closeModal }) => {
  const [reason, setReason] = useState('')

  return (
    <div className="modal-form">
      <h2>
        {activeModal === 'EDIT_LEAVE'
          ? 'Edit leave'
          : activeModal === 'CREATE_REQUEST'
            ? 'Create request'
            : 'Add request'}
      </h2>
      {activeModal === 'CREATE_REQUEST' && (
        <>
          <label>
            First name
            <input type="text" maxLength={50} />
          </label>
          <label>
            Surname
            <input type="text" maxLength={50} />
          </label>
        </>
      )}
      <label>
        Type of leave (by payment)
        <select>
          <option>Paid</option>
          <option>Unpaid</option>
        </select>
      </label>
      <label>
        Type of leave
        <select>
          <option>Vacation</option>
          <option>Sick</option>
          <option>Personal</option>
        </select>
      </label>
      <div className="balance-hint">If vacation: you have 15 days left</div>
      <label>
        From
        <input type="date" />
      </label>
      <label>
        Till
        <input type="date" />
      </label>
      <label>
        Reason
        <textarea
          rows={2}
          maxLength={REASON_MAX}
          value={reason}
          onChange={e => setReason(e.target.value)}
        />
        <span className="char-counter">{REASON_MAX - reason.length} characters left</span>
      </label>
      <button className="primary-btn full-width" onClick={closeModal}>
        {activeModal === 'EDIT_LEAVE' ? 'edit leave' : '+ create request'}
      </button>
    </div>
  )
}

export default LeaveModal
