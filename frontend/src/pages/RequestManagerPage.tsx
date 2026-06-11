import React from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import type { LeaveRequest } from '../types'
import AccordionScreen from '../components/AccordionScreen'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'

const RequestManagerPage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  // TODO: wire to GET /api/leave-requests (pending requests in overseen departments).
  const pendingRequests: LeaveRequest[] = []
  const { visibleRequests, filterNode } = useRequestStatusFilter(pendingRequests)

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
      {visibleRequests.length > 0 && (
        <div className="card-box list-box">
          {visibleRequests.map(req => (
            <div
              key={req.id}
              className="employee-row clickable dashed-row"
              onClick={() => openModal('ACCEPT_REQUEST', undefined, req)}
            >
              <span className="emp-name">{req.employeeName}</span>
              <div className="emp-meta">
                <span className={`badge badge-${req.type.toLowerCase()}`}>{req.type}</span>
                <span className="until-text">{req.dateRange}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </AccordionScreen>
  )
}

export default RequestManagerPage
