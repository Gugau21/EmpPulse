import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { landingPath } from '../utils/guards'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

const ForbiddenPage: React.FC = () => {
  const { currentUser } = useAuth()
  const navigate = useNavigate()
  const { language } = useLanguage()
  const t = translations[language].forbiddenPage
  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h1 className="auth-brand">EmpPulse</h1>
        <div className="error-page">
          <h2 className="error-page-code">403</h2>
          <h3 className="error-page-title">{t.accessDenied}</h3>
          <p className="error-page-text">{t.noPermission}</p>
          <button
            className="primary-btn auth-submit-btn error-page-btn"
            onClick={() => navigate(landingPath(currentUser))}
          >
            {t.goHome}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ForbiddenPage
