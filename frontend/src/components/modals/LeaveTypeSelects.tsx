import React from 'react'
import type { LeaveRequest } from '../../types'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

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
}) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  return (
    <>
      <label>
        {t.typeOfLeaveByPayment}
        <select value={paymentType} onChange={e => onPaymentTypeChange(e.target.value)}>
          {/* Values must remain exact strings for backend state mapping */}
          <option value="Paid">{t.paid}</option>
          <option value="Unpaid">{t.unpaid}</option>
        </select>
      </label>

      <label>
        {t.typeOfLeave}
        <select
          value={leaveType}
          onChange={e => onLeaveTypeChange(e.target.value as LeaveRequest['type'])}
        >
          <option value="Vacation">{t.optVacation}</option>
          <option value="Sick">{t.optSick}</option>
          <option value="Personal">{t.optPersonal}</option>
        </select>
      </label>
    </>
  )
}

export default LeaveTypeSelects
