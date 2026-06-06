import React, { useState } from 'react'
import Sidebar from './components/Sidebar'
import Modals from './components/Modals'
import LoginScreen from './pages/LoginPage'
import ForbiddenScreen from './pages/ForbiddenPage'
import EmployeesScreen from './pages/EmployeesPage'
import RequestManagerScreen from './pages/RequestManagerPage'
import MyRequestsScreen from './pages/MyRequestsPage'
import DepartmentsScreen from './pages/DepartmentsPage'
import DepartmentDetailScreen from './pages/DepartmentDetailPage'
import ProfileScreen from './pages/ProfilePage'

import type {
  ScreenType,
  ModalType,
  ModalPayload,
  Employee,
  LeaveRequest,
  Department,
  MeUser
} from './types'
import { canAccessRoute } from './utils/guards'
import { useAuth } from './context/useAuth'
import { useDepartmentsList } from './hooks/useDepartmentsList'
import { useDepartmentDetail } from './hooks/useDepartmentDetail'
import { useDeleteDepartment } from './hooks/useDepartmentMutations'
import '@fontsource/poppins'
import { useDeleteEmployee } from './hooks/useEmployeeMutations'
import './styles/global.css'

const App: React.FC = () => {
  const { userRole, logout } = useAuth()
  const [currentScreen, setCurrentScreen] = useState<ScreenType>('login')
  const [activeModal, setActiveModal] = useState<ModalType>(null)

  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(null)
  const [selectedRequest, setSelectedRequest] = useState<LeaveRequest | null>(null)
  // Full department object passed into modals (e.g. EditAdmins seeds from .admins).
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null)
  // Id of the department whose detail view is open; drives the detail query.
  const [selectedDeptId, setSelectedDeptId] = useState<number | null>(null)
  // Error surfaced inside the confirm modal when a confirm action fails (delete
  // employee/department). Cleared on close and when the action is re-attempted.
  const [confirmError, setConfirmError] = useState<string | null>(null)

  const departmentsQuery = useDepartmentsList()
  const departments = departmentsQuery.data ?? []
  const departmentDetailQuery = useDepartmentDetail(selectedDeptId)
  const deleteDepartment = useDeleteDepartment()
  const deleteEmployee = useDeleteEmployee()

  const handleLoginSuccess = (user: MeUser) => {
    setCurrentScreen(user.adminProfile === null && !user.owner ? 'my-requests' : 'employees')
  }

  const handleSetScreen = (screen: ScreenType) => {
    if (!canAccessRoute(screen, userRole)) {
      setCurrentScreen('forbidden')
      return
    }
    setCurrentScreen(screen)
  }

  const handleOpenModal = (
    modal: ModalType,
    deptOrEmp?: ModalPayload,
    requestObj?: LeaveRequest
  ) => {
    if (deptOrEmp) {
      // Departments carry an `admins` array; employees don't — discriminate on it.
      if ('admins' in deptOrEmp) {
        setSelectedDepartment(deptOrEmp)
      } else {
        setSelectedEmployee(deptOrEmp)
      }
    }
    if (requestObj) setSelectedRequest(requestObj)
    setActiveModal(modal)
  }

  // Open a department's detail view; the useDepartmentDetail query fetches its full
  // record. React Query cancels a superseded request when selectedDeptId changes.
  const handleSelectDepartment = (dept: Department) => {
    if (!canAccessRoute('department-detail', userRole)) {
      setCurrentScreen('forbidden')
      return
    }
    setSelectedDeptId(dept.id)
    setCurrentScreen('department-detail')
  }

  const handleOpenEmployeeProfile = (emp: Employee) => {
    setSelectedEmployee(emp)
    handleSetScreen('employee-profile')
  }

  if (currentScreen === 'login') {
    return <LoginScreen onLoginSuccess={handleLoginSuccess} />
  }

  if (currentScreen === 'forbidden') {
    return (
      <ForbiddenScreen
        onHome={() => handleSetScreen(userRole === 'WORKER' ? 'my-requests' : 'employees')}
      />
    )
  }

  return (
    <div className="dashboard-layout">
      <Sidebar currentScreen={currentScreen} setScreen={handleSetScreen} />

      <main className="main-content-area">
        {departmentDetailQuery.error && (
          <p className="form-error form-error-block">{departmentDetailQuery.error.message}</p>
        )}
        {departmentsQuery.error && (
          <p className="form-error form-error-block">{departmentsQuery.error.message}</p>
        )}
        {currentScreen === 'employees' && userRole && ['OWNER', 'ADMIN'].includes(userRole) && (
          <EmployeesScreen
            openEmployeeProfile={handleOpenEmployeeProfile}
            openModal={handleOpenModal}
          />
        )}
        {currentScreen === 'request-manager' &&
          userRole &&
          ['OWNER', 'ADMIN'].includes(userRole) && (
            <RequestManagerScreen openModal={handleOpenModal} />
          )}
        {currentScreen === 'my-requests' && userRole && ['ADMIN', 'WORKER'].includes(userRole) && (
          <MyRequestsScreen openModal={handleOpenModal} />
        )}
        {currentScreen === 'departments' && userRole && ['OWNER', 'ADMIN'].includes(userRole) && (
          <DepartmentsScreen
            departments={departments}
            loading={departmentsQuery.isLoading}
            openModal={handleOpenModal}
            onSelectDepartment={handleSelectDepartment}
          />
        )}
        {currentScreen === 'department-detail' &&
          userRole &&
          ['OWNER', 'ADMIN'].includes(userRole) && (
            <DepartmentDetailScreen
              department={departmentDetailQuery.data ?? null}
              loading={departmentDetailQuery.isLoading}
              openModal={handleOpenModal}
              onBack={() => handleSetScreen('departments')}
            />
          )}
        {currentScreen === 'my-profile' && (
          <ProfileScreen
            isMyProfile={true}
            openModal={handleOpenModal}
            onBack={() => handleSetScreen('employees')}
          />
        )}
        {currentScreen === 'employee-profile' &&
          userRole &&
          ['OWNER', 'ADMIN'].includes(userRole) && (
            <ProfileScreen
              isMyProfile={false}
              employee={selectedEmployee}
              openModal={handleOpenModal}
              onBack={() => handleSetScreen('employees')}
            />
          )}
      </main>

      <Modals
        activeModal={activeModal}
        openModal={handleOpenModal}
        confirmModal={async () => {
          if (activeModal === 'LOGOUT') {
            // AuthContext.logout clears the local session even if the network
            // call fails, so the user is never stranded in a signed-in state.
            await logout()
            setCurrentScreen('login')
            setActiveModal(null)
            return
          }
          if (activeModal === 'DELETE_EMPLOYEE' && selectedEmployee) {
            deleteEmployee.mutate(Number(selectedEmployee.id), {
              onSuccess: () => {
                setActiveModal(null)
                if (currentScreen === 'employee-profile') {
                  setCurrentScreen('employees')
                }
              },
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
                if (currentScreen === 'department-detail') {
                  setSelectedDeptId(null)
                  setCurrentScreen('departments')
                }
              },
              onError: () => {
                setConfirmError(
                  dept.admins.length > 0
                    ? 'This department still has administrators attached to it. Unassign all administrators before deleting.'
                    : 'This department still has employees assigned to it. Unassign all employees before deleting.'
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

export default App
