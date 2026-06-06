import React, { useState } from 'react'
import type { LeaveRequest, OpenModal } from '../types'
import trashIcon from '../assets/trash-icon.png.webp'
import blackTriangleIcon from '../assets/black_triangle.png'

interface Props {
  openModal: OpenModal
}

const myRecordsData: LeaveRequest[] = [
  {
    id: '1',
    employeeName: 'Me',
    type: 'Vacation',
    dateRange: '20.06.2026 - 30.06.2026',
    status: 'PENDING'
  },
  {
    id: '2',
    employeeName: 'Me',
    type: 'Personal',
    dateRange: '28.05.2026 - 30.05.2026',
    status: 'REJECTED'
  },
  {
    id: '3',
    employeeName: 'Me',
    type: 'Sick',
    dateRange: '17.03.2026 - 21.03.2026',
    status: 'APPROVED'
  },
  {
    id: '4',
    employeeName: 'Me',
    type: 'Vacation',
    dateRange: '20.12.2025 - 26.12.2025',
    status: 'CANCELLED'
  }
]

const MyRequestsScreen: React.FC<Props> = ({ openModal }) => {
  const [expanded, setExpanded] = useState(true)

  return (
    <div className="screen-container">
      <header className="page-header">
        <h2>My requests</h2>
        <div className="filter-dropdown">
          <span>Filter by</span>...
        </div>
      </header>

      <div className="accordion-section">
        <h3 className="department-title" onClick={() => setExpanded(!expanded)}>
          Last requests{' '}
          <img
            src={blackTriangleIcon}
            alt="Toggle last requests"
            className={`chevron ${expanded ? 'expanded' : ''}`}
          />
        </h3>

        {expanded && (
          <div className="card-box list-box">
            {myRecordsData.map(req => (
              <div
                key={req.id}
                className={`employee-row hover-slide-container clickable ${req.status === 'PENDING' ? 'dashed-active-row' : ''}`}
                onClick={() => openModal('EDIT_LEAVE_FORM', undefined, req)} // Red arrow connects ALL rows. Click to edit.
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
                    e.stopPropagation() // Prevents navigating to edit
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
      </div>

      <div className="center-action">
        <button className="primary-btn" onClick={() => openModal('ADD_LEAVE')}>
          + add request
        </button>
      </div>
    </div>
  )
}

export default MyRequestsScreen
