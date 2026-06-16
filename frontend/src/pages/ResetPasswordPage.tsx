import React, { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import { authService } from '../services/api'

const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate()
  const { language } = useLanguage()
  const t = translations[language].resetPasswordPage
  // The raw token comes from the emailed link (/reset-password?token=…).
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [repeatPassword, setRepeatPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!token) {
      setError(t.missingToken)
      return
    }
    if (password !== repeatPassword) {
      setError(t.passwordMismatch)
      return
    }
    setLoading(true)
    try {
      await authService.resetPassword({
        token,
        newPassword: password,
        confirmNewPassword: repeatPassword
      })
      // The password is changed and every session was invalidated server-side;
      // send the user to log in with the new credentials.
      navigate('/login')
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : t.resetFailed)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h2 className="auth-heading">{t.title}</h2>

        {error && <div className="auth-error-msg">{error}</div>}

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

          <button type="submit" className="primary-btn auth-submit-btn" disabled={loading}>
            {loading ? t.resetting : t.resetBtn}
          </button>
        </form>
      </div>
    </div>
  )
}

export default ResetPasswordPage
