import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { landingPath } from '../utils/guards'

const LoginPage: React.FC = () => {
  const { login } = useAuth()
  const navigate = useNavigate()
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
      navigate(landingPath(user))
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
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required maxLength={254} />
          </label>

          <label className="auth-input-label">
            Password
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              maxLength={128}
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
