// Kept for the (currently hidden) default working-hours feature.
export interface WorkingShift {
  start: string
  end: string
}

export interface DaySchedule {
  day: 'Monday' | 'Tuesday' | 'Wednesday' | 'Thursday' | 'Friday' | 'Saturday' | 'Sunday'
  shifts: WorkingShift[]
}

export interface DepartmentAdmin {
  id: number
  user: { id: number; name: string; surname: string; email: string }
  departmentIds: number[]
  active: boolean
}

export interface Department {
  id: number
  name: string
  admins: DepartmentAdmin[]
}

export interface Employee {
  id: string
  name: string
  surname: string
  email?: string
  department?: string
  role?: string
  // "Working" is not a status — an employee is working when this is absent.
  status?: 'Personal' | 'Sick' | 'Vacation'
  untilDate?: string
}

export interface LeaveRequest {
  id: string
  employeeName: string
  type: 'Vacation' | 'Personal' | 'Sick'
  dateRange: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  reason?: string
}

export interface MeUser {
  id: number
  name: string
  surname: string
  email: string
  owner: boolean
  preferences: { theme: string; language: string }
  employeeProfile: {
    employeeId: number
    departmentId: number | null
    departmentName: string | null
    yearlyVacationBalance: number
  } | null
  adminProfile: { id: number; departmentIds: number[] } | null
}

// --- App State Types ---
export type ModalType =
  | null
  | 'ADD_EMPLOYEE'
  | 'DELETE_EMPLOYEE'
  | 'LOG_HOURS'
  | 'ADD_DEPARTMENT'
  | 'DELETE_DEPARTMENT'
  | 'EDIT_ADMINS'
  | 'EDIT_WORKING_HOURS'
  | 'ADD_LEAVE'
  | 'EDIT_LEAVE'
  | 'CREATE_REQUEST'
  | 'ACCEPT_REQUEST'
  | 'ADD_REQUEST_FORM'
  | 'EDIT_LEAVE_FORM'
  | 'DELETE_LEAVE'
  | 'CANCEL_LEAVE'
  | 'LOGOUT'
  | 'CHANGE_PASSWORD_CONFIRM'
  | 'CHANGE_PASSWORD_FORM'
  | 'CHANGE_PASSWORD'
  | 'EDIT_DEPARTMENT'
  | 'ADD_WORKING_HOURS'
  | 'EDIT_WORKING_HOURS'
  | 'EDIT_EMPLOYEE'
  | 'EDIT_LOGGED_HOURS'

// Payload a caller passes to openModal: a department (its detail/admins modals)
// or an employee (its profile/edit modals). Discriminated via the `admins` field.
export type ModalPayload = Department | Employee

// Shared signature for the openModal callback threaded through screens and modals.
export type OpenModal = (
  modal: ModalType,
  payload?: ModalPayload,
  requestObj?: LeaveRequest
) => void
