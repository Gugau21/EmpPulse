import React from 'react'
import type { OpenModal } from '../../types'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  openModal: OpenModal
}

const ChangePasswordModal: React.FC<Props> = ({ openModal }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  return (
    <div className="modal-form">
      <h2>{t.changePassword}</h2>
      <label>
        {t.oldPassword}
        <input type="password" placeholder=" " />
      </label>
      <label>
        {t.newPassword}
        <input type="password" placeholder=" " />
      </label>
      <label>
        {t.repeatNewPassword}
        <input type="password" placeholder=" " />
      </label>
      <button className="primary-btn full-width" onClick={() => openModal('CHANGE_PASSWORD')}>
        {t.changePassword}
      </button>
    </div>
  )
}

export default ChangePasswordModal