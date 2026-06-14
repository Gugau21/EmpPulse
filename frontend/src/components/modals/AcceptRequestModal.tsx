import React from 'react'
import type { LeaveRequest, OpenModal } from '../../types'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
  openModal: OpenModal
}

const AcceptRequestModal: React.FC<Props> = ({ closeModal, selectedRequest, openModal }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  // `dateRange` is the display-formatted "dd.mm.yyyy - dd.mm.yyyy"; split it back
  // into the From/Till the summary shows.
  const [from, till] = selectedRequest?.dateRange?.split(' - ') ?? []

  return (
    <div className="modal-form">
      <h2>{selectedRequest?.type ?? 'Vacation'} {t.leaveSuffix}</h2>
      <div className="request-summary-box">
        <h3>{selectedRequest?.employeeName}</h3>
        <p>{t.from} {from}</p>
        <p>{t.till} {till}</p>
        <p>{t.payment} {selectedRequest?.paid ? t.paid : t.unpaid}</p>
        <label>
          {t.reason}<div className="reason-box">{selectedRequest?.reason ?? ''}</div>
        </label>
      </div>
      <div className="modal-actions">
        <button className="primary-btn full-width" onClick={closeModal}>
          {t.acceptRequest}
        </button>
        <button className="primary-btn danger full-width" onClick={closeModal}>
          {t.rejectRequest}
        </button>
      </div>
      <button
        className="link-btn"
        // Reopen this accept/reject modal as the back target so the edit form
        // can return here.
        onClick={() =>
          openModal('EDIT_LEAVE_FORM', undefined, selectedRequest ?? undefined, 'ACCEPT_REQUEST')
        }
      >
        {t.editRequest}
      </button>
    </div>
  )
}

export default AcceptRequestModal