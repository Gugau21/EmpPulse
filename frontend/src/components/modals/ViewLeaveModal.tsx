import React from 'react'
import type { LeaveRequest } from '../../types'
import { useLeaveRequestDetail } from '../../hooks/useLeaveRequestDetail'
import LeaveRequestLoadState from './LeaveRequestLoadState'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

// A read-only twin of EditLeaveModal: it lays the request out with the same
// labels but renders each value as static text, with no inputs and no actions.
// Opened when a request can no longer be acted on (cancelled or rejected), so the
// person can inspect it but not change it. Only the id comes from the selected row;
// the displayed values are fetched fresh from the API.
const ViewLeaveModal: React.FC<Props> = ({ selectedRequest }) => {
  const rawId = selectedRequest ? Number(selectedRequest.id) : NaN
  const id = Number.isInteger(rawId) ? rawId : null
  const { data: request, isLoading, error } = useLeaveRequestDetail(id)

  const [startStr, endStr] = request?.dateRange?.split(' - ') ?? []

  return (
    <div className="modal-form">
      <h2>View request</h2>

      <LeaveRequestLoadState isLoading={isLoading} error={error} loaded={!!request} />

      {request && (
        <>
          <label>
            Status
            <div className="reason-box">{request.status ?? '—'}</div>
          </label>

          <label>
            Type of leave
            <div className="reason-box">{request.type ?? '—'}</div>
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
            <div className="reason-box">{request.reason?.trim() || '—'}</div>
          </label>
        </>
      )}
    </div>
  )
}

export default ViewLeaveModal
