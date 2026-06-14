import React, { useState } from 'react'
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import Sidebar from './Sidebar'
import Modals from './Modals'
import type { ModalType, Employee, LeaveRequest, Department, OpenModal } from '../types'
import { useAuth } from '../context/useAuth'
import { useDepartmentsList } from '../hooks/useDepartmentsList'
import { useDeleteDepartment } from '../hooks/useDepartmentMutations'
import { useDeleteEmployee } from '../hooks/useEmployeeMutations'
import { useCancelLeaveRequest, useDeleteLeaveRequest } from '../hooks/useLeaveRequestMutations'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

// Context shared with child pages via <Outlet>. Pages pull `openModal` from here
// instead of receiving it as a prop, so the modal state can live in one place.
export interface OutletContext {
  openModal: OpenModal
}

// Authenticated shell: sidebar + routed page (<Outlet/>) + the single modal stack.
// Owns all modal state and the confirm/close handlers that App.tsx used to hold;
// navigation that those handlers triggered is now done via the router.
const AppLayout: React.FC = () => {
  const { currentUser, isBootstrapping, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const { language } = useLanguage()
  const t = translations[language].appLayout

  const [activeModal, setActiveModal] = useState<ModalType>(null)
  // The modal a back button should return to, set when one modal opens another.
  const [previousModal, setPreviousModal] = useState<ModalType>(null)
  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(null)
  const [selectedRequest, setSelectedRequest] = useState<LeaveRequest | null>(null)
  // Full department object passed into modals (e.g. EditAdmins seeds from .admins).
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null)
  // Error surfaced inside the confirm modal when a confirm action fails (delete
  // employee/department). Cleared on close and when the action is re-attempted.
  const [confirmError, setConfirmError] = useState<string | null>(null)

  // Kept here (not just in DepartmentsPage) because the modals need the full list
  // for admin reassignment; React Query dedupes the shared request.
  const departmentsQuery = useDepartmentsList()
  const departments = departmentsQuery.data ?? []
  const deleteDepartment = useDeleteDepartment()
  const deleteEmployee = useDeleteEmployee()
  const deleteLeaveRequest = useDeleteLeaveRequest()
  const cancelLeaveRequest = useCancelLeaveRequest()

  const openModal: OpenModal = (modal, deptOrEmp, requestObj, returnTo) => {
    if (deptOrEmp) {
      // Departments carry an `admins` array; employees don't — discriminate on it.
      if ('admins' in deptOrEmp) {
        setSelectedDepartment(deptOrEmp)
      } else {
        setSelectedEmployee(deptOrEmp)
      }
    }
    if (requestObj) setSelectedRequest(requestObj)
    setPreviousModal(returnTo ?? null)
    setActiveModal(modal)
  }

  // Auth gate: wait for the on-mount /api/me probe so a refresh on a protected URL
  // doesn't flash to /login before the session is restored.
  if (isBootstrapping) {
    return (
      <div className="dashboard-layout">
        <main className="main-content-area">
          <p className="muted">{t.loading}</p>
        </main>
      </div>
    )
  }
  if (!currentUser) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="dashboard-layout">
      <Sidebar />

      <main className="main-content-area">
        {departmentsQuery.error && (
          <p className="form-error form-error-block">{departmentsQuery.error.message}</p>
        )}
        <Outlet context={{ openModal } satisfies OutletContext} />
      </main>

      <Modals
        activeModal={activeModal}
        previousModal={previousModal}
        openModal={openModal}
        confirmModal={async () => {
          if (activeModal === 'LOGOUT') {
            // AuthContext.logout clears the local session even if the network
            // call fails, so the user is never stranded in a signed-in state.
            await logout()
            setActiveModal(null)
            navigate('/login')
            return
          }
          if (activeModal === 'DELETE_EMPLOYEE' && selectedEmployee) {
            deleteEmployee.mutate(Number(selectedEmployee.id), {
              onSuccess: () => {
                setActiveModal(null)
                // If we deleted the employee whose profile we're viewing, leave it.
                if (location.pathname.startsWith('/employees/')) {
                  navigate('/employees')
                }
              },
              onError: err => setConfirmError(err.message)
            })
            return
          }
          if (activeModal === 'DELETE_LEAVE' && selectedRequest) {
            deleteLeaveRequest.mutate(Number(selectedRequest.id), {
              onSuccess: () => setActiveModal(null),
              onError: err => setConfirmError(err.message)
            })
            return
          }
          if (activeModal === 'CANCEL_LEAVE' && selectedRequest) {
            cancelLeaveRequest.mutate(Number(selectedRequest.id), {
              onSuccess: () => setActiveModal(null),
              onError: err => setConfirmError(err.message)
            })
            return
          }
          if (activeModal === 'DELETE_DEPARTMENT' && selectedDepartment) {
            const dept = selectedDepartment
            deleteDepartment.mutate(dept.id, {
              // The mutation invalidates the list, so no manual reload is needed.
              onSuccess: () => {
                setActiveModal(null)
                if (location.pathname.startsWith('/departments/')) {
                  navigate('/departments')
                }
              },
              onError: () => {
                setConfirmError(
                  dept.admins.length > 0 ? t.errAdminsAttached : t.errEmployeesAttached
                )
              }
            })
            return
          }
          setActiveModal(null)
        }}
        closeModal={() => {
          setConfirmError(null)
          setActiveModal(null)
        }}
        selectedEmployee={selectedEmployee}
        selectedRequest={selectedRequest}
        selectedDepartment={selectedDepartment}
        departments={departments}
        confirmError={confirmError}
        onConfirmErrorClear={() => setConfirmError(null)}
      />
    </div>
  )
}

export default AppLayout
