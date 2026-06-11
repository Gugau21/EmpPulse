import React, { useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import { PENDING_REQUESTS } from '../utils/mockData'
import AccordionScreen from '../components/AccordionScreen'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'

const RequestManagerPage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const [expanded, setExpanded] = useState(true)
  const { visibleRequests, filterNode } = useRequestStatusFilter(PENDING_REQUESTS)

  return (
    <AccordionScreen
      pageTitle="Requests"
      accordionTitle="Pending Requests"
      expanded={expanded}
      onToggle={() => setExpanded(!expanded)}
      filter={filterNode}
      footer={
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('CREATE_REQUEST')}>
            + create request
          </button>
        </div>
      }
    >
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
    </AccordionScreen>
  )
}

export default RequestManagerPage
