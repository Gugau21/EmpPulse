import React from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import AccordionScreen from '../components/AccordionScreen'
import RequestList from '../components/RequestList'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'
import { useLeaveRequests } from '../hooks/useLeaveRequests'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import { useRequestLabels } from '../hooks/useRequestLabels'

const RequestManagerPage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const { language } = useLanguage()
  const t = translations[language].requestManagerPage
  const { typeMap, statusMap } = useRequestLabels()

  // The server already scopes this to what the caller manages: an admin gets
  // their overseen departments' requests (plus their own); the owner gets all.
  const managedRequestsQuery = useLeaveRequests()
  const { visibleRequests, filterNode } = useRequestStatusFilter(managedRequestsQuery.data ?? [])

  return (
    <AccordionScreen
      pageTitle={t.title}
      filter={filterNode}
      footer={
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('CREATE_REQUEST')}>
            {t.createRequest}
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
            className={`employee-row clickable ${req.status === 'PENDING' ? 'dashed-row' : ''}`}
            // Pending rows open the accept/reject modal; approved rows open the
            // edit form directly; finalized (cancelled/rejected) rows open the
            // read-only view.
            onClick={
              req.status === 'PENDING'
                ? () => openModal('ACCEPT_REQUEST', undefined, req)
                : req.status === 'APPROVED'
                  ? () => openModal('EDIT_LEAVE_FORM', undefined, req)
                  : () => openModal('VIEW_LEAVE_FORM', undefined, req)
            }
          >
            <span className="emp-name">{req.employeeName}</span>
            <span className="date-span">{req.dateRange}</span>
            <div className="emp-meta">
              {/* Map the type but leave the CSS class as English */}
              <span className={`badge badge-${req.type.toLowerCase()}`}>
                {typeMap[req.type] || req.type}
              </span>
              {/* Map the status but leave the CSS class as English */}
              <span className={`status-label status-${req.status.toLowerCase()}`}>
                {statusMap[req.status] || req.status}
              </span>
            </div>
          </div>
        )}
      />
    </AccordionScreen>
  )
}

export default RequestManagerPage
