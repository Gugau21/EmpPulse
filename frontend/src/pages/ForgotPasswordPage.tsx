import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate()
  const { language } = useLanguage()
  const t = translations[language].forgotPasswordPage
  const [email, setEmail] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    navigate('/check-email')
  }

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h2 className="auth-heading">{t.title}</h2>
        <p className="auth-subtitle">{t.instruction}</p>
        
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
          
          <button type="submit" className="primary-btn auth-submit-btn">
            {t.continueBtn}
          </button>
        </form>
      </div>
    </div>
  )
}

export default ForgotPasswordPage