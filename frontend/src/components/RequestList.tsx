import React from 'react'
import type { UseQueryResult } from '@tanstack/react-query'
import type { LeaveRequest } from '../types'

interface Props {
  // The list-loading query straight from useLeaveRequests; this reads its
  // loading/error flags so both request pages render those states identically.
  state: Pick<UseQueryResult<LeaveRequest[]>, 'isLoading' | 'isError' | 'error'>
  requests: LeaveRequest[]
  // Each page renders its own row markup (the columns and click behaviour differ
  // between "My requests" and the manager view); this owns the surrounding
  // loading/error/empty states and the list-box wrapper they share.
  renderRow: (request: LeaveRequest) => React.ReactNode
}

const RequestList: React.FC<Props> = ({ state, requests, renderRow }) => (
  <>
    {state.isLoading && <p className="muted">Loading requests…</p>}
    {state.isError && (
      <p className="form-error">{state.error?.message ?? 'Failed to load requests.'}</p>
    )}

    {requests.length > 0 && <div className="card-box list-box">{requests.map(renderRow)}</div>}
  </>
)

export default RequestList
