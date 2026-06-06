import React, { useState } from 'react'
import type { MeUser } from '../types'
import { useAuth } from '../context/useAuth'

interface Props {
  onLoginSuccess: (user: MeUser) => void
}

const LoginPage: React.FC<Props> = ({ onLoginSuccess }) => {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await login(email, password)
      onLoginSuccess(user)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h1 className="auth-brand">EmpPulse</h1>
        <h2 className="auth-title">Log In</h2>

        {error && <div className="auth-error-msg">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <label className="auth-input-label">
            Email
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
          </label>

          <label className="auth-input-label">
            Password
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </label>

          <button type="submit" className="primary-btn auth-submit-btn" disabled={loading}>
            {loading ? 'Logging in…' : 'log in'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage
