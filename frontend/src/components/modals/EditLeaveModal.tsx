import React, { useState } from 'react'
import type { LeaveRequest } from '../../types'
import type { ApiLeaveType } from '../../services/api'
import { useAuth } from '../../context/useAuth'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useLeaveRequestDetail } from '../../hooks/useLeaveRequestDetail'
import { useUpdateLeaveRequest } from '../../hooks/useLeaveRequestMutations'
import LeaveTypeSelects from './LeaveTypeSelects'
import LeaveRequestLoadState from './LeaveRequestLoadState'

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

// Only the id is taken from the selected row; the request's own fields are fetched
// from the API so the form opens on the server's current state. The form itself is
// a child that mounts only once the request has loaded, so its initial state seeds
// cleanly from the fetched values.
const EditLeaveModal: React.FC<Props> = ({ closeModal, selectedRequest, onBack }) => {
  const rawId = selectedRequest ? Number(selectedRequest.id) : NaN
  const id = Number.isInteger(rawId) ? rawId : null
  const { data: request, isLoading, error } = useLeaveRequestDetail(id)

  return (
    <div className="modal-form">
      {onBack && (
        <button className="modal-back" onClick={onBack} aria-label="Go back">
          ‹
        </button>
      )}
      <h2>Edit request</h2>

      <LeaveRequestLoadState isLoading={isLoading} error={error} loaded={!!request} />
      {request && <EditLeaveForm request={request} closeModal={closeModal} onBack={onBack} />}
    </div>
  )
}

interface FormProps {
  request: LeaveRequest
  closeModal: () => void
  onBack?: () => void
}

const EditLeaveForm: React.FC<FormProps> = ({ request, closeModal, onBack }) => {
  const [startStr, endStr] = request.dateRange?.split(' - ') ?? []

  const [paymentType, setPaymentType] = useState(request.paid ? 'Paid' : 'Unpaid')
  const [leaveType, setLeaveType] = useState(request.type)
  const [from, setFrom] = useState(formatDate(startStr))
  const [till, setTill] = useState(formatDate(endStr))
  const [reason, setReason] = useState(request.reason ?? '')
  const [validationError, setValidationError] = useState<string | null>(null)
  const updateLeave = useUpdateLeaveRequest()

  // The vacation balance shown to the editor. When the request is the caller's
  // own, the balance already lives on currentUser; otherwise only an admin/owner
  // may fetch the target employee's detail (an employee can't read /api/users/{id}
  // for someone else), so the lookup is gated on that.
  const { currentUser, isOwner, isAdmin } = useAuth()
  const requestEmployeeId = request.employeeId
  // Same meaning as AddLeaveModal's isMine: is the request's subject the caller.
  // Here it must be derived from the request's employeeId (one edit modal serves
  // both own and others' requests), rather than from the modal type.
  const isMine =
    currentUser?.employeeProfile != null &&
    currentUser.employeeProfile.employeeId === requestEmployeeId
  const { data: requestEmployee } = useUserDetail(
    !isMine && (isOwner || isAdmin) ? requestEmployeeId : null
  )
  const vacationDaysLeft = isMine
    ? currentUser?.employeeProfile?.yearlyVacationBalance
    : requestEmployee?.employeeProfile?.yearlyVacationBalance

  const handleSubmit = () => {
    setValidationError(null)
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
    updateLeave.mutate(
      {
        id: Number(request.id),
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
    <>
      <LeaveTypeSelects
        paymentType={paymentType}
        onPaymentTypeChange={setPaymentType}
        leaveType={leaveType}
        onLeaveTypeChange={setLeaveType}
      />

      {leaveType === 'Vacation' && (
        <div className="balance-hint">
          {vacationDaysLeft != null
            ? isMine
              ? `You have ${vacationDaysLeft} vacation days left`
              : `${request.employeeName} has ${vacationDaysLeft} vacation days left`
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
    </>
  )
}

export default EditLeaveModal
