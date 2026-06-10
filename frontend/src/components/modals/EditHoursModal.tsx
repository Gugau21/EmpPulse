import React from 'react'
import type { Employee } from '../../types'

interface Props {
  closeModal: () => void
  selectedEmployee: Employee | null
}

const EditHoursModal: React.FC<Props> = ({ closeModal, selectedEmployee }) => (
  <div className="modal-form">
    <h2>Edit hours</h2>
    <h4 className="log-hours-name">
      {selectedEmployee?.name || 'Employee'} {selectedEmployee?.surname || ''}
    </h4>

    <label>
      From
      <input type="time" defaultValue="09:00" />
    </label>

    <label>
      Till
      <input type="time" defaultValue="17:00" />
    </label>

    <button className="primary-btn full-width" onClick={closeModal}>
      edit hours
    </button>
  </div>
)

export default EditHoursModal
