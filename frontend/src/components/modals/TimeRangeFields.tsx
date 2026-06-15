import React from 'react'

interface Props {
  from: string
  till: string
  onFromChange: (value: string) => void
  onTillChange: (value: string) => void
}

// The From/Till time inputs shared by the log-hours and edit-hours modals.
const TimeRangeFields: React.FC<Props> = ({ from, till, onFromChange, onTillChange }) => (
  <>
    <label>
      From
      <input type="time" value={from} onChange={e => onFromChange(e.target.value)} />
    </label>

    <label>
      Till
      <input type="time" value={till} onChange={e => onTillChange(e.target.value)} />
    </label>
  </>
)

export default TimeRangeFields
