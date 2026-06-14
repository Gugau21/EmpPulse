import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { landingPath } from '../utils/guards'
import UKflag from '../assets/uk_icon.png'
import UkranianFlag from '../assets/ukraine_icon.png'
import profileIcon from '../assets/profile_icon.png'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

// NavLink className callback: keep the base `nav-item` class and add `active`
// when the link matches the current URL (router-driven, replacing currentScreen).
const navItemClass = ({ isActive }: { isActive: boolean }) => `nav-item${isActive ? ' active' : ''}`

const Sidebar: React.FC = () => {
  const { currentUser, isOwner, isAdmin, isEmployee } = useAuth()
  const navigate = useNavigate()
  const { language, changeLanguage } = useLanguage()
  const t = translations[language].sidebar

  const showEmployees = isOwner || isAdmin
  const showRequestMgr = isOwner || isAdmin
  const showDepartments = isOwner || isAdmin
  const showMyRequests = isEmployee
  const displayName = currentUser ? `${currentUser.name} ${currentUser.surname}` : ''
  const displayRole = isOwner ? t.owner : isAdmin ? t.administrator : t.employee

  return (
    <aside className="sidebar">
      <div className="sidebar-top">
        <h1 className="brand-logo clickable" onClick={() => navigate(landingPath(currentUser))}>
          EmpPulse
        </h1>
        <nav className="sidebar-nav">
          {showDepartments && (
            <NavLink to="/departments" className={navItemClass}>
              {t.departmentList}
            </NavLink>
          )}
          {showEmployees && (
            <NavLink to="/employees" className={navItemClass}>
              {t.employees}
            </NavLink>
          )}
          {showRequestMgr && (
            <NavLink to="/request-manager" className={navItemClass}>
              {t.requestManager}
            </NavLink>
          )}
          {showMyRequests && (
            <NavLink to="/my-requests" className={navItemClass}>
              {t.myRequests}
            </NavLink>
          )}
        </nav>
      </div>

      <div className="sidebar-bottom">
        <div className="user-profile clickable" onClick={() => navigate('/profile')}>
          <div className="user-avatar">
            <img src={profileIcon} alt="Profile" width={35} height={35} />
          </div>
          <div className="user-info">
            <span className="user-name">{displayName}</span>
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
            <span 
              className={`lang-icon ${language === 'uk' ? 'is-dimmed' : ''}`}
              onClick={() => changeLanguage('en')}
            >
              <img src={UKflag} alt="English" width={27} height={27} />
            </span>
            <span 
              className={`lang-icon ${language === 'en' ? 'is-dimmed' : ''}`}
              onClick={() => changeLanguage('uk')}
            >
              <img src={UkranianFlag} alt="Ukrainian" width={27} height={27} />
            </span>
          </div>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
