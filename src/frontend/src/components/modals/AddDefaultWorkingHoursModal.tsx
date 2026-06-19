import React, { useState } from 'react'
import type { DefaultHoursDay } from '../../services/api'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'
import { useWeekdayLabels, WEEKDAYS } from '../../hooks/useWeekdayLabels'
import { useEmployeeDefaultHours, useDepartmentDefaultHours } from '../../hooks/useDefaultHours'
import {
  useSetEmployeeDefaultHours,
  useSetDepartmentDefaultHours
} from '../../hooks/useDefaultHoursMutations'
import {
  daysFromSchedule,
  emptySchedule,
  isScheduleValid,
  scheduleFromDays,
  type Schedule
} from '../../utils/defaultHours'

interface Props {
  closeModal: () => void
  isEditMode?: boolean
  // Editing a department's default hours (vs an employee's) — changes the wording
  // and which endpoint the editor reads/writes.
  isDepartment?: boolean
  // The employee whose default hours to edit (employee mode). Set for both the
  // add-new-employee and edit flows; null disables saving.
  employeeId?: number | null
  // The department whose default hours to edit (department mode).
  departmentId?: number | null
}

const AddDefaultWorkingHoursModal: React.FC<Props> = ({
  closeModal,
  isEditMode,
  isDepartment,
  employeeId,
  departmentId
}) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  // Display the day in the active language without breaking the hardcoded keys.
  const dayLabels = useWeekdayLabels()

  // Load the saved schedule for whichever target this editor is for. Both hooks are
  // called (hooks can't be conditional); the off-mode one is held idle with a null id.
  const employeeQuery = useEmployeeDefaultHours(isDepartment ? null : (employeeId ?? null))
  const departmentQuery = useDepartmentDefaultHours(isDepartment ? (departmentId ?? null) : null)
  const query = isDepartment ? departmentQuery : employeeQuery

  const setEmployeeHours = useSetEmployeeDefaultHours()
  const setDepartmentHours = useSetDepartmentDefaultHours()
  const saving = setEmployeeHours.isPending || setDepartmentHours.isPending

  // Start empty; replaced with the saved schedule once it loads (a freshly created
  // employee may already carry hours inherited from their department).
  const [schedule, setSchedule] = useState<Schedule>(emptySchedule)
  const [error, setError] = useState<string | null>(null)

  // Seed the editor from the fetched schedule by adjusting state during render (the
  // React-recommended alternative to an effect). React Query keeps `data` stable
  // between renders, so the guard reseeds only when the loaded schedule changes —
  // not on every keystroke, leaving the user's edits intact.
  const [seededFrom, setSeededFrom] = useState<DefaultHoursDay[] | null>(null)
  const loaded = query.data
  if (loaded && loaded !== seededFrom) {
    setSeededFrom(loaded)
    setSchedule(scheduleFromDays(loaded))
  }

  const handleAddShift = (day: string) => {
    setSchedule(prev => ({
      ...prev,
      // One interval per day (the backend stores at most one); the "+" is hidden
      // once a day has a shift, so this only ever adds the first.
      [day]: prev[day].length > 0 ? prev[day] : [{ start: '', end: '' }]
    }))
  }

  const handleRemoveShift = (day: string, shiftIndex: number) => {
    setSchedule(prev => ({
      ...prev,
      [day]: prev[day].filter((_, index) => index !== shiftIndex)
    }))
  }

  const handleTimeChange = (
    day: string,
    shiftIndex: number,
    field: 'start' | 'end',
    value: string
  ) => {
    setSchedule(prev => ({
      ...prev,
      [day]: prev[day].map((shift, index) =>
        index === shiftIndex ? { ...shift, [field]: value } : shift
      )
    }))
  }

  const handleSave = () => {
    setError(null)
    if (!isScheduleValid(schedule)) {
      setError(t.errInvalidWorkingHours)
      return
    }
    const days = daysFromSchedule(schedule)
    const onSuccess = () => closeModal()
    const onError = (err: Error) => setError(err.message)

    if (isDepartment) {
      if (departmentId == null) return
      setDepartmentHours.mutate({ departmentId, days }, { onSuccess, onError })
    } else {
      if (employeeId == null) return
      setEmployeeHours.mutate({ employeeId, days }, { onSuccess, onError })
    }
  }

  return (
    <div className="modal-form">
      <h2 style={{ lineHeight: '1.2' }}>
        {isDepartment ? (
          t.editDepartmentWorkingHours
        ) : (
          <>
            {isEditMode ? t.editDefaultWorkingHours : t.addDefaultWorkingHours}
            <br />
            {t.workingHoursSuffix}
          </>
        )}
      </h2>

      {query.isError && <p className="form-error form-error-block">{query.error.message}</p>}

      <div
        style={{ maxHeight: '400px', overflowY: 'auto', paddingRight: '8px', marginBottom: '24px' }}
      >
        {WEEKDAYS.map(day => (
          <div key={day} style={{ marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <h4 style={{ fontSize: '15px', fontWeight: 500, margin: 0 }}>{dayLabels[day]}</h4>
              {/* At most one interval per day, so the add button hides once a shift exists. */}
              {schedule[day].length === 0 && (
                <button
                  className="btn-tiny-pill"
                  onClick={() => handleAddShift(day)}
                  style={{ backgroundColor: '#5932EA', fontSize: '14px', padding: '2px 12px' }}
                >
                  +
                </button>
              )}
            </div>

            {schedule[day].map((shift, index) => (
              <div
                key={index}
                style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}
              >
                <input
                  type="time"
                  value={shift.start}
                  onChange={e => handleTimeChange(day, index, 'start', e.target.value)}
                  style={{ padding: '4px 8px', width: '100px' }}
                />

                <span style={{ fontWeight: 'bold' }}>-</span>

                <input
                  type="time"
                  value={shift.end}
                  onChange={e => handleTimeChange(day, index, 'end', e.target.value)}
                  style={{ padding: '4px 8px', width: '100px' }}
                />

                <button
                  className="btn-secondary"
                  onClick={() => handleRemoveShift(day, index)}
                  style={{
                    padding: '4px 12px',
                    minWidth: 'auto',
                    border: 'none',
                    cursor: 'pointer'
                  }}
                >
                  -
                </button>
              </div>
            ))}
          </div>
        ))}
      </div>

      {error && <p className="form-error">{error}</p>}

      <button
        className="primary-btn full-width"
        onClick={handleSave}
        disabled={saving || query.isLoading}
      >
        {isDepartment ? t.editDepartmentWithHours : isEditMode ? t.editWithHours : t.addWithHours}
      </button>
    </div>
  )
}

export default AddDefaultWorkingHoursModal
