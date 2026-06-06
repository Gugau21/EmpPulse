import React from 'react'
import type { Employee } from '../../types'

interface Props {
  closeModal: () => void
  selectedEmployee: Employee | null
}

const LogHoursModal: React.FC<Props> = ({ closeModal, selectedEmployee }) => (
  <div className="modal-form">
    <h2>Log hours</h2>
    <h4 className="log-hours-name">{selectedEmployee?.name || 'Employee'}</h4>
    <label>
      Type
      <select>
        <option>Regular</option>
        <option>Overtime</option>
      </select>
    </label>
    <label>
      From
      <input type="time" defaultValue="09:00" />
    </label>
    <label>
      Till
      <input type="time" defaultValue="17:00" />
    </label>
    <button className="primary-btn full-width" onClick={closeModal}>
      + add hours
    </button>
  </div>
)

export default LogHoursModal
