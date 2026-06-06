import React from 'react'
import type { LeaveRequest } from '../../types'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

const AcceptRequestModal: React.FC<Props> = ({ closeModal, selectedRequest }) => (
  <div className="modal-form">
    <h2>Vacation leave</h2>
    <div className="request-summary-box">
      <h3>{selectedRequest?.employeeName || 'Maryia Stralchonak'}</h3>
      <p>From: 10.05.2026</p>
      <p>Till: 18.05.2026</p>
      <label>
        Reason<div className="reason-box">Taking designated annual family leave.</div>
      </label>
    </div>
    <button className="primary-btn full-width" onClick={closeModal}>
      accept request
    </button>
  </div>
)

export default AcceptRequestModal
