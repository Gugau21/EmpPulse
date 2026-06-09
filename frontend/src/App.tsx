import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './components/AppLayout'
import RequireRole from './components/RequireRole'
import LoginPage from './pages/LoginPage'
import ForbiddenPage from './pages/ForbiddenPage'
import EmployeesPage from './pages/EmployeesPage'
import RequestManagerPage from './pages/RequestManagerPage'
import MyRequestsPage from './pages/MyRequestsPage'
import DepartmentsPage from './pages/DepartmentsPage'
import DepartmentDetailPage from './pages/DepartmentDetailPage'
import ProfilePage from './pages/ProfilePage'
import { useAuth } from './context/useAuth'
import { landingPath } from './utils/guards'
import '@fontsource/poppins'
import './styles/global.css'

// `/` lands the user on the page their role makes sense for (mirrors the old
// post-login redirect). Lives inside AppLayout, so currentUser is already settled.
const IndexRedirect: React.FC = () => {
  const { currentUser } = useAuth()
  return <Navigate to={landingPath(currentUser)} replace />
}

const App: React.FC = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/forbidden" element={<ForbiddenPage />} />

    {/* Authenticated shell: sidebar + modals + the matched page in its <Outlet/>. */}
    <Route element={<AppLayout />}>
      <Route index element={<IndexRedirect />} />
      <Route
        path="employees"
        element={
          <RequireRole roles={['OWNER', 'ADMIN']}>
            <EmployeesPage />
          </RequireRole>
        }
      />
      <Route
        path="employees/:userId"
        element={
          <RequireRole roles={['OWNER', 'ADMIN']}>
            <ProfilePage />
          </RequireRole>
        }
      />
      <Route
        path="request-manager"
        element={
          <RequireRole roles={['OWNER', 'ADMIN']}>
            <RequestManagerPage />
          </RequireRole>
        }
      />
      <Route
        path="my-requests"
        element={
          <RequireRole roles={['WORKER']}>
            <MyRequestsPage />
          </RequireRole>
        }
      />
      <Route
        path="departments"
        element={
          <RequireRole roles={['OWNER', 'ADMIN']}>
            <DepartmentsPage />
          </RequireRole>
        }
      />
      <Route
        path="departments/:deptId"
        element={
          <RequireRole roles={['OWNER', 'ADMIN']}>
            <DepartmentDetailPage />
          </RequireRole>
        }
      />
      <Route
        path="profile"
        element={
          <RequireRole roles={['OWNER', 'ADMIN', 'WORKER']}>
            <ProfilePage />
          </RequireRole>
        }
      />
    </Route>

    {/* Unknown path → bounce through the index redirect to the role landing. */}
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
)

export default App
