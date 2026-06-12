import React from 'react'
import type { LeaveRequest, OpenModal } from '../../types'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
  openModal: OpenModal
}

const AcceptRequestModal: React.FC<Props> = ({ closeModal, selectedRequest, openModal }) => {
  // `dateRange` is the display-formatted "dd.mm.yyyy - dd.mm.yyyy"; split it back
  // into the From/Till the summary shows.
  const [from, till] = selectedRequest?.dateRange?.split(' - ') ?? []

  return (
    <div className="modal-form">
      <h2>{selectedRequest?.type ?? 'Vacation'} leave</h2>
      <div className="request-summary-box">
        <h3>{selectedRequest?.employeeName}</h3>
        <p>From: {from}</p>
        <p>Till: {till}</p>
        <p>Payment: {selectedRequest?.paid ? 'Paid' : 'Unpaid'}</p>
        <label>
          Reason<div className="reason-box">{selectedRequest?.reason ?? ''}</div>
        </label>
      </div>
      <div className="modal-actions">
        <button className="primary-btn full-width" onClick={closeModal}>
          accept request
        </button>
        <button className="primary-btn danger full-width" onClick={closeModal}>
          reject request
        </button>
      </div>
      <button
        className="link-btn"
        // Reopen this accept/reject modal as the back target so the edit form
        // can return here.
        onClick={() =>
          openModal('EDIT_LEAVE_FORM', undefined, selectedRequest ?? undefined, 'ACCEPT_REQUEST')
        }
      >
        edit request
      </button>
    </div>
  )
}

export default AcceptRequestModal
