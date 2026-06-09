import React from 'react'
import { useServerHealth } from '../hooks/useServerHealth'

// Wraps the whole app: while the backend is unreachable, it hides everything and
// shows a single "service unavailable" screen instead. The poll keeps running
// underneath, so the app reappears on its own once the server is back — no reload
// needed. Mounted inside the providers so cached session/data survive the outage.
const ServerStatusGate: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const online = useServerHealth()

  if (!online) {
    return (
      <div className="auth-layout">
        <div className="auth-card">
          <h1 className="auth-brand">EmpPulse</h1>
          <h2 className="auth-title">Service unavailable</h2>
          <p className="muted">
            We can’t reach the server right now. This page will reconnect automatically once it’s
            back online.
          </p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}

export default ServerStatusGate
