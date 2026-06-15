import React from 'react'
import type { Employee, LoggedHours } from '../../types'
import { useDeleteLoggedHours, useUpdateLoggedHours } from '../../hooks/useLoggedHoursMutations'
import TimeRangeFields from './TimeRangeFields'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  closeModal: () => void
  selectedEmployee: Employee | null
  selectedLoggedHours: LoggedHours | null
}

// Edits an existing interval's times or deletes it. The day is fixed (the date
// field is shown but disabled); same merge-on-save behaviour as logging new hours.
const EditHoursModal: React.FC<Props> = ({ closeModal, selectedEmployee, selectedLoggedHours }) => {
  const [from, setFrom] = useState(selectedLoggedHours?.startTime ?? '09:00')
  const [till, setTill] = useState(selectedLoggedHours?.endTime ?? '17:00')
  const [validationError, setValidationError] = useState<string | null>(null)
  const updateLoggedHours = useUpdateLoggedHours()
  const deleteLoggedHours = useDeleteLoggedHours()

  // The modal only opens from a row that carries its interval, so this is defensive.
  if (!selectedLoggedHours) return null

  const employeeId = Number(selectedEmployee?.id)
  const isBusy = updateLoggedHours.isPending || deleteLoggedHours.isPending

  const handleSubmit = () => {
    setValidationError(null)
    if (!from || !till) {
      setValidationError('From and till are both required.')
      return
    }
    // "HH:mm" strings compare correctly as plain strings.
    if (from >= till) {
      setValidationError('The "From" time must be before the "Till" time.')
      return
    }
    updateLoggedHours.mutate(
      {
        employeeId,
        loggedHoursId: selectedLoggedHours.id,
        // The day can't move from the edit modal, so the original date is sent back.
        payload: { date: selectedLoggedHours.date, startTime: from, endTime: till }
      },
      { onSuccess: () => closeModal() }
    )
  }

  const handleDelete = () => {
    deleteLoggedHours.mutate(
      { employeeId, loggedHoursId: selectedLoggedHours.id },
      { onSuccess: () => closeModal() }
    )
  }

  return (
    <div className="modal-form">
      <h2>Edit hours</h2>
      <h4 className="log-hours-name">
        {selectedEmployee?.name || 'Employee'} {selectedEmployee?.surname || ''}
      </h4>

      <label>
        Date
        <input type="date" value={selectedLoggedHours.date} disabled />
      </label>

      <TimeRangeFields from={from} till={till} onFromChange={setFrom} onTillChange={setTill} />

      {(validationError || updateLoggedHours.error || deleteLoggedHours.error) && (
        <p className="form-error">
          {validationError ?? updateLoggedHours.error?.message ?? deleteLoggedHours.error?.message}
        </p>
      )}

      <button className="primary-btn full-width" onClick={handleSubmit} disabled={isBusy}>
        edit hours
      </button>

      <button className="primary-btn danger full-width" onClick={handleDelete} disabled={isBusy}>
        delete interval
      </button>
    </div>
  )
}

export default EditHoursModal
