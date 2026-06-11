import React from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import AccordionScreen from '../components/AccordionScreen'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'
import type { LeaveRequest } from '../types'
import trashIcon from '../assets/trash-icon.png.webp'

const MyRequestsScreen: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  // TODO: wire to GET /api/leave-requests (the caller's own requests).
  const myRequests: LeaveRequest[] = []
  const { visibleRequests, filterNode } = useRequestStatusFilter(myRequests)

  return (
    <AccordionScreen
      pageTitle="My requests"
      filter={filterNode}
      footer={
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('ADD_LEAVE')}>
            + add request
          </button>
        </div>
      }
    >
      {visibleRequests.length > 0 && (
        <div className="card-box list-box">
          {visibleRequests.map(req => (
            <div
              key={req.id}
              className={`employee-row hover-slide-container clickable ${req.status === 'PENDING' ? 'dashed-active-row' : ''}`}
              onClick={() => openModal('EDIT_LEAVE_FORM', undefined, req)}
            >
              <span className={`badge badge-${req.type.toLowerCase()}`}>{req.type}</span>
              <span className="date-span">{req.dateRange}</span>
              <div className="emp-meta">
                <span className={`status-label status-${req.status.toLowerCase()}`}>
                  {req.status}
                </span>
              </div>

              <button
                className="slide-bin-btn"
                onClick={e => {
                  e.stopPropagation() // Don't also open the row's edit modal
                  openModal(
                    req.status === 'APPROVED' ? 'CANCEL_LEAVE' : 'DELETE_LEAVE',
                    undefined,
                    req
                  )
                }}
                title={req.status === 'APPROVED' ? 'Cancel Approved Leave' : 'Delete Record'}
              >
                {req.status === 'APPROVED' ? (
                  '✕'
                ) : (
                  <img src={trashIcon} alt="Delete" width={30} height={30} />
                )}
              </button>
            </div>
          ))}
        </div>
      )}
    </AccordionScreen>
  )
}

export default MyRequestsScreen
