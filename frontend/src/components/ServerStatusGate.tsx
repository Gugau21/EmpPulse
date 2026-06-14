import React from 'react'
import { useServerHealth } from '../hooks/useServerHealth'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

// Wraps the whole app: while the backend is unreachable, it hides everything and
// shows a single "service unavailable" screen instead. The poll keeps running
// underneath, so the app reappears on its own once the server is back — no reload
// needed. Mounted inside the providers so cached session/data survive the outage.
const ServerStatusGate: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const online = useServerHealth()
  const { language } = useLanguage()
  const t = translations[language].serverStatus

  if (!online) {
    return (
      <div className="auth-layout">
        <div className="auth-card">
          <h1 className="auth-brand">EmpPulse</h1>
          <h2 className="auth-title">{t.serviceUnavailable}</h2>
          <p className="muted">
            {t.unreachable}
          </p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}

export default ServerStatusGate
