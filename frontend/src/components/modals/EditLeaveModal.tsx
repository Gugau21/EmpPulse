import React, { useState } from 'react'
import type { LeaveRequest } from '../../types'
import { LEAVE_TYPE_TO_API } from '../../services/api'
import { useAuth } from '../../context/useAuth'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useUpdateLeaveRequest, useModifyLeaveRequest } from '../../hooks/useLeaveRequestMutations'
import LeaveTypeSelects from './LeaveTypeSelects'
import LeaveRequestModalShell from './LeaveRequestModalShell'

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
  // Present only when this form was opened from the accept/reject modal; renders a
  // back arrow that returns there.
  onBack?: () => void
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

// The request's fields are fetched from the API so the form opens on the server's
// current state. The form itself is a child that mounts only once the request has
// loaded, so its initial state seeds cleanly from the fetched values.
const EditLeaveModal: React.FC<Props> = ({ closeModal, selectedRequest, onBack }) => {
  return (
    <LeaveRequestModalShell
      selectedRequest={selectedRequest}
      heading="Edit request"
      onBack={onBack}
    >
      {request => <EditLeaveForm request={request} closeModal={closeModal} onBack={onBack} />}
    </LeaveRequestModalShell>
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
  const modifyLeave = useModifyLeaveRequest()

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
    ? currentUser?.employeeProfile?.vacationBalance
    : requestEmployee?.employeeProfile?.vacationBalance

  // An employee can't edit their own APPROVED request in place — the server only
  // accepts that as a modification request an admin then reviews. Pending requests,
  // and admins editing a request they oversee, go through the normal update.
  const isModification = isMine && request.status === 'APPROVED'
  const activeMutation = isModification ? modifyLeave : updateLeave

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
    const vars = {
      id: Number(request.id),
      payload: {
        type: LEAVE_TYPE_TO_API[leaveType],
        paid: paymentType === 'Paid',
        startDate: from,
        endDate: till,
        reason: reason.trim() || null
      }
    }
    activeMutation.mutate(vars, { onSuccess: () => closeModal() })
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

      {(validationError || activeMutation.error) && (
        <p className="form-error">{validationError ?? activeMutation.error?.message}</p>
      )}

      {isModification && (
        <div className="balance-hint">
          Editing an approved request submits the change for admin approval.
        </div>
      )}

      <button
        className="primary-btn full-width"
        onClick={handleSubmit}
        disabled={activeMutation.isPending}
      >
        {isModification
          ? 'request change'
          : onBack
            ? 'edit and auto approve request'
            : 'edit request'}
      </button>
    </>
  )
}

export default EditLeaveModal
