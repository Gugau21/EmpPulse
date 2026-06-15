import React from 'react'
import type { LeaveRequest } from '../../types'
import { useLeaveRequestDetail } from '../../hooks/useLeaveRequestDetail'
import LeaveRequestLoadState from './LeaveRequestLoadState'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  // Only the id is read from the selected row; the request is fetched fresh so the
  // modal opens on the server's current state rather than the possibly-stale list row.
  selectedRequest: LeaveRequest | null
  // Heading text; defaults to "{type} leave" from the fetched (or selected) request.
  heading?: React.ReactNode
  // When set, renders a back arrow that returns to the previous modal.
  onBack?: () => void
  // Renders the modal body once the request has loaded, receiving the fetched request.
  children: (request: LeaveRequest) => React.ReactNode
}

// The shared scaffold for the view/accept/edit leave modals: it derives the id from
// the selected row, fetches the request detail, renders the heading and the
// loading/error/not-found block, then hands the loaded request to its children.
const LeaveRequestModalShell: React.FC<Props> = ({
  selectedRequest,
  heading,
  onBack,
  children
}) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  const rawId = selectedRequest ? Number(selectedRequest.id) : NaN
  const id = Number.isInteger(rawId) ? rawId : null
  const { data: request, isLoading, error } = useLeaveRequestDetail(id)

  return (
    <div className="modal-form">
      {onBack && (
        <button className="modal-back" onClick={onBack} aria-label={t.goBack}>
          ‹
        </button>
      )}
      <h2>
        {heading ?? `${request?.type ?? selectedRequest?.type ?? 'Vacation'} ${t.leaveSuffix}`}
      </h2>

      <LeaveRequestLoadState isLoading={isLoading} error={error} loaded={!!request} />

      {request && children(request)}
    </div>
  )
}

export default LeaveRequestModalShell
