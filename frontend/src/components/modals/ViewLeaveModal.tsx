import React from 'react'
import type { LeaveRequest } from '../../types'
import LeaveRequestModalShell from './LeaveRequestModalShell'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

// A read-only twin of the accept/reject modal: it lays the request out with the
// same summary-box style but drops every action (no edit, approve or reject).
// Opened when a request can no longer be acted on (cancelled or rejected), so the
// person can inspect it but not change it.
const ViewLeaveModal: React.FC<Props> = ({ selectedRequest }) => {
  return (
    <LeaveRequestModalShell selectedRequest={selectedRequest}>
      {request => {
        // `dateRange` is the display-formatted "dd.mm.yyyy - dd.mm.yyyy"; split it
        // back into the From/Till the summary shows.
        const [from, till] = request.dateRange?.split(' - ') ?? []
        return (
          <div className="request-summary-box">
            <h3>{request.employeeName}</h3>
            <p>From: {from}</p>
            <p>Till: {till}</p>
            <p>Payment: {request.paid ? 'Paid' : 'Unpaid'}</p>
            <label>
              Reason<div className="reason-box">{request.reason?.trim() || '—'}</div>
            </label>
          </div>
        )
      }}
    </LeaveRequestModalShell>
  )
}

export default ViewLeaveModal
