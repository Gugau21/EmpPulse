import React from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import AccordionScreen from '../components/AccordionScreen'
import RequestList from '../components/RequestList'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'
import { useLeaveRequests } from '../hooks/useLeaveRequests'

const RequestManagerPage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  // The server already scopes this to what the caller manages: an admin gets
  // their overseen departments' requests (plus their own); the owner gets all.
  const managedRequestsQuery = useLeaveRequests()
  const { visibleRequests, filterNode } = useRequestStatusFilter(managedRequestsQuery.data ?? [])

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
      <RequestList
        state={managedRequestsQuery}
        requests={visibleRequests}
        renderRow={req => (
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
        )}
      />
    </AccordionScreen>
  )
}

export default RequestManagerPage
