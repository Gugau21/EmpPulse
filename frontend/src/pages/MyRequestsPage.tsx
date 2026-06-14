import React from 'react'
import { useOutletContext } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import AccordionScreen from '../components/AccordionScreen'
import RequestList from '../components/RequestList'
import { useRequestStatusFilter } from '../hooks/useRequestStatusFilter'
import { useLeaveRequests } from '../hooks/useLeaveRequests'
import { useAuth } from '../context/useAuth'
import trashIcon from '../assets/trash-icon.png.webp'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

const MyRequestsScreen: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const { currentUser } = useAuth()
  const { language } = useLanguage()
  const t = translations[language].myRequestsPage
  
  // Grab our filter dictionary so we can reuse the type/status translations
  const filtersT = translations[language].requestFilters

  const typeMap: Record<string, string> = {
    Vacation: filtersT.typeVacation,
    Sick: filtersT.typeSick,
    Personal: filtersT.typePersonal
  }

  const statusMap: Record<string, string> = {
    PENDING: filtersT.statusPending,
    APPROVED: filtersT.statusApproved,
    REJECTED: filtersT.statusRejected,
    CANCELLED: filtersT.statusCancelled
  }

  const myRequestsQuery = useLeaveRequests()
  // The shared query may also carry requests this user oversees (when they are
  // an admin/owner too); this page is only the caller's own, keyed by employee id.
  const myEmployeeId = currentUser?.employeeProfile?.employeeId
  const myRequests =
    myEmployeeId != null
      ? (myRequestsQuery.data ?? []).filter(req => req.employeeId === myEmployeeId)
      : []
  const { visibleRequests, filterNode } = useRequestStatusFilter(myRequests)

  return (
    <AccordionScreen
      pageTitle={t.title}
      filter={filterNode}
      footer={
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('ADD_LEAVE')}>
            {t.addRequest}
          </button>
        </div>
      }
    >
      <RequestList
        state={myRequestsQuery}
        requests={visibleRequests}
        renderRow={req => (
          <div
            key={req.id}
            className={`employee-row hover-slide-container clickable ${req.status === 'PENDING' ? 'dashed-active-row' : ''}`}
            onClick={() =>
              openModal(
                // Cancelled/rejected requests are final, so they open read-only;
                // anything still actionable opens the editable form.
                req.status === 'CANCELLED' || req.status === 'REJECTED'
                  ? 'VIEW_LEAVE_FORM'
                  : 'EDIT_LEAVE_FORM',
                undefined,
                req
              )
            }
          >
            {/* Map the type but leave the CSS class as English */}
            <span className={`badge badge-${req.type.toLowerCase()}`}>
              {typeMap[req.type] || req.type}
            </span>
            <span className="date-span">{req.dateRange}</span>
            <div className="emp-meta">
              {/* Map the status but leave the CSS class as English */}
              <span className={`status-label status-${req.status.toLowerCase()}`}>
                {statusMap[req.status] || req.status}
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
              title={req.status === 'APPROVED' ? t.cancelApproved : t.deleteRecord}
            >
              {req.status === 'APPROVED' ? (
                '✕'
              ) : (
                <img src={trashIcon} alt="Delete" width={30} height={30} />
              )}
            </button>
          </div>
        )}
      />
    </AccordionScreen>
  )
}

export default MyRequestsScreen