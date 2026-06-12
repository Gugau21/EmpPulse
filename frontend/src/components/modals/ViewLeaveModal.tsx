import React from 'react'
import type { LeaveRequest } from '../../types'
import { useLeaveRequestDetail } from '../../hooks/useLeaveRequestDetail'
import LeaveRequestLoadState from './LeaveRequestLoadState'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

// A read-only twin of the accept/reject modal: it lays the request out with the
// same summary-box style but drops every action (no edit, approve or reject).
// Opened when a request can no longer be acted on (cancelled or rejected), so the
// person can inspect it but not change it. Only the id comes from the selected row;
// the displayed values are fetched fresh from the API.
const ViewLeaveModal: React.FC<Props> = ({ selectedRequest }) => {
  const rawId = selectedRequest ? Number(selectedRequest.id) : NaN
  const id = Number.isInteger(rawId) ? rawId : null
  const { data: request, isLoading, error } = useLeaveRequestDetail(id)

  // `dateRange` is the display-formatted "dd.mm.yyyy - dd.mm.yyyy"; split it back
  // into the From/Till the summary shows.
  const [from, till] = request?.dateRange?.split(' - ') ?? []

  return (
    <div className="modal-form">
      <h2>{request?.type ?? selectedRequest?.type ?? 'Vacation'} leave</h2>

      <LeaveRequestLoadState isLoading={isLoading} error={error} loaded={!!request} />

      {request && (
        <div className="request-summary-box">
          <h3>{request.employeeName}</h3>
          <p>From: {from}</p>
          <p>Till: {till}</p>
          <p>Payment: {request.paid ? 'Paid' : 'Unpaid'}</p>
          <label>
            Reason<div className="reason-box">{request.reason?.trim() || '—'}</div>
          </label>
        </div>
      )}
    </div>
  )
}

export default ViewLeaveModal
