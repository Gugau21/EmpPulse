import React, { useState } from 'react'
import type { Employee } from '../../types'
import { useBonusVacationDays, useUpdateBonusVacationDays } from '../../hooks/useBonusVacationDays'

interface Props {
  closeModal: () => void
  selectedEmployee: Employee | null
}

// Fetches the employee's current bonus days (GET) and renders the form once
// they're available, so the input pre-fills from the loaded value without an
// effect syncing props into state. Keyed on the response in the inner form.
const AddBonusVacationDaysModal: React.FC<Props> = ({ closeModal, selectedEmployee }) => {
  const userId = selectedEmployee?.id ? Number(selectedEmployee.id) : null
  const { data: bonus, isLoading, error } = useBonusVacationDays(userId)

  if (userId === null || isLoading || !bonus) {
    return (
      <div className="modal-form">
        <h2>Bonus vacation days</h2>
        {error ? <p className="form-error">{error.message}</p> : <p>Loading…</p>}
      </div>
    )
  }

  return (
    <BonusDaysForm
      key={`${userId}-${bonus.year}`}
      userId={userId}
      employee={selectedEmployee}
      year={bonus.year}
      initialDays={bonus.days}
      closeModal={closeModal}
    />
  )
}

interface FormProps {
  userId: number
  employee: Employee | null
  year: number
  initialDays: number
  closeModal: () => void
}

const BonusDaysForm: React.FC<FormProps> = ({
  userId,
  employee,
  year,
  initialDays,
  closeModal
}) => {
  // Kept as a string so the field can be cleared; non-digits are stripped so the
  // value stays a non-negative integer (matching the server's days >= 0 rule).
  const [days, setDays] = useState(String(initialDays))
  const update = useUpdateBonusVacationDays()

  const handleSubmit = () => {
    const parsed = days.trim() === '' ? 0 : Number(days)
    update.mutate({ userId, payload: { year, days: parsed } }, { onSuccess: () => closeModal() })
  }

  return (
    <div className="modal-form">
      <h2>Bonus vacation days</h2>
      <h4 className="log-hours-name">
        {employee?.name || 'Employee'} {employee?.surname || ''}
      </h4>

      <label>
        Bonus days for {year}
        <input
          type="text"
          inputMode="numeric"
          value={days}
          onChange={e => setDays(e.target.value.replace(/\D/g, ''))}
        />
      </label>

      {update.error && <p className="form-error">{update.error.message}</p>}

      <button className="primary-btn full-width" onClick={handleSubmit} disabled={update.isPending}>
        save bonus days
      </button>
    </div>
  )
}

export default AddBonusVacationDaysModal
