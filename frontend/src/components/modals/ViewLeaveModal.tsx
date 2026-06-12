import React from 'react'
import type { LeaveRequest } from '../../types'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

// A read-only twin of EditLeaveModal: it lays the request out with the same
// labels but renders each value as static text, with no inputs and no actions.
// Opened when a request can no longer be acted on (cancelled or rejected), so the
// person can inspect it but not change it.
const ViewLeaveModal: React.FC<Props> = ({ selectedRequest }) => {
  const [startStr, endStr] = selectedRequest?.dateRange?.split(' - ') ?? []

  return (
    <div className="modal-form">
      <h2>View request</h2>

      <label>
        Status
        <div className="reason-box">{selectedRequest?.status ?? '—'}</div>
      </label>

      <label>
        Type of leave
        <div className="reason-box">{selectedRequest?.type ?? '—'}</div>
      </label>

      <label>
        From
        <div className="reason-box">{startStr ?? '—'}</div>
      </label>

      <label>
        Till
        <div className="reason-box">{endStr ?? '—'}</div>
      </label>

      <label>
        Reason
        <div className="reason-box">{selectedRequest?.reason?.trim() || '—'}</div>
      </label>
    </div>
  )
}

export default ViewLeaveModal
