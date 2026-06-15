import React, { useState } from 'react'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'
import { useWeekdayLabels, WEEKDAYS } from '../../hooks/useWeekdayLabels'

interface Props {
  closeModal: () => void
  isEditMode?: boolean
  // Editing a department's default hours (vs an employee's) — changes the wording only.
  isDepartment?: boolean
}

type Shift = { start: string; end: string }
type Schedule = Record<string, Shift[]>

const AddDefaultWorkingHoursModal: React.FC<Props> = ({
  closeModal,
  isEditMode,
  isDepartment
}) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  // Display the day in the active language without breaking the hardcoded keys.
  const dayLabels = useWeekdayLabels()

  // Start with no shifts; the user adds intervals per day via the "+" button.
  const [schedule, setSchedule] = useState<Schedule>(() =>
    Object.fromEntries(WEEKDAYS.map(day => [day, []]))
  )

  const handleAddShift = (day: string) => {
    setSchedule(prev => ({
      ...prev,
      [day]: [...prev[day], { start: '', end: '' }]
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

      <div
        style={{ maxHeight: '400px', overflowY: 'auto', paddingRight: '8px', marginBottom: '24px' }}
      >
        {WEEKDAYS.map(day => (
          <div key={day} style={{ marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <h4 style={{ fontSize: '15px', fontWeight: 500, margin: 0 }}>{dayLabels[day]}</h4>
              <button
                className="btn-tiny-pill"
                onClick={() => handleAddShift(day)}
                style={{ backgroundColor: '#5932EA', fontSize: '14px', padding: '2px 12px' }}
              >
                +
              </button>
            </div>

            {schedule[day].map((shift, index) => (
              <div
                key={index}
                style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}
              >
                <span style={{ fontWeight: 500, width: '16px' }}>{index + 1})</span>

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

      <button className="primary-btn full-width" onClick={closeModal}>
        {isDepartment ? t.editDepartmentWithHours : isEditMode ? t.editWithHours : t.addWithHours}
      </button>
    </div>
  )
}

export default AddDefaultWorkingHoursModal
