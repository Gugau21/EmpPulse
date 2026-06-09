import React from 'react'
import type { ScreenType } from '../types'
import { useAuth } from '../context/useAuth'

interface Props {
  currentScreen: ScreenType
  setScreen: (screen: ScreenType) => void
}

const Sidebar: React.FC<Props> = ({ currentScreen, setScreen }) => {
  const { currentUser, isOwner, isAdmin, isEmployee } = useAuth()

  const showEmployees = isOwner || isAdmin
  const showRequestMgr = isOwner || isAdmin
  const showDepartments = isOwner || isAdmin
  const showMyRequests = isEmployee
  const displayName = currentUser ? `${currentUser.name} ${currentUser.surname}` : ''
  const displayRole = isOwner ? 'Owner' : isAdmin ? 'Administrator' : 'Employee'

  return (
    <aside className="sidebar">
      <div className="sidebar-top">
        <h1
          className="brand-logo clickable"
          onClick={() => setScreen(isOwner || isAdmin ? 'employees' : 'my-requests')}
        >
          EmpPulse
        </h1>
        <nav className="sidebar-nav">
          {showEmployees && (
            <button
              className={`nav-item ${currentScreen === 'employees' ? 'active' : ''}`}
              onClick={() => setScreen('employees')}
            >
              Employees
            </button>
          )}
          {showRequestMgr && (
            <button
              className={`nav-item ${currentScreen === 'request-manager' ? 'active' : ''}`}
              onClick={() => setScreen('request-manager')}
            >
              Request manager
            </button>
          )}
          {showMyRequests && (
            <button
              className={`nav-item ${currentScreen === 'my-requests' ? 'active' : ''}`}
              onClick={() => setScreen('my-requests')}
            >
              My requests
            </button>
          )}
          {showDepartments && (
            <button
              className={`nav-item ${currentScreen === 'departments' ? 'active' : ''}`}
              onClick={() => setScreen('departments')}
            >
              Department list
            </button>
          )}
        </nav>
      </div>

      <div className="sidebar-bottom">
        <div className="user-profile clickable" onClick={() => setScreen('my-profile')}>
          <div className="user-avatar"></div>
          <div className="user-info">
            <span className="user-name">{displayName}</span>
            {/* Only show role badge for owner (already prominent in the app). */}
            {isOwner && <span className="user-role">{displayRole}</span>}
          </div>
        </div>

        <div className="sidebar-controls">
          <div className="theme-toggle">
            <div className="toggle-thumb">
              <svg
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
              </svg>
            </div>
          </div>
          <div className="lang-toggles">
            <span className="lang-icon">🇬🇧</span>
            <span className="lang-icon is-dimmed">🇺🇦</span>
          </div>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
