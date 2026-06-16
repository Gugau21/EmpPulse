import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import { authService } from '../services/api'

const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate()
  const { language } = useLanguage()
  const t = translations[language].forgotPasswordPage
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authService.forgotPassword(email)
      // The server has queued the reset email; send the user to the confirmation
      // page (we don't reveal anything more about the account here).
      navigate('/check-email')
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : t.requestFailed)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h2 className="auth-heading">{t.title}</h2>
        <p className="auth-subtitle">{t.instruction}</p>

        {error && <div className="auth-error-msg">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <label className="auth-input-label">
            {t.emailLabel}
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </label>

          <button type="submit" className="primary-btn auth-submit-btn" disabled={loading}>
            {loading ? t.sending : t.continueBtn}
          </button>
        </form>
      </div>
    </div>
  )
}

export default ForgotPasswordPage
