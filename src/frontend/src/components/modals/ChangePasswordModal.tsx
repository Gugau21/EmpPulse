import React, { useState } from 'react'
import type { OpenModal } from '../../types'
import { useChangePassword } from '../../hooks/useChangePassword'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  openModal: OpenModal
}

const ChangePasswordModal: React.FC<Props> = ({ openModal }) => {
  const { language } = useLanguage()
  const t = translations[language].modals
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')
  const [validationError, setValidationError] = useState<string | null>(null)
  const changePassword = useChangePassword()

  const handleSubmit = () => {
    setValidationError(null)
    if (!currentPassword || !newPassword || !confirmNewPassword) {
      setValidationError(t.errAllPwdRequired)
      return
    }
    if (newPassword !== confirmNewPassword) {
      setValidationError(t.errPwdMismatch)
      return
    }
    if (newPassword === currentPassword) {
      setValidationError(t.errPwdSameAsOld)
      return
    }
    changePassword.mutate(
      { currentPassword, newPassword, confirmNewPassword },
      // On success the form modal is swapped for the confirmation modal; the
      // server has already changed the password and dropped other sessions.
      { onSuccess: () => openModal('CHANGE_PASSWORD_SUCCESS') }
    )
  }

  return (
    <div className="modal-form">
      <h2>{t.changePassword}</h2>
      <label>
        {t.oldPassword}
        <input
          type="password"
          placeholder=" "
          value={currentPassword}
          onChange={e => setCurrentPassword(e.target.value)}
        />
      </label>
      <label>
        {t.newPassword}
        <input
          type="password"
          placeholder=" "
          value={newPassword}
          onChange={e => setNewPassword(e.target.value)}
        />
      </label>
      <label>
        {t.repeatNewPassword}
        <input
          type="password"
          placeholder=" "
          value={confirmNewPassword}
          onChange={e => setConfirmNewPassword(e.target.value)}
        />
      </label>

      {(validationError || changePassword.error) && (
        <p className="form-error">{validationError ?? changePassword.error?.message}</p>
      )}

      <button
        className="primary-btn full-width"
        onClick={handleSubmit}
        disabled={changePassword.isPending}
      >
        {t.changePassword}
      </button>
    </div>
  )
}

export default ChangePasswordModal
