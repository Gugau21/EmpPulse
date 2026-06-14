import React, { useMemo, useRef, useState } from 'react'
import type { LeaveRequest, ModalType } from '../../types'
import type { LeaveRequestCreatePayload } from '../../services/api'
import { useAuth } from '../../context/useAuth'
import { useEmployeesList } from '../../hooks/useEmployeesList'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useCreateLeaveRequest } from '../../hooks/useLeaveRequestMutations'
import { useOutsideClick } from '../../hooks/useOutsideClick'
import LeaveTypeSelects from './LeaveTypeSelects'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  activeModal: ModalType
  closeModal: () => void
}

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

interface EmployeePickerProps {
  selectedId: number | null
  onSelect: (id: number | null) => void
  t: any
}

const EmployeePicker: React.FC<EmployeePickerProps> = ({ selectedId, onSelect, t }) => {
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
      {t.employee}
      <input
        type="text"
        placeholder={t.typeAName}
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
            <div className="autocomplete-empty">{t.noMatchingEmp}</div>
          )}
        </div>
      )}
    </label>
  )
}

const AddLeaveModal: React.FC<Props> = ({ activeModal, closeModal }) => {
  const { currentUser, isOwner, isAdmin } = useAuth()
  const { language } = useLanguage()
  const t = translations[language].modals

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
      setValidationError(t.errSelectEmployee)
      return
    }
    if (!from || !till) {
      setValidationError(t.errDatesRequired)
      return
    }
    if (from > till) {
      setValidationError(t.errFromAfterTill)
      return
    }
    if (!canSetPastDates && minDate && from < minDate) {
      setValidationError(t.errPastStart)
      return
    }
    // Reason is mandatory for personal leave for every role; optional otherwise.
    if (leaveType === 'Personal' && !reason.trim()) {
      setValidationError(t.errReasonRequired)
      return
    }
    // The target is the caller themselves (self flow) or the picked employee.
    const targetEmployeeId = isMine
      ? (currentUser?.employeeProfile?.employeeId ?? null)
      : employeeId
    if (targetEmployeeId == null) {
      setValidationError(t.errNoEmpProfile)
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
      <h2>{isMine ? t.addRequest : t.createRequest}</h2>

      {!isMine && <EmployeePicker selectedId={employeeId} onSelect={setEmployeeId} t={t} />}

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
              ? `${t.youHave}${vacationDaysLeft}${t.daysLeft}`
              : `${selectedEmployee?.name} ${selectedEmployee?.surname}${t.has}${vacationDaysLeft}${t.daysLeft}`
            : t.vacationBalanceUnavailable}
        </div>
      )}

      <label>
        {t.fromLabel}
        <input type="date" min={minDate} value={from} onChange={e => setFrom(e.target.value)} />
      </label>

      <label>
        {t.tillLabel}
        <input type="date" min={minDate} value={till} onChange={e => setTill(e.target.value)} />
      </label>

      <label>
        {t.reason}{leaveType === 'Personal' ? ' *' : ''}
        <textarea
          rows={2}
          maxLength={300}
          value={reason}
          onChange={e => setReason(e.target.value)}
        />
        <span className="char-counter">{300 - reason.length}{t.charsLeft}</span>
      </label>

      {(validationError || createLeave.error) && (
        <p className="form-error">{validationError ?? createLeave.error?.message}</p>
      )}

      <button
        className="primary-btn full-width"
        onClick={handleSubmit}
        disabled={createLeave.isPending}
      >
        {t.createRequestBtn}
      </button>
    </div>
  )
}

export default AddLeaveModal