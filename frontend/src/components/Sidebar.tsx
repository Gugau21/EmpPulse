import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { landingPath } from '../utils/guards'
import moonIcon from '../assets/moon_icon.png'
import sunIcon from '../assets/sun_icon.png'
import { useTheme } from '../hooks/useTheme'

const navItemClass = ({ isActive }: { isActive: boolean }) => `nav-item${isActive ? ' active' : ''}`

const Sidebar: React.FC = () => {
  const { currentUser, isOwner, isAdmin, isEmployee } = useAuth()
  const navigate = useNavigate()

  const { theme, toggleTheme } = useTheme()

  const showEmployees = isOwner || isAdmin
  const showRequestMgr = isOwner || isAdmin
  const showDepartments = isOwner || isAdmin
  const showMyRequests = isEmployee
  const displayName = currentUser ? `${currentUser.name} ${currentUser.surname}` : ''
  const displayRole = isOwner ? 'Owner' : isAdmin ? 'Administrator' : 'Employee'
  const isLightMode = theme === 'light'

  return (
    <aside className="sidebar">
      <div className="sidebar-top">
        <h1 className="brand-logo clickable" onClick={() => navigate(landingPath(currentUser))}>
          EmpPulse
        </h1>
        <nav className="sidebar-nav">
          {showDepartments && (
            <NavLink to="/departments" className={navItemClass}>
              Department list
            </NavLink>
          )}
          {showEmployees && (
            <NavLink to="/employees" className={navItemClass}>
              Employees
            </NavLink>
          )}
          {showRequestMgr && (
            <NavLink to="/request-manager" className={navItemClass}>
              Request manager
            </NavLink>
          )}
          {showMyRequests && (
            <NavLink to="/my-requests" className={navItemClass}>
              My requests
            </NavLink>
          )}
        </nav>
      </div>

      <div className="sidebar-bottom">
        <div className="user-profile clickable" onClick={() => navigate('/profile')}>
          <div className="user-avatar"></div>
          <div className="user-info">
            <span className="user-name">{displayName}</span>
            {isOwner && <span className="user-role">{displayRole}</span>}
          </div>
        </div>

        <div className="sidebar-controls">
          {/* Here is the perfectly structured toggle! */}
          <div className={`theme-toggle ${isLightMode ? 'light' : ''}`} onClick={toggleTheme}>
            <div className="toggle-thumb">
              {!isLightMode ? (
                // Solid Moon Icon
                <img src={sunIcon} alt="Light mode" width={22} height={22} />
              ) : (
                // Solid Center Sun Icon with thick rays

                <img src={moonIcon} alt="Dark mode" width={25} height={25} />
              )}
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
