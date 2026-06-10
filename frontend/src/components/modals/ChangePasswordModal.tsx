import React from 'react'
import type { OpenModal } from '../../types'

interface Props {
  openModal: OpenModal
}

const ChangePasswordModal: React.FC<Props> = ({ openModal }) => {
  return (
    <div className="modal-form">
      <h2>Change password</h2>
      <label>
        Old password
        <input type="password" placeholder=" " />
      </label>
      <label>
        New password
        <input type="password" placeholder=" " />
      </label>
      <label>
        Repeat new password
        <input type="password" placeholder=" " />
      </label>
      <button 
        className="primary-btn full-width" 
        onClick={() => openModal('CHANGE_PASSWORD')}
      >
        change password
      </button>
    </div>
  )
}

export default ChangePasswordModal