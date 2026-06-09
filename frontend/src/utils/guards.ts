import type { ScreenType, MeUser } from '../types'

export function canAccessRoute(screen: ScreenType, user: MeUser | null): boolean {
  if (screen === 'login') return true
  if (!user) return false

  switch (screen) {
    case 'forbidden':
    case 'my-profile':
      return true
    case 'my-requests':
      return user.employeeProfile !== null
    default:
      // employees, request-manager, departments, department-detail, employee-profile
      return user.owner || user.adminProfile !== null
  }
}
