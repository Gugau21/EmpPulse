import React, { useState } from 'react'
import type { OpenModal } from '../types'
import { PENDING_REQUESTS } from '../utils/mockData'
import blackTriangleIcon from '../assets/black_triangle.png'

interface Props {
  openModal: OpenModal
}

const RequestManagerPage: React.FC<Props> = ({ openModal }) => {
  const [expanded, setExpanded] = useState(true)

  return (
    <div className="screen-container">
      <header className="page-header">
        <h2>Requests</h2>
        <div className="filter-dropdown">
          <span>Filter by</span>...
        </div>
      </header>

      <div className="accordion-section">
        <h3 className="department-title" onClick={() => setExpanded(!expanded)}>
          Pending Requests
          <img
            src={blackTriangleIcon}
            alt="Toggle pending requests"
            className={`chevron ${expanded ? 'expanded' : ''}`}
          />
        </h3>

        {expanded && (
          <div className="card-box list-box dashed-wrapper">
            {PENDING_REQUESTS.map(req => (
              <div
                key={req.id}
                className="employee-row clickable"
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
      </div>

      <div className="center-action">
        <button className="primary-btn" onClick={() => openModal('CREATE_REQUEST')}>
          + create request
        </button>
      </div>
    </div>
  )
}

export default RequestManagerPage
