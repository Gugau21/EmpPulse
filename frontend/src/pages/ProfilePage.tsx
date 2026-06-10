import React, { useState } from 'react'
import { useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { Employee } from '../types'
import type { OutletContext } from '../components/AppLayout'
import { MOCK_LOGGED_HOURS, MOCK_DEFAULT_WORKING_HOURS } from '../utils/mockData'
import { useAuth } from '../context/useAuth'
import { useUserDetail } from '../hooks/useUserDetail'
import { landingPath } from '../utils/guards'
import blackTriangleIcon from '../assets/black_triangle.png'

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
  chevron: React.ReactNode
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

const ProfilePage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const navigate = useNavigate()
  // A :userId param means we're viewing someone else's profile (employee mode);
  // no param means the signed-in user's own profile.
  const { userId } = useParams()
  const isMyProfile = userId == null
  const { currentUser } = useAuth()
  const [loggedExpanded, setLoggedExpanded] = useState(true)
  const [unpaidExpanded, setUnpaidExpanded] = useState(true)
  const [workingHoursExpanded, setWorkingHoursExpanded] = useState(true)

  // Parse userId as number, but guard against NaN (e.g., /employees/not-a-number)
  const parsedId = userId ? Number(userId) : null
  const employeeId = parsedId && !isNaN(parsedId) ? parsedId : null
  const { data: employeeUser } = useUserDetail(employeeId)

  // Route back based on user role: employees go to /my-requests, others to /employees
  const onBack = () => {
    const backPath = landingPath(currentUser)
    navigate(backPath)
  }

  // The modals (EDIT_EMPLOYEE/LOG_HOURS) take an Employee; in employee mode we
  // rebuild one from the fetched user. EditEmployeeModal only needs the id (it
  // refetches the rest), but we fill the display fields too for completeness.
  const employee: Employee | null = employeeUser
    ? {
        id: String(employeeUser.id),
        name: employeeUser.name,
        surname: employeeUser.surname,
        email: employeeUser.email,
        department: employeeUser.employeeProfile?.departmentName ?? undefined
      }
    : null

  const targetName = isMyProfile
    ? [currentUser?.name, currentUser?.surname].filter(Boolean).join(' ')
    : [employeeUser?.name, employeeUser?.surname].filter(Boolean).join(' ') || 'Fallback Name'
  const targetEmail = isMyProfile ? (currentUser?.email ?? '') : (employeeUser?.email ?? '')
  // The vacation widget only applies to employees; non-employee users (e.g.
  // admin-only) have no balance to show. Drives both the own- and looked-up cases.
  const targetUser = isMyProfile ? currentUser : employeeUser
  const employeeProfile = targetUser?.employeeProfile ?? null

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
          <div className="banner-top-row">
            <div className="banner-main-info">
              <h3>{targetName}</h3>
              <p className="email-sub">{targetEmail}</p>

              <div className="banner-stacked-detail">
                <label>Department:</label>
                <span>{employeeProfile?.departmentName || 'Department1'}</span>
              </div>

              {/* Administrator info (Displays based on data, mocked for UI right now) */}
              <div className="banner-stacked-detail">
                <label>Administrator of:</label>
                <span>
                  {targetUser?.adminProfile?.departmentIds.length
                    ? `Department ${targetUser.adminProfile.departmentIds.join(', ')}`
                    : 'Department1, Department2'}
                </span>
              </div>
            </div>

            <div className="banner-side-info">
              <div className="banner-stacked-detail">
                <label>Status:</label>
                {/* Fallback to Vacation for visual matching if user lacks status */}
                <span>{(targetUser as { status?: string })?.status || 'Vacation'}</span>
              </div>
            </div>
          </div>

          <div className="banner-center-action">
            {isMyProfile ? (
              <button
                className="btn-change-password"
                onClick={() => openModal('CHANGE_PASSWORD_FORM')}
              >
                Change password
              </button>
            ) : (
              <button
                className="btn-change-password"
                onClick={() => openModal('EDIT_EMPLOYEE', employee as Employee)}
              >
                Edit profile
              </button>
            )}
          </div>
        </div>

        {employeeProfile && (
          <div className="vacation-widget">
            <h4>Vacation balance</h4>
            <div className="balance-badge-card">
              Vacations day left: {employeeProfile.yearlyVacationBalance}
            </div>
          </div>
        )}
      </div>

      <div className="accordion-section">
        <h3
          className="department-title"
          onClick={() => setWorkingHoursExpanded(!workingHoursExpanded)}
          style={{
            marginBottom: '20px',
            fontSize: '20px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}
        >
          Default working hours
          <img
            src={blackTriangleIcon}
            alt="Toggle working hours"
            className={`chevron ${workingHoursExpanded ? 'expanded' : ''}`}
          />
        </h3>

        {workingHoursExpanded && (
          <div className="card-box working-hours-grid">
            {MOCK_DEFAULT_WORKING_HOURS.map((column, ci) => (
              <div className="shifts-stack" key={ci}>
                {column.map((day, di) => (
                  <React.Fragment key={day.label}>
                    <div className={`day-label ${di > 0 ? 'day-label-margin' : ''}`}>
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
        )}

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

      <div className="accordion-section">
        <h3
          className="department-title profile-accordion-title"
          onClick={() => setLoggedExpanded(!loggedExpanded)}
          style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          Logged hours
          <img
            src={blackTriangleIcon}
            alt="Toggle logged hours"
            className={`chevron ${loggedExpanded ? 'expanded' : ''}`}
          />
        </h3>

        {loggedExpanded && (
          <div className="card-box table-box">
            <div className="table-header-grid logged-hours-grid">
              <span>Date</span>
              <span>Time interval</span>
              <span>Duration</span>
            </div>

            <div
              className={`table-row-grid logged-hours-grid ${!isMyProfile ? 'clickable' : ''}`}
              onClick={() => {
                if (!isMyProfile) openModal('EDIT_LOGGED_HOURS', employee as Employee)
              }}
              style={{ cursor: !isMyProfile ? 'pointer' : 'default' }}
            >
              <span>28.05.2026</span>
              <span>9:00 - 17:00</span>
              <span>8 hours</span>
            </div>

            <div
              className={`table-row-grid logged-hours-grid ${!isMyProfile ? 'clickable' : ''}`}
              onClick={() => {
                if (!isMyProfile) openModal('EDIT_LOGGED_HOURS', employee as Employee)
              }}
              style={{ cursor: !isMyProfile ? 'pointer' : 'default' }}
            >
              <span>28.05.2026</span>
              <div>
                <span
                  className="badge badge-sick"
                  style={{ padding: '4px 32px', borderRadius: '16px' }}
                >
                  Sick
                </span>
              </div>
              <span>8 hours</span>
            </div>

            <div className="table-footer-actions">
              <button className="btn-tiny-pill wide">show more</button>
              <button className="btn-tiny-pill wide">show less</button>
            </div>
          </div>
        )}
      </div>

      {/* HIDDEN FOR NOW (not deleted): "Logged hours" table — see SHOW_HOURS_TABLES. */}
      {SHOW_HOURS_TABLES && (
        <HoursAccordion
          title="Logged hours"
          expanded={loggedExpanded}
          onToggle={() => setLoggedExpanded(!loggedExpanded)}
          chevron={
            <img
              src={blackTriangleIcon}
              alt=""
              className={`chevron ${loggedExpanded ? 'expanded' : ''}`}
            />
          }
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
