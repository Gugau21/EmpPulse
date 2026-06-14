import React, { useState } from 'react'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  closeModal: () => void
  isEditMode?: boolean
}

// DO NOT translate these string keys, as the state object relies on them.
const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

type Shift = { start: string; end: string }
type Schedule = Record<string, Shift[]>

const AddDefaultWorkingHoursModal: React.FC<Props> = ({ closeModal, isEditMode }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  // A helper map so we can display the day in the active language 
  // without breaking the hardcoded object keys.
  const dayLabels: Record<string, string> = {
    Monday: t.monday, Tuesday: t.tuesday, Wednesday: t.wednesday, 
    Thursday: t.thursday, Friday: t.friday, Saturday: t.saturday, Sunday: t.sunday
  }

  const [schedule, setSchedule] = useState<Schedule>({
    Monday: [{ start: '09:00', end: '17:00' }],
    Tuesday: [{ start: '09:00', end: '17:00' }],
    Wednesday: [{ start: '09:00', end: '17:00' }],
    Thursday: [{ start: '09:00', end: '17:00' }],
    Friday: [{ start: '09:00', end: '17:00' }],
    Saturday: [],
    Sunday: []
  })

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
        {isEditMode ? t.editDefaultWorkingHours : t.addDefaultWorkingHours}
        <br />
        {t.workingHoursSuffix}
      </h2>

      <div
        style={{ maxHeight: '400px', overflowY: 'auto', paddingRight: '8px', marginBottom: '24px' }}
      >
        {DAYS.map(day => (
          <div key={day} style={{ marginBottom: '16px' }}>
            <h4 style={{ fontSize: '15px', fontWeight: 500, marginBottom: '8px' }}>{dayLabels[day]}</h4>

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

            <div style={{ display: 'flex', justifyContent: 'center', width: '250px' }}>
              <button
                className="btn-tiny-pill"
                onClick={() => handleAddShift(day)}
                style={{ backgroundColor: '#5932EA', fontSize: '14px', padding: '2px 12px' }}
              >
                +
              </button>
            </div>
          </div>
        ))}
      </div>

      <button className="primary-btn full-width" onClick={closeModal}>
        {isEditMode ? t.saveChanges : t.addEmployeeBtnWorkingHours}
      </button>
    </div>
  )
}

export default AddDefaultWorkingHoursModal