import React from 'react'
import type { LeaveRequest } from '../../types'

interface Props {
  paymentType: string
  onPaymentTypeChange: (value: string) => void
  leaveType: LeaveRequest['type']
  onLeaveTypeChange: (value: LeaveRequest['type']) => void
}

// The paid/unpaid and leave-kind pickers shared by the create (AddLeaveModal) and
// edit (EditLeaveModal) forms. State lives in the parent form; this only renders
// the two <select>s so both forms offer the same options in the same order.
const LeaveTypeSelects: React.FC<Props> = ({
  paymentType,
  onPaymentTypeChange,
  leaveType,
  onLeaveTypeChange
}) => (
  <>
    <label>
      Type of leave (by payment)
      <select value={paymentType} onChange={e => onPaymentTypeChange(e.target.value)}>
        <option value="Paid">Paid</option>
        <option value="Unpaid">Unpaid</option>
      </select>
    </label>

    <label>
      Type of leave
      <select
        value={leaveType}
        onChange={e => onLeaveTypeChange(e.target.value as LeaveRequest['type'])}
      >
        <option value="Vacation">Vacation</option>
        <option value="Sick">Sick</option>
        <option value="Personal">Personal</option>
      </select>
    </label>
  </>
)

export default LeaveTypeSelects
