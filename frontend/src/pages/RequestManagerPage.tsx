import React from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import AccordionScreen from '../components/AccordionScreen'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'
import { useLeaveRequests } from '../hooks/useLeaveRequests'

const RequestManagerPage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  // The server already scopes this to what the caller manages: an admin gets
  // their overseen departments' requests (plus their own); the owner gets all.
  const { data: requests = [], isLoading, isError, error } = useLeaveRequests()
  const { visibleRequests, filterNode } = useRequestStatusFilter(requests)

  return (
    <AccordionScreen
      pageTitle="Requests"
      filter={filterNode}
      footer={
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('CREATE_REQUEST')}>
            + create request
          </button>
        </div>
      }
    >
      {isLoading && <p className="muted">Loading requests…</p>}
      {isError && (
        <p className="form-error">{error?.message ?? 'Failed to load requests.'}</p>
      )}

      {visibleRequests.length > 0 && (
        <div className="card-box list-box">
          {visibleRequests.map(req => (
            <div
              key={req.id}
              className={`employee-row ${
                req.status === 'PENDING'
                  ? 'clickable dashed-row'
                  : req.status === 'APPROVED'
                    ? 'clickable'
                    : ''
              }`}
              // Pending rows open the accept/reject modal; approved rows open the
              // edit form directly. Other statuses aren't clickable.
              onClick={
                req.status === 'PENDING'
                  ? () => openModal('ACCEPT_REQUEST', undefined, req)
                  : req.status === 'APPROVED'
                    ? () => openModal('EDIT_LEAVE_FORM', undefined, req)
                    : undefined
              }
            >
              <span className="emp-name">{req.employeeName}</span>
              <span className="date-span">{req.dateRange}</span>
              <div className="emp-meta">
                <span className={`badge badge-${req.type.toLowerCase()}`}>{req.type}</span>
                <span className={`status-label status-${req.status.toLowerCase()}`}>
                  {req.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </AccordionScreen>
  )
}

export default RequestManagerPage
