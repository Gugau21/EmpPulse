import type { MeUser, Department, DepartmentAdmin, Employee, LeaveRequest } from '../types'

// Abort a request that hasn't responded in this long so a hung backend surfaces as
// a retryable failure rather than an indefinite spinner. React Query then retries
// (see queryClient), giving up to 3 attempts total before the error reaches the UI.
const REQUEST_TIMEOUT_MS = 5000

function getCsrfToken(): string {
  return (
    document.cookie
      .split('; ')
      .find(row => row.startsWith('XSRF-TOKEN='))
      ?.split('=')[1] ?? ''
  )
}

// Carries the HTTP status so callers (e.g. the React Query retry predicate) can
// distinguish client (4xx) from server/network errors without parsing messages.
export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function clientSafeError(
  res: Response,
  fallback: string,
  overrides: Record<number, string> = {}
): Promise<ApiError> {
  if (overrides[res.status]) return new ApiError(res.status, overrides[res.status])
  switch (res.status) {
    case 400:
      return new ApiError(
        res.status,
        'The request was invalid. Please check your input and try again.'
      )
    case 401:
      return new ApiError(res.status, 'Your session has expired. Please sign in again.')
    case 403:
      return new ApiError(res.status, 'You do not have permission to perform this action.')
    case 404:
      return new ApiError(res.status, 'The requested item could not be found.')
    case 409:
      return new ApiError(
        res.status,
        'This action conflicts with the current state. Please refresh and retry.'
      )
    default:
      return new ApiError(res.status, fallback)
  }
}

interface RequestOptions {
  method?: string
  // JSON body to send; when present, the JSON content-type header is added.
  body?: unknown
  signal?: AbortSignal
  // Human-readable fallback message used when no status override matches.
  errorFallback: string
  // Per-status message overrides passed through to clientSafeError.
  errorOverrides?: Record<number, string>
}

// Single place that performs an authenticated fetch and turns a non-ok response
// into an ApiError. Every service method below routes through here so the
// credentials / CSRF / error-mapping boilerplate isn't copied per endpoint.
async function apiRequest(path: string, opts: RequestOptions): Promise<Response> {
  const { method = 'GET', body, signal, errorFallback, errorOverrides } = opts
  const headers: Record<string, string> = {}
  if (method !== 'GET') headers['X-XSRF-TOKEN'] = getCsrfToken()
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  // Race the caller's signal (e.g. React Query cancelling a superseded query)
  // against our own timeout, so either can abort the fetch.
  const timeout = AbortSignal.timeout(REQUEST_TIMEOUT_MS)
  const merged = signal ? AbortSignal.any([signal, timeout]) : timeout

  let res: Response
  try {
    res = await fetch(path, {
      method,
      headers,
      credentials: 'include',
      signal: merged,
      ...(body !== undefined ? { body: JSON.stringify(body) } : {})
    })
  } catch (err) {
    // A caller-initiated abort must propagate untouched so React Query treats it as
    // a cancellation, not a failure. Only our timeout is remapped into a retryable
    // ApiError (status 0 isn't a 4xx, so the retry predicate lets it through).
    if (timeout.aborted && !signal?.aborted) {
      throw new ApiError(0, 'The server took too long to respond. Please try again.')
    }
    throw err
  }
  if (!res.ok) {
    throw await clientSafeError(res, errorFallback, errorOverrides)
  }
  return res
}

export const authService = {
  login: async (email: string, password: string): Promise<MeUser> => {
    const res = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: { email, password },
      errorFallback: 'Unable to sign in. Please try again.',
      errorOverrides: {
        400: 'Invalid email or password.',
        401: 'Invalid email or password.'
      }
    })
    const data = await res.json()
    return data as MeUser
  },

  // GET /api/me — restores the session on page load. Returns the MeUser when a
  // session cookie is still valid; a 401 means "no session" (not an error), so we
  // return null. Any other status propagates as an ApiError.
  me: async (signal?: AbortSignal): Promise<MeUser | null> => {
    try {
      const res = await apiRequest('/api/me', {
        signal,
        errorFallback: 'Failed to restore session.'
      })
      return (await res.json()) as MeUser
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) return null
      throw err
    }
  },

  logout: async (): Promise<void> => {
    // Logout is best-effort: swallow errors so a failed call still clears local state.
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'X-XSRF-TOKEN': getCsrfToken() },
        credentials: 'include'
      })
    } catch {
      // Network failure — the caller clears the local session regardless.
    }
  }
}

export interface UserCreatePayload {
  name: string
  surname: string
  email: string
  password: string
  employeeDepartmentId?: number | null
  yearlyVacationBalance?: number
  adminDepartmentIds?: number[]
}

export interface UserUpdatePayload {
  name?: string
  surname?: string
  email?: string
  password?: string
  // Must be true for employeeDepartmentId to be applied; the server otherwise
  // can't tell an omitted department from a detach (null).
  changeEmployeeDepartment?: boolean
  employeeDepartmentId?: number | null
  yearlyVacationBalance?: number
  adminDepartmentIds?: number[]
}

export const userService = {
  create: async (payload: UserCreatePayload): Promise<void> => {
    await apiRequest('/api/users', {
      method: 'POST',
      body: payload,
      errorFallback: 'Failed to create user.',
      errorOverrides: { 409: 'A user with this email already exists.' }
    })
  },

  delete: async (userId: number): Promise<void> => {
    await apiRequest(`/api/users/${userId}`, {
      method: 'DELETE',
      errorFallback: 'Failed to delete employee.',
      errorOverrides: { 403: 'Only the owner can delete employees.' }
    })
  },

  getById: async (userId: number, signal?: AbortSignal): Promise<MeUser> => {
    const res = await apiRequest(`/api/users/${userId}`, {
      signal,
      errorFallback: 'Failed to load employee details.'
    })
    return (await res.json()) as MeUser
  },

  update: async (userId: number, payload: UserUpdatePayload): Promise<void> => {
    await apiRequest(`/api/users/${userId}`, {
      method: 'PATCH',
      body: payload,
      errorFallback: 'Failed to update employee.',
      errorOverrides: {
        403: 'You do not have permission to change these fields.',
        409: 'A user with this email already exists.'
      }
    })
  }
}

// Raw item shape from GET /api/employees (EmployeeSummaryResponse).
interface EmployeeSummaryDto {
  id: number
  name: string
  surname: string
  departmentId: number | null
  departmentName: string | null
}

export const employeeService = {
  // GET /api/employees (OWNER lists all; ADMIN receives only employees in their
  // departments — filtered server-side). Mapped into the app's Employee shape;
  // the API summary carries no leave/status data, so those fields stay absent.
  getAll: async (signal?: AbortSignal): Promise<Employee[]> => {
    const res = await apiRequest('/api/employees', {
      signal,
      errorFallback: 'Failed to load employees.'
    })
    const data = await res.json()
    const items = (data.items ?? []) as EmployeeSummaryDto[]
    return items.map(e => ({
      id: String(e.id),
      name: e.name,
      surname: e.surname,
      department: e.departmentName ?? undefined
    }))
  }
}

// Wire format for leave types (LeaveType in the API contract); the UI's
// display values ('Vacation' | 'Sick' | 'Personal') must be mapped to these.
export type ApiLeaveType = 'SICK' | 'VACATION' | 'PERSONAL'

// PATCH /api/leave-requests/{id} body (LeaveRequestUpdate in the API contract).
// All fields optional; dates are ISO yyyy-mm-dd. reason is required server-side
// when type is PERSONAL.
export interface LeaveRequestUpdatePayload {
  type?: ApiLeaveType
  paid?: boolean
  startDate?: string
  endDate?: string
  reason?: string | null
}

// POST /api/leave-requests body (LeaveCreateRequest in the API contract). The
// wire enum is lower-cased to match the server's LeaveType; dates are ISO
// yyyy-mm-dd. reason is required server-side when type is personal; adminComment
// is accepted only from administrators (filing on behalf of an employee).
export interface LeaveRequestCreatePayload {
  employeeId: number
  type: 'vacation' | 'sick' | 'personal'
  paid: boolean
  startDate: string
  endDate: string
  reason?: string | null
  adminComment?: string | null
}

// Raw item from GET /api/leave-requests (LeaveResponse). The wire enums are
// lower-cased (the server serializes its LeaveType/LeaveStatus by name); the UI
// uses display-cased types and upper-cased statuses, so both are mapped below.
interface LeaveResponseDto {
  id: number
  employee: EmployeeSummaryDto & { active: boolean }
  type: 'vacation' | 'sick' | 'personal'
  paid: boolean
  startDate: string
  endDate: string
  reason: string | null
  status: 'pending' | 'approved' | 'rejected' | 'cancelled'
  adminReviewerId: number | null
  adminComment: string | null
  createdAt: string
  updatedAt: string | null
}

const LEAVE_TYPE_FROM_API: Record<LeaveResponseDto['type'], LeaveRequest['type']> = {
  vacation: 'Vacation',
  sick: 'Sick',
  personal: 'Personal'
}

// ISO yyyy-mm-dd → the dd.mm.yyyy display the request rows and edit modal expect.
function isoToDisplayDate(iso: string): string {
  const [y, m, d] = iso.split('-')
  return `${d}.${m}.${y}`
}

function mapLeaveResponse(dto: LeaveResponseDto): LeaveRequest {
  return {
    id: String(dto.id),
    employeeId: dto.employee.id,
    employeeName: `${dto.employee.name} ${dto.employee.surname}`,
    type: LEAVE_TYPE_FROM_API[dto.type],
    startDate: dto.startDate,
    // When the request has never been edited the server leaves updatedAt null,
    // so fall back to its creation time as the "last edit".
    lastEditedAt: dto.updatedAt ?? dto.createdAt,
    dateRange: `${isoToDisplayDate(dto.startDate)} - ${isoToDisplayDate(dto.endDate)}`,
    status: dto.status.toUpperCase() as LeaveRequest['status'],
    reason: dto.reason ?? undefined
  }
}

export const leaveRequestService = {
  // GET /api/leave-requests — the requests visible to the caller, scoped
  // server-side by role: an employee receives only their own; an admin receives
  // their own plus those of employees in departments they oversee; the owner
  // receives everyone's. Mapped into the app's LeaveRequest shape.
  list: async (signal?: AbortSignal): Promise<LeaveRequest[]> => {
    const res = await apiRequest('/api/leave-requests', {
      signal,
      errorFallback: 'Failed to load leave requests.'
    })
    const data = await res.json()
    const items = (data.items ?? []) as LeaveResponseDto[]
    return items.map(mapLeaveResponse)
  },

  // POST /api/leave-requests — an employee files their own request (stays
  // pending); an admin/owner may file on behalf of an employee in a department
  // they oversee (auto-approved). The server rejects overlapping ranges (409).
  create: async (payload: LeaveRequestCreatePayload): Promise<void> => {
    await apiRequest('/api/leave-requests', {
      method: 'POST',
      body: payload,
      errorFallback: 'Failed to create leave request.',
      errorOverrides: {
        409: 'This leave overlaps an existing request. Please choose different dates.'
      }
    })
  },

  // PATCH /api/leave-requests/{id} — employees edit their own PENDING request;
  // admins update any request in their departments in-place.
  update: async (id: number, payload: LeaveRequestUpdatePayload): Promise<void> => {
    await apiRequest(`/api/leave-requests/${id}`, {
      method: 'PATCH',
      body: payload,
      errorFallback: 'Failed to update leave request.',
      errorOverrides: {
        409: 'This request was changed elsewhere. Please refresh and retry.'
      }
    })
  }
}

export interface DepartmentCreatePayload {
  name: string
  adminIds?: number[]
}

export const departmentService = {
  // GET /api/departments (OWNER lists all; ADMIN receives only their own — filtered server-side)
  getAll: async (): Promise<Department[]> => {
    const res = await apiRequest('/api/departments', {
      errorFallback: 'Failed to load departments.'
    })
    const data = await res.json()
    return (data.items ?? []) as Department[]
  },

  // GET /api/departments/{id} (OWNER, or ADMIN for departments they administer)
  // `signal` lets a caller abort an in-flight request (e.g. a superseded selection).
  getById: async (id: number, signal?: AbortSignal): Promise<Department> => {
    const res = await apiRequest(`/api/departments/${id}`, {
      signal,
      errorFallback: 'Failed to load department.'
    })
    return (await res.json()) as Department
  },

  // POST /api/departments (OWNER only)
  create: async (payload: DepartmentCreatePayload): Promise<void> => {
    await apiRequest('/api/departments', {
      method: 'POST',
      body: payload,
      errorFallback: 'Failed to create department.',
      errorOverrides: { 409: 'A department with this name already exists.' }
    })
  },

  // DELETE /api/departments/{id} (OWNER only) — department must have no admins or employees
  delete: async (id: number): Promise<void> => {
    await apiRequest(`/api/departments/${id}`, {
      method: 'DELETE',
      errorFallback: 'Failed to delete department.'
    })
  },

  // PATCH /api/departments/{id} (OWNER only) — rename and/or reassign admins
  update: async (id: number, payload: { name?: string; adminIds?: number[] }): Promise<void> => {
    await apiRequest(`/api/departments/${id}`, {
      method: 'PATCH',
      body: payload,
      errorFallback: 'Failed to update department.',
      errorOverrides: { 409: 'A department with this name already exists.' }
    })
  }
}

export const adminService = {
  // GET /api/admins (OWNER only) — every admin, used to assign admins to departments
  getAll: async (): Promise<DepartmentAdmin[]> => {
    const res = await apiRequest('/api/admins', { errorFallback: 'Failed to load admins.' })
    const data = await res.json()
    return (data.items ?? []) as DepartmentAdmin[]
  }
}
