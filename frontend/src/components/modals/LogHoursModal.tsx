import React, { useState } from 'react'
import type { Employee } from '../../types'
import { useCreateLoggedHours } from '../../hooks/useLoggedHoursMutations'
import { todayISO } from '../../utils/date'
import TimeRangeFields from './TimeRangeFields'

interface Props {
  closeModal: () => void
  selectedEmployee: Employee | null
}

// Logs a working interval for the selected employee. The backend merges it with
// any overlapping/adjacent interval already on that day; the table refetches via
// the mutation's cache invalidation, so the merged result shows up automatically.
const LogHoursModal: React.FC<Props> = ({ closeModal, selectedEmployee }) => {
  const [date, setDate] = useState(todayISO())
  const [from, setFrom] = useState('09:00')
  const [till, setTill] = useState('17:00')
  const [validationError, setValidationError] = useState<string | null>(null)
  const createLoggedHours = useCreateLoggedHours()

  const handleSubmit = () => {
    setValidationError(null)
    if (!date || !from || !till) {
      setValidationError('Date, from and till are all required.')
      return
    }
    // "HH:mm" strings compare correctly as plain strings.
    if (from >= till) {
      setValidationError('The "From" time must be before the "Till" time.')
      return
    }
    createLoggedHours.mutate(
      {
        employeeId: Number(selectedEmployee?.id),
        payload: { date, startTime: from, endTime: till }
      },
      { onSuccess: () => closeModal() }
    )
  }

  return (
    <div className="modal-form">
      <h2>Log hours</h2>
      <h4 className="log-hours-name">
        {selectedEmployee?.name || 'Employee'} {selectedEmployee?.surname || ''}
      </h4>

      <label>
        Date
        <input type="date" value={date} max={todayISO()} onChange={e => setDate(e.target.value)} />
      </label>

      <TimeRangeFields from={from} till={till} onFromChange={setFrom} onTillChange={setTill} />

      {(validationError || createLoggedHours.error) && (
        <p className="form-error">{validationError ?? createLoggedHours.error?.message}</p>
      )}

      <button
        className="primary-btn full-width"
        onClick={handleSubmit}
        disabled={createLoggedHours.isPending}
      >
        + add hours
      </button>
    </div>
  )
}

export default LogHoursModal
