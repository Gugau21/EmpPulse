import type { ScreenType, UserRole } from '../types'

const ROUTE_PERMISSIONS: Record<ScreenType, UserRole[]> = {
  login: [],
  forbidden: ['OWNER', 'ADMIN', 'WORKER'],
  'my-profile': ['OWNER', 'ADMIN', 'WORKER'],
  employees: ['OWNER', 'ADMIN'],
  'request-manager': ['OWNER', 'ADMIN'],
  'my-requests': ['ADMIN', 'WORKER'],
  departments: ['OWNER', 'ADMIN'],
  'department-detail': ['OWNER', 'ADMIN'],
  'employee-profile': ['OWNER', 'ADMIN']
}

export function canAccessRoute(screen: ScreenType, userRole: UserRole | null): boolean {
  if (screen === 'login') return true
  if (!userRole) return false
  const allowedRoles = ROUTE_PERMISSIONS[screen] || []
  return allowedRoles.includes(userRole)
}
