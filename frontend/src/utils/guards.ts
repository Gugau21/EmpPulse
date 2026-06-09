import type { ScreenType, MeUser } from '../types'

export function canAccessRoute(screen: ScreenType, user: MeUser | null): boolean {
  if (screen === 'login') return true
  if (!user) return false

  const hasAdminAccess = user.owner || user.adminProfile !== null
  const isEmployee = user.employeeProfile !== null

  switch (screen) {
    case 'forbidden':
    case 'my-profile':
      return true
    case 'my-requests':
      return isEmployee
    default:
      // employees, request-manager, departments, department-detail, employee-profile
      return hasAdminAccess
  }
}
