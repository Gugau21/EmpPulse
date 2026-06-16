import React, { useState } from 'react'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

const ResetPasswordPage: React.FC = () => {
  const { language } = useLanguage()
  const t = translations[language].resetPasswordPage
  const [password, setPassword] = useState('')
  const [repeatPassword, setRepeatPassword] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Functionality omitted for now; UI layer only
  }

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h2 className="auth-heading">{t.title}</h2>

        <form onSubmit={handleSubmit} className="auth-form">
          <label className="auth-input-label">
            {t.newPasswordLabel}
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </label>

          <label className="auth-input-label">
            {t.repeatPasswordLabel}
            <input
              type="password"
              value={repeatPassword}
              onChange={e => setRepeatPassword(e.target.value)}
              required
            />
          </label>

          <button type="submit" className="primary-btn auth-submit-btn">
            {t.resetBtn}
          </button>
        </form>
      </div>
    </div>
  )
}

export default ResetPasswordPage
