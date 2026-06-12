import React, { useMemo, useRef, useState } from 'react'
import type { LeaveRequest, ModalType } from '../../types'
import type { LeaveRequestCreatePayload } from '../../services/api'
import { useAuth } from '../../context/useAuth'
import { useEmployeesList } from '../../hooks/useEmployeesList'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useCreateLeaveRequest } from '../../hooks/useLeaveRequestMutations'
import { useOutsideClick } from '../../hooks/useOutsideClick'
import LeaveTypeSelects from './LeaveTypeSelects'

interface Props {
  activeModal: ModalType
  closeModal: () => void
}

const REASON_MAX = 300

// The UI shows display-cased leave types; the API takes the lower-cased wire enum.
const LEAVE_TYPE_TO_API: Record<LeaveRequest['type'], LeaveRequestCreatePayload['type']> = {
  Vacation: 'vacation',
  Sick: 'sick',
  Personal: 'personal'
}

// Today as a local ISO `yyyy-mm-dd`, used as the `min` for the date pickers when
// the caller may not back-date (employees filing their own request).
function todayISO(): string {
  const now = new Date()
  const off = now.getTimezoneOffset()
  return new Date(now.getTime() - off * 60_000).toISOString().slice(0, 10)
}

// Type-ahead picker the admin/owner uses to choose whose request they're filing.
// The list it draws from is already scoped server-side: an admin receives only
// employees in the departments they oversee, the owner receives everyone — so no
// extra filtering by role is needed here, just narrowing by the typed text.
interface EmployeePickerProps {
  selectedId: number | null
  onSelect: (id: number | null) => void
}

const EmployeePicker: React.FC<EmployeePickerProps> = ({ selectedId, onSelect }) => {
  const { data: employees = [] } = useEmployeesList()
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLLabelElement>(null)

  useOutsideClick(ref, open, () => setOpen(false))

  const q = query.trim().toLowerCase()
  const matches = q
    ? employees.filter(e => `${e.name} ${e.surname}`.toLowerCase().includes(q))
    : employees

  return (
    <label className="employee-picker" ref={ref}>
      Employee
      <input
        type="text"
        placeholder="Type a name"
        value={query}
        maxLength={101}
        onFocus={() => setOpen(true)}
        onChange={e => {
          setQuery(e.target.value)
          setOpen(true)
          // Editing the text invalidates any prior pick until one is chosen again.
          if (selectedId !== null) onSelect(null)
        }}
      />
      {open && (
        <div className="autocomplete-menu">
          {matches.length ? (
            matches.map(e => (
              <button
                type="button"
                key={e.id}
                className="autocomplete-option"
                onClick={() => {
                  onSelect(Number(e.id))
                  setQuery(`${e.name} ${e.surname}`)
                  setOpen(false)
                }}
              >
                {e.name} {e.surname}
              </button>
            ))
          ) : (
            <div className="autocomplete-empty">No matching employees</div>
          )}
        </div>
      )}
    </label>
  )
}

const AddLeaveModal: React.FC<Props> = ({ activeModal, closeModal }) => {
  const { currentUser, isOwner, isAdmin } = useAuth()
  // ADD_LEAVE is the caller filing their own request ("mine"); CREATE_REQUEST is
  // the admin/owner flow, filing on behalf of another employee.
  const isMine = activeModal === 'ADD_LEAVE'

  const [paymentType, setPaymentType] = useState('Paid')
  const [leaveType, setLeaveType] = useState<LeaveRequest['type']>('Vacation')
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [from, setFrom] = useState('')
  const [till, setTill] = useState('')
  const [reason, setReason] = useState('')
  const [validationError, setValidationError] = useState<string | null>(null)
  const createLeave = useCreateLeaveRequest()

  // Admins and the owner may back-date a request; an employee filing their own
  // cannot, so their pickers are floored at today.
  const canSetPastDates = isOwner || isAdmin
  const minDate = useMemo(() => (canSetPastDates ? undefined : todayISO()), [canSetPastDates])

  // The selected employee's vacation balance (on-behalf flow); the caller's own
  // balance feeds the self flow. Treated as "days left" to match the profile page.
  const { data: selectedEmployee } = useUserDetail(isMine ? null : employeeId)
  const vacationDaysLeft = isMine
    ? currentUser?.employeeProfile?.yearlyVacationBalance
    : selectedEmployee?.employeeProfile?.yearlyVacationBalance

  // The balance hint only makes sense for a vacation request, and in the on-behalf
  // flow only once an employee (whose balance we show) has been chosen.
  const showVacationHint = leaveType === 'Vacation' && (isMine || employeeId !== null)

  const handleSubmit = () => {
    setValidationError(null)
    if (!isMine && employeeId === null) {
      setValidationError('Select an employee.')
      return
    }
    if (!from || !till) {
      setValidationError('Both dates are required.')
      return
    }
    if (from > till) {
      setValidationError('The "From" date must not be after the "Till" date.')
      return
    }
    if (!canSetPastDates && minDate && from < minDate) {
      setValidationError('Leave cannot start in the past.')
      return
    }
    // Reason is mandatory for personal leave for every role; optional otherwise.
    if (leaveType === 'Personal' && !reason.trim()) {
      setValidationError('A reason is required for personal leave.')
      return
    }
    // The target is the caller themselves (self flow) or the picked employee.
    const targetEmployeeId = isMine
      ? (currentUser?.employeeProfile?.employeeId ?? null)
      : employeeId
    if (targetEmployeeId == null) {
      setValidationError('You do not have an employee profile to file a request for.')
      return
    }
    createLeave.mutate(
      {
        employeeId: targetEmployeeId,
        type: LEAVE_TYPE_TO_API[leaveType],
        paid: paymentType === 'Paid',
        startDate: from,
        endDate: till,
        reason: reason.trim() || null
      },
      { onSuccess: () => closeModal() }
    )
  }

  return (
    <div className="modal-form">
      <h2>{isMine ? 'Add request' : 'Create request'}</h2>

      {!isMine && <EmployeePicker selectedId={employeeId} onSelect={setEmployeeId} />}

      <LeaveTypeSelects
        paymentType={paymentType}
        onPaymentTypeChange={setPaymentType}
        leaveType={leaveType}
        onLeaveTypeChange={setLeaveType}
      />

      {showVacationHint && (
        <div className="balance-hint">
          {vacationDaysLeft != null
            ? isMine
              ? `You have ${vacationDaysLeft} vacation days left`
              : `${selectedEmployee?.name} ${selectedEmployee?.surname} has ${vacationDaysLeft} vacation days left`
            : 'Vacation balance unavailable'}
        </div>
      )}

      <label>
        From
        <input type="date" min={minDate} value={from} onChange={e => setFrom(e.target.value)} />
      </label>

      <label>
        Till
        <input type="date" min={minDate} value={till} onChange={e => setTill(e.target.value)} />
      </label>

      <label>
        Reason{leaveType === 'Personal' ? ' *' : ''}
        <textarea
          rows={2}
          maxLength={REASON_MAX}
          value={reason}
          onChange={e => setReason(e.target.value)}
        />
        <span className="char-counter">{REASON_MAX - reason.length} characters left</span>
      </label>

      {(validationError || createLeave.error) && (
        <p className="form-error">{validationError ?? createLeave.error?.message}</p>
      )}

      <button
        className="primary-btn full-width"
        onClick={handleSubmit}
        disabled={createLeave.isPending}
      >
        + create request
      </button>
    </div>
  )
}

export default AddLeaveModal
