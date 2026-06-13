import React from 'react'
import type { LeaveRequest, OpenModal } from '../../types'
import { useRespondToLeaveRequest } from '../../hooks/useLeaveRequestMutations'
import LeaveRequestModalShell from './LeaveRequestModalShell'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
  openModal: OpenModal
}

const AcceptRequestModal: React.FC<Props> = ({ closeModal, selectedRequest, openModal }) => {
  const respond = useRespondToLeaveRequest()

  // Send the admin's approve/reject decision, then close on success. `request` is
  // the loaded detail the shell hands us, so its id is always valid.
  const handleDecision = (request: LeaveRequest, status: 'approved' | 'rejected') => {
    respond.mutate(
      { id: Number(request.id), payload: { status } },
      { onSuccess: () => closeModal() }
    )
  }

  return (
    <LeaveRequestModalShell selectedRequest={selectedRequest}>
      {request => {
        // `dateRange` is the display-formatted "dd.mm.yyyy - dd.mm.yyyy"; split it
        // back into the From/Till the summary shows.
        const [from, till] = request.dateRange?.split(' - ') ?? []
        return (
          <>
            <div className="request-summary-box">
              <h3>{request.employeeName}</h3>
              <p>From: {from}</p>
              <p>Till: {till}</p>
              <p>{request.paid ? 'Paid' : 'Unpaid'}</p>
              <label>
                Reason<div className="reason-box">{request.reason ?? ''}</div>
              </label>
            </div>
            {respond.error && <p className="form-error">{respond.error.message}</p>}
            <div className="modal-actions">
              <button
                className="primary-btn full-width"
                onClick={() => handleDecision(request, 'approved')}
                disabled={respond.isPending}
              >
                accept request
              </button>
              <button
                className="primary-btn danger full-width"
                onClick={() => handleDecision(request, 'rejected')}
                disabled={respond.isPending}
              >
                reject request
              </button>
            </div>
            <button
              className="link-btn"
              // Reopen this accept/reject modal as the back target so the edit form
              // can return here.
              onClick={() =>
                openModal(
                  'EDIT_LEAVE_FORM',
                  undefined,
                  selectedRequest ?? undefined,
                  'ACCEPT_REQUEST'
                )
              }
            >
              edit request
            </button>
          </>
        )
      }}
    </LeaveRequestModalShell>
  )
}

export default AcceptRequestModal
