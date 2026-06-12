import React, { useState } from 'react'
import type { LeaveRequest } from '../../types'
import type { ApiLeaveType } from '../../services/api'
import { useAuth } from '../../context/useAuth'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useUpdateLeaveRequest } from '../../hooks/useLeaveRequestMutations'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
  // Present only when this form was opened from the accept/reject modal; renders a
  // back arrow that returns there.
  onBack?: () => void
}

// The UI shows display-cased leave types; the API takes the uppercase enum.
const LEAVE_TYPE_TO_API: Record<LeaveRequest['type'], ApiLeaveType> = {
  Vacation: 'VACATION',
  Sick: 'SICK',
  Personal: 'PERSONAL'
}

// Stored leave dates are display-formatted as `dd.mm.yyyy`, but a native
// <input type="date"> only accepts ISO `yyyy-mm-dd`. Flip the parts so the
// existing range pre-fills the date pickers; an unparseable value yields '',
// leaving the input blank rather than rejecting the whole form.
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const [dd, mm, yyyy] = dateStr.split('.')
  if (dd && mm && yyyy) return `${yyyy}-${mm}-${dd}`
  return ''
}

const EditLeaveModal: React.FC<Props> = ({ closeModal, selectedRequest, onBack }) => {
  const [startStr, endStr] = selectedRequest?.dateRange?.split(' - ') ?? []

  const [paymentType, setPaymentType] = useState('Paid')
  const [leaveType, setLeaveType] = useState(selectedRequest?.type ?? 'Vacation')
  const [from, setFrom] = useState(formatDate(startStr))
  const [till, setTill] = useState(formatDate(endStr))
  const [reason, setReason] = useState(selectedRequest?.reason ?? '')
  const [validationError, setValidationError] = useState<string | null>(null)
  const updateLeave = useUpdateLeaveRequest()

  // The vacation balance shown to the editor. When the request is the caller's
  // own, the balance already lives on currentUser; otherwise only an admin/owner
  // may fetch the target employee's detail (an employee can't read /api/users/{id}
  // for someone else), so the lookup is gated on that.
  const { currentUser, isOwner, isAdmin } = useAuth()
  const requestEmployeeId = selectedRequest?.employeeId ?? null
  const isOwnRequest =
    currentUser?.employeeProfile != null &&
    currentUser.employeeProfile.employeeId === requestEmployeeId
  const { data: requestEmployee } = useUserDetail(
    !isOwnRequest && (isOwner || isAdmin) ? requestEmployeeId : null
  )
  const vacationDaysLeft = isOwnRequest
    ? currentUser?.employeeProfile?.yearlyVacationBalance
    : requestEmployee?.employeeProfile?.yearlyVacationBalance

  const handleSubmit = () => {
    setValidationError(null)
    if (!selectedRequest) {
      // Shouldn't happen (the modal is opened from an existing request row),
      // but without an id there is nothing to update.
      closeModal()
      return
    }
    if (!from || !till) {
      setValidationError('Both dates are required.')
      return
    }
    // ISO yyyy-mm-dd strings compare correctly as plain strings.
    if (from > till) {
      setValidationError('The "From" date must not be after the "Till" date.')
      return
    }
    if (leaveType === 'Personal' && !reason.trim()) {
      setValidationError('A reason is required for personal leave.')
      return
    }
    // The API expects a numeric id; bail out clearly rather than sending
    // `/api/leave-requests/NaN` if the id is ever non-numeric.
    const id = Number(selectedRequest.id)
    if (!Number.isInteger(id)) {
      setValidationError('This leave request has an invalid identifier and cannot be updated.')
      return
    }
    updateLeave.mutate(
      {
        id,
        payload: {
          type: LEAVE_TYPE_TO_API[leaveType],
          paid: paymentType === 'Paid',
          startDate: from,
          endDate: till,
          reason: reason.trim() || null
        }
      },
      { onSuccess: () => closeModal() }
    )
  }

  return (
    <div className="modal-form">
      {onBack && (
        <button className="modal-back" onClick={onBack} aria-label="Go back">
          ‹
        </button>
      )}
      <h2>{'Edit request'}</h2>

      <label>
        Type of leave (by payment)
        <select value={paymentType} onChange={e => setPaymentType(e.target.value)}>
          <option value="Paid">Paid</option>
          <option value="Unpaid">Unpaid</option>
        </select>
      </label>

      <label>
        Type of leave
        <select
          value={leaveType}
          onChange={e => setLeaveType(e.target.value as LeaveRequest['type'])}
        >
          <option value="Vacation">Vacation</option>
          <option value="Sick">Sick</option>
          <option value="Personal">Personal</option>
        </select>
      </label>

      {leaveType === 'Vacation' && (
        <div className="balance-hint">
          {vacationDaysLeft != null
            ? `You have ${vacationDaysLeft} vacation days left`
            : 'Vacation balance unavailable'}
        </div>
      )}

      <label>
        From
        <input type="date" value={from} onChange={e => setFrom(e.target.value)} />
      </label>

      <label>
        Till
        <input type="date" value={till} onChange={e => setTill(e.target.value)} />
      </label>

      <label>
        Reason
        <textarea rows={2} value={reason} onChange={e => setReason(e.target.value)}></textarea>
      </label>

      {(validationError || updateLeave.error) && (
        <p className="form-error">{validationError ?? updateLeave.error?.message}</p>
      )}

      <button
        className="primary-btn full-width"
        onClick={handleSubmit}
        disabled={updateLeave.isPending}
      >
        {onBack ? 'edit and auto approve request' : 'edit request'}
      </button>
    </div>
  )
}

export default EditLeaveModal
