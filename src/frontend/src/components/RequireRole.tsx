import React from 'react'
import { Navigate } from 'react-router-dom'
import type { MeUser } from '../types'
import { useAuth } from '../context/useAuth'

type Role = 'OWNER' | 'ADMIN' | 'WORKER'

interface Props {
  roles: Role[]
  children: React.ReactNode
}

// Route guard: renders children if the current user holds ANY of the allowed
// capabilities, else redirects to /forbidden. Capabilities are NOT mutually
// exclusive — a user can be an admin and an employee at once — so we test each
// allowed role against its own profile flag rather than collapsing to one role.
const ROLE_PREDICATES: Record<Role, (u: MeUser) => boolean> = {
  OWNER: u => u.owner,
  ADMIN: u => u.adminProfile != null,
  WORKER: u => u.employeeProfile != null
}

const RequireRole: React.FC<Props> = ({ roles, children }) => {
  const { currentUser } = useAuth()
  const allowed = currentUser != null && roles.some(role => ROLE_PREDICATES[role](currentUser))

  if (!allowed) {
    return <Navigate to="/forbidden" replace />
  }
  return <>{children}</>
}

export default RequireRole
