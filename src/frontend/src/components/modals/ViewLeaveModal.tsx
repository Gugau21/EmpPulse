import React from 'react'
import type { LeaveRequest } from '../../types'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'
import LeaveRequestModalShell from './LeaveRequestModalShell'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

// A read-only twin of the accept/reject modal: it lays the request out with the
// same summary-box style but drops every action (no edit, approve or reject).
// Opened when a request can no longer be acted on (cancelled or rejected), so the
// person can inspect it but not change it.
const ViewLeaveModal: React.FC<Props> = ({ selectedRequest }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  return (
    <LeaveRequestModalShell selectedRequest={selectedRequest}>
      {request => {
        // `dateRange` is the display-formatted "dd.mm.yyyy - dd.mm.yyyy"; split it
        // back into the From/Till the summary shows.
        const [from, till] = request.dateRange?.split(' - ') ?? []
        return (
          <div className="request-summary-box">
            <h3>{request.employeeName}</h3>
            <p>
              {t.from} {from}
            </p>
            <p>
              {t.till} {till}
            </p>
            <p>
              {t.payment} {request.paid ? t.paid : t.unpaid}
            </p>
            <label>
              {t.reason}
              <div className="reason-box">{request.reason?.trim() || t.emptyReason}</div>
            </label>
          </div>
        )
      }}
    </LeaveRequestModalShell>
  )
}

export default ViewLeaveModal
