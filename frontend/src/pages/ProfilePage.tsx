import React, { useState } from 'react'
import type { Employee, OpenModal } from '../types'
import { MOCK_LOGGED_HOURS, MOCK_DEFAULT_WORKING_HOURS } from '../utils/mockData'
import { useAuth } from '../context/useAuth'
import blackTriangleIcon from '../assets/black_triangle.png'

interface Props {
  isMyProfile?: boolean
  employee?: Employee | null
  openModal: OpenModal
  onBack: () => void
}

// Feature flag: the logged/unpaid hours tables are hidden until that data is
// wired to the API. Flip to true to restore both sections (they still render
// MOCK_LOGGED_HOURS placeholder rows). A named flag keeps the intent explicit.
const SHOW_HOURS_TABLES = false

// Logged/Unpaid hours share the same accordion + table layout, differing only in
// title and expanded state. Kept in one place so the two sections don't drift.
interface HoursAccordionProps {
  title: string
  expanded: boolean
  onToggle: () => void
  chevron: string
  className?: string
}

const HoursAccordion: React.FC<HoursAccordionProps> = ({
  title,
  expanded,
  onToggle,
  chevron,
  className = ''
}) => (
  <div className={`accordion-section ${className}`}>
    <h3 className="department-title" onClick={onToggle}>
      {title} <span className={`chevron ${expanded ? 'expanded' : ''}`}>{chevron}</span>
    </h3>

    {expanded && (
      <div className="card-box table-box">
        <div className="table-header-grid">
          <span>Date</span>
          <span>Start</span>
          <span>End</span>
          <span>Duration</span>
        </div>
        {MOCK_LOGGED_HOURS.map((log, i) => (
          <div key={i} className="table-row-grid">
            <span>{log.date}</span>
            <span>{log.start}</span>
            <span>{log.end}</span>
            <span>{log.duration}</span>
          </div>
        ))}
        <div className="table-footer-actions">
          <button className="btn-tiny-pill">show more</button>
          <button className="btn-tiny-pill">show less</button>
        </div>
      </div>
    )}
  </div>
)

const ProfilePage: React.FC<Props> = ({ isMyProfile, employee, openModal, onBack }) => {
  const { currentUser } = useAuth()
  const [loggedExpanded, setLoggedExpanded] = useState(true)
  const [unpaidExpanded, setUnpaidExpanded] = useState(true)

  const targetName = isMyProfile
    ? [currentUser?.name, currentUser?.surname].filter(Boolean).join(' ')
    : [employee?.name, employee?.surname].filter(Boolean).join(' ') || 'Fallback Name'
  const targetEmail = isMyProfile
    ? (currentUser?.email ?? '')
    : employee?.email || 'fallback_email@emppulse.com'

  return (
    <div className="screen-container">
      <button className="btn-pill-secondary" onClick={onBack}>
        <img src={blackTriangleIcon} alt="Back to employees list" />
      </button>
      <header className="page-header profile-header">
        <h2>{isMyProfile ? 'My Profile' : "Employee's Profile"}</h2>
        {isMyProfile && (
          <button className="btn-logout-pill" onClick={() => openModal('LOGOUT')}>
            log out
          </button>
        )}
      </header>

      <div className="profile-top-grid">
        <div className="profile-banner">
          <div className="banner-info">
            <h3>{targetName}</h3>
            <p className="email-sub">{targetEmail}</p>
            {/*
              HIDDEN FOR NOW (not deleted): "Change password" button.
              Intentionally not rendered on the user's own profile until the
              change-password flow is wired to the API. Restore by re-enabling
              this button for the isMyProfile case.
            */}
            {!isMyProfile && (
              <button
                className="btn-pill-action"
                onClick={() => openModal('EDIT_EMPLOYEE', employee as Employee)}
              >
                Edit profile
              </button>
            )}
          </div>
          <div className="banner-stats">
            <div>
              <label>Department:</label>
              <span></span>
            </div>
            <div>
              <label>Role:</label>
              <span></span>
            </div>
            <div>
              <label>Status:</label>
              <span></span>
            </div>
          </div>
        </div>

        <div className="vacation-widget">
          <h4>Vacation balance</h4>
          <div className="balance-badge-card">Vacations day left: </div>
        </div>
      </div>

      <div className="accordion-section">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px' }}>
          <h3 className="department-title" style={{ marginBottom: 0, fontSize: '20px' }}>
            Default working hours
          </h3>
          <span className="chevron expanded">🡇</span>
        </div>

        <div
          className="card-box"
          style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '24px' }}
        >
          {MOCK_DEFAULT_WORKING_HOURS.map((column, ci) => (
            <div className="shifts-stack" key={ci}>
              {column.map((day, di) => (
                <React.Fragment key={day.label}>
                  <div className="day-label" style={di > 0 ? { marginTop: '16px' } : undefined}>
                    {day.label}
                  </div>
                  {day.shifts.map((shift, si) => (
                    <div className="shift-pill-row" key={si}>
                      <span className="shift-index">{si + 1})</span>
                      <div className="time-range-display">
                        <span>{shift.start}</span> <span className="muted-separator">—</span>{' '}
                        <span>{shift.end}</span>
                      </div>
                    </div>
                  ))}
                </React.Fragment>
              ))}
            </div>
          ))}
        </div>

        {!isMyProfile && (
          <div className="center-action tight">
            <button
              className="primary-btn"
              onClick={() => openModal('EDIT_WORKING_HOURS', employee as Employee)}
            >
              edit working hours
            </button>
          </div>
        )}
      </div>

      {/* HIDDEN FOR NOW (not deleted): "Logged hours" table — see SHOW_HOURS_TABLES. */}
      {SHOW_HOURS_TABLES && (
        <HoursAccordion
          title="Logged hours"
          expanded={loggedExpanded}
          onToggle={() => setLoggedExpanded(!loggedExpanded)}
          chevron="►"
        />
      )}

      {!isMyProfile && (
        <div className="center-action tight">
          <button
            className="primary-btn"
            onClick={() => openModal('LOG_HOURS', employee as Employee)}
          >
            + log hours
          </button>
        </div>
      )}

      {/* HIDDEN FOR NOW (not deleted): "Unpaid hours" table — see SHOW_HOURS_TABLES. */}
      {SHOW_HOURS_TABLES && (
        <HoursAccordion
          title="Unpaid hours"
          expanded={unpaidExpanded}
          onToggle={() => setUnpaidExpanded(!unpaidExpanded)}
          chevron="🡇"
          className={isMyProfile ? '' : 'no-top-margin'}
        />
      )}
    </div>
  )
}

export default ProfilePage
