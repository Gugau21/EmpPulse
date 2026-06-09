import type { MeUser } from '../types'

// Where a user should land after login or when hitting `/` / an unknown route.
// Employee-only users (an employee profile, no admin profile, not the owner) go
// to their own requests; everyone else (owner/admin) goes to the employee list.
// Mirrors the old handleLoginSuccess branch in App.tsx.
export function landingPath(user: MeUser | null): string {
  if (!user) return '/login'
  if (user.employeeProfile != null && user.adminProfile === null && !user.owner) {
    return '/my-requests'
  }
  return '/employees'
}
