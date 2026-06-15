import React from 'react'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

const CheckEmailPage: React.FC = () => {
  const { language } = useLanguage()
  const t = translations[language].checkEmailPage

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h2 className="auth-heading">{t.title}</h2>
        <p className="auth-subtitle" style={{ marginBottom: 0 }}>
          {t.instruction}
        </p>
      </div>
    </div>
  )
}

export default CheckEmailPage