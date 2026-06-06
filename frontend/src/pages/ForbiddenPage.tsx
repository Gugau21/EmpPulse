import React from 'react'

interface Props {
  onHome: () => void
}

const ForbiddenPage: React.FC<Props> = ({ onHome }) => {
  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h1 className="auth-brand">EmpPulse</h1>
        <div className="error-page">
          <h2 className="error-page-code">403</h2>
          <h3 className="error-page-title">Access Denied</h3>
          <p className="error-page-text">You don't have permission to access this resource.</p>
          <button className="primary-btn auth-submit-btn error-page-btn" onClick={onHome}>
            Go to home
          </button>
        </div>
      </div>
    </div>
  )
}

export default ForbiddenPage
