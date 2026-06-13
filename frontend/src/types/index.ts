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

// A currently-active approved leave, as returned on the employee list and the
// user profile. The wire `type` is the lower-cased LeaveType enum; the UI maps it
// to a display-cased status badge and an "until …" label (see describeActiveLeave).
export interface ActiveLeave {
  type: 'vacation' | 'sick' | 'personal'
  // ISO yyyy-mm-dd bounds of the leave; endDate drives the "until …" label.
  startDate: string
  endDate: string
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
  // dd.mm.yyyy end date of the active leave, paired with `status`.
  untilDate?: string
}

export interface LeaveRequest {
  id: string
  // The id of the employee the request belongs to. Lets "My requests" narrow the
  // shared list to the caller's own rows (an admin/owner is served everyone's).
  employeeId: number
  employeeName: string
  type: 'Vacation' | 'Personal' | 'Sick'
  // ISO yyyy-mm-dd start of the leave, kept alongside the display `dateRange`
  // so the request lists can sort by when the leave begins.
  startDate: string
  // ISO yyyy-mm-dd end of the leave; with `startDate` it lets the lists bucket a
  // request as current/future/past relative to today.
  endDate: string
  // ISO timestamp of the last edit: the server's updatedAt, or its createdAt when
  // the request has never been edited. Lets the lists sort by most recently touched.
  lastEditedAt: string
  dateRange: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  // Whether the leave is paid; the view modal surfaces this as "Paid"/"Unpaid".
  paid: boolean
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
    // The employee's current approved leave, or null when they are working.
    activeLeave: ActiveLeave | null
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
  | 'VIEW_LEAVE_FORM'
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
// `returnTo` records the modal to go back to, letting a modal opened from another
// (e.g. the edit form launched from the accept/reject modal) show a back button.
export type OpenModal = (
  modal: ModalType,
  payload?: ModalPayload,
  requestObj?: LeaveRequest,
  returnTo?: ModalType
) => void
