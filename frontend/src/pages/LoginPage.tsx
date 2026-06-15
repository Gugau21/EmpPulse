import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { landingPath } from '../utils/guards'
import { isValidEmail } from '../utils/validation'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

const LoginPage: React.FC = () => {
  const { login } = useAuth()
  const navigate = useNavigate()
  const { language } = useLanguage()
  const t = translations[language].loginPage
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault()
    setError('')
    if (!isValidEmail(email)) {
      setError(t.invalidEmail)
      return
    }
    setLoading(true)
    try {
      const user = await login(email, password)
      navigate(landingPath(user))
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : t.loginFailed)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h1 className="auth-brand">EmpPulse</h1>
        <h2 className="auth-title">{t.logInTitle}</h2>

        {error && <div className="auth-error-msg">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form" noValidate>
          <label className="auth-input-label">
            {t.emailLabel}
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              maxLength={254}
            />
          </label>

          <label className="auth-input-label">
            {t.passwordLabel}
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              maxLength={128}
            />
          </label>

          <button type="submit" className="primary-btn auth-submit-btn" disabled={loading}>
            {loading ? t.loggingIn : t.loginBtn}
          </button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage
