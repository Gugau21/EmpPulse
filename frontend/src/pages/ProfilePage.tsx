import React, { useState } from 'react'
import { Navigate, useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { Employee } from '../types'
import type { OutletContext } from '../components/AppLayout'
import { MOCK_DEFAULT_WORKING_HOURS } from '../utils/mockData'
import { useAuth } from '../context/useAuth'
import { useUserDetail } from '../hooks/useUserDetail'
import { useDepartmentsList } from '../hooks/useDepartmentsList'
import { landingPath } from '../utils/guards'
import { describeActiveLeave } from '../utils/activeLeave'
import blackTriangleIcon from '../assets/black_triangle.png'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import whiteTriangleIcon from '../assets/white_triangle.png'
import { useTheme } from '../hooks/useTheme'

// A single row in the "Logged hours" table. The clickable/edit wiring is
// identical across rows, so it lives here to avoid duplicating the wrapper.
interface LoggedHoursRowProps {
  isMyProfile: boolean
  onEdit: () => void
  children: React.ReactNode
}

const LoggedHoursRow: React.FC<LoggedHoursRowProps> = ({ isMyProfile, onEdit, children }) => (
  <div
    className={`table-row-grid logged-hours-grid ${!isMyProfile ? 'clickable' : ''}`}
    onClick={() => {
      if (!isMyProfile) onEdit()
    }}
    style={{ cursor: !isMyProfile ? 'pointer' : 'default' }}
  >
    {children}
  </div>
)

const ProfilePage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const navigate = useNavigate()
  const { theme } = useTheme()
  // A :userId param means we're viewing someone else's profile (employee mode);
  // no param means the signed-in user's own profile.
  const { userId } = useParams()
  const isMyProfile = userId == null
  const { currentUser, isOwner } = useAuth()
  const { language } = useLanguage()
  const t = translations[language].profilePage
  // The owner's own profile is a stripped-down view: no department/admin/status
  // banner details and no working-hours/logged-hours tables (none apply to them).
  const isOwnerPersonal = isMyProfile && isOwner
  const [loggedExpanded, setLoggedExpanded] = useState(true)
  const [workingHoursExpanded, setWorkingHoursExpanded] = useState(true)

  // Parse userId as number, but guard against NaN (e.g., /employees/not-a-number)
  const parsedId = userId ? Number(userId) : null
  const employeeId = parsedId && !isNaN(parsedId) ? parsedId : null
  const { data: employeeUser, isLoading, error } = useUserDetail(employeeId)

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
    : [employeeUser?.name, employeeUser?.surname].filter(Boolean).join(' ')
  const targetEmail = isMyProfile ? (currentUser?.email ?? '') : (employeeUser?.email ?? '')
  // The vacation widget only applies to employees; non-employee users (e.g.
  // admin-only) have no balance to show. Drives both the own- and looked-up cases.
  const targetUser = isMyProfile ? currentUser : employeeUser
  const employeeProfile = targetUser?.employeeProfile ?? null
  const adminDepartmentIds = targetUser?.adminProfile?.departmentIds ?? []
  // Current leave from the profile's active leave (null = working, no badge).
  const { status, untilDate } = describeActiveLeave(employeeProfile?.activeLeave)

  // Resolve admin department IDs to real names. The list is gated to owners/admins
  // and an admin only receives their own departments, so a name may be missing
  // (e.g. an admin viewing another admin) — fall back to the ID in that case.
  const { data: departments } = useDepartmentsList()
  const departmentNamesById = new Map((departments ?? []).map(d => [d.id, d.name]))
  const adminDepartmentNames = adminDepartmentIds
    .map(id => departmentNamesById.get(id) ?? `Department ${id}`)
    .sort((a, b) => a.localeCompare(b))

  // Viewing your own id in employee mode (e.g. an admin who appears in the
  // employees list and clicks themselves) is really the personal profile —
  // send it to /profile so the own-profile view (log out, change password)
  // renders instead of the read-only employee one.
  if (!isMyProfile && currentUser != null && employeeId === currentUser.id) {
    return <Navigate to="/profile" replace />
  }

  // In employee mode, don't render the profile shell until the user is loaded:
  // the banner would otherwise show empty fields and the action buttons would
  // pass a null employee to their modals. Covers a malformed :userId, the
  // in-flight fetch, and a fetch that failed after retries.
  if (!isMyProfile && employeeUser == null) {
    return (
      <div className="screen-container">
        <button className="btn-pill-secondary" onClick={onBack}>
          <img
            src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon}
            alt="Back to employees list"
          />
        </button>
        {employeeId == null ? (
          <p className="form-error">{t.doesNotExist}</p>
        ) : isLoading ? (
          <p className="muted">{t.loading}</p>
        ) : (
          <p className="form-error">{error?.message ?? t.failedLoad}</p>
        )}
      </div>
    )
  }

  return (
    <div className="screen-container">
      <button className="btn-pill-secondary" onClick={onBack}>
        <img
          src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon}
          alt="Back to employees list"
        />
      </button>
      <header className="page-header profile-header">
        <h2>{isMyProfile ? t.myProfile : t.userProfile}</h2>
        {isMyProfile && (
          <button className="btn-logout-pill" onClick={() => openModal('LOGOUT')}>
            {t.logOut}
          </button>
        )}
      </header>

      <div className="profile-top-grid">
        <div className="profile-banner">
          <div className="banner-top-row">
            <div className="banner-main-info">
              <h3>{targetName}</h3>
              <p className="email-sub">{targetEmail}</p>

              {/* Department only applies to employees; admin-only users have none. */}
              {!isOwnerPersonal && employeeProfile && (
                <div className="banner-stacked-detail">
                  <label>{t.departmentLabel}</label>
                  <span>{employeeProfile.departmentName ?? 'None'}</span>
                </div>
              )}

              {/* "Administrator of" only shown when the user actually administers a department. */}
              {!isOwnerPersonal && adminDepartmentIds.length > 0 && (
                <div className="banner-stacked-detail">
                  <label>{t.adminOfLabel}</label>
                  <span>{adminDepartmentNames.join(', ')}</span>
                </div>
              )}
            </div>

            {/* Current leave status. Only employees have one; a working employee
                (no active leave) shows "Working" rather than a badge. */}
            {!isOwnerPersonal && employeeProfile && (
              <div className="banner-side-info">
                <div className="banner-stacked-detail">
                  <label>Status:</label>
                  {status ? (
                    <span className="banner-status">
                      <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>
                      {untilDate && <span className="until-text">until {untilDate}</span>}
                    </span>
                  ) : (
                    <span>Working</span>
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="banner-center-action">
            {isMyProfile ? (
              <button
                className="btn-change-password"
                onClick={() => openModal('CHANGE_PASSWORD_FORM')}
              >
                {t.changePassword}
              </button>
            ) : (
              <button
                className="btn-change-password"
                onClick={() => openModal('EDIT_EMPLOYEE', employee as Employee)}
              >
                {t.editProfile}
              </button>
            )}
          </div>
        </div>

        {employeeProfile && (
          <div className="vacation-widget">
            <h4>{t.vacationBalance}</h4>
            <div className="balance-badge-card">
              {t.daysLeft} {employeeProfile.yearlyVacationBalance}
            </div>
          </div>
        )}
      </div>

      {!isOwnerPersonal && (
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
            {t.defaultWorkingHours}
            <img
              src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon}
              alt="Toggle working hours"
              className={`chevron ${workingHoursExpanded ? 'expanded' : ''}`}
            />
          </h3>

          {workingHoursExpanded && (
            <>
              <div className="card-box working-hours-grid">
                {MOCK_DEFAULT_WORKING_HOURS.map((column, ci) => (
                  <div className="shifts-stack" key={ci}>
                    {column.map((day, di) => {
                      // Map the hardcoded English day to our translation
                      const translatedDays: Record<string, string> = {
                        Monday: t.monday, Tuesday: t.tuesday, Wednesday: t.wednesday,
                        Thursday: t.thursday, Friday: t.friday, Saturday: t.saturday, Sunday: t.sunday
                      };
                      const displayDay = translatedDays[day.label] || day.label;

                      return (
                        <React.Fragment key={day.label}>
                          <div className={`day-label ${di > 0 ? 'day-label-margin' : ''}`}>
                            {displayDay}
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
                      )
                    })}
                  </div>
                ))}
              </div>

              {!isMyProfile && (
                <div className="center-action tight">
                  <button
                    className="primary-btn"
                    onClick={() => openModal('EDIT_WORKING_HOURS', employee as Employee)}
                  >
                    {t.editWorkingHours}
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {!isOwnerPersonal && (
        <div className="accordion-section">
          <h3
            className="department-title profile-accordion-title"
            onClick={() => setLoggedExpanded(!loggedExpanded)}
            style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}
          >
            {t.loggedHours}
            <img
              src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon}
              alt="Toggle logged hours"
              className={`chevron ${loggedExpanded ? 'expanded' : ''}`}
            />
          </h3>

          {loggedExpanded && (
            <>
              <div className="card-box table-box">
                <div className="table-header-grid logged-hours-grid">
                  <span>{t.dateCol}</span>
                  <span>{t.timeIntervalCol}</span>
                  <span>{t.durationCol}</span>
                </div>

                <LoggedHoursRow
                  isMyProfile={isMyProfile}
                  onEdit={() => openModal('EDIT_LOGGED_HOURS', employee as Employee)}
                >
                  <span>28.05.2026</span>
                  <span>9:00 - 17:00</span>
                  <span>8 hours</span>
                </LoggedHoursRow>

                <LoggedHoursRow
                  isMyProfile={isMyProfile}
                  onEdit={() => openModal('EDIT_LOGGED_HOURS', employee as Employee)}
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
                </LoggedHoursRow>

                <div className="table-footer-actions">
                  <button className="btn-tiny-pill wide">{t.showMore}</button>
                  <button className="btn-tiny-pill wide">{t.showLess}</button>
                </div>
              </div>

              {!isMyProfile && (
                <div className="center-action tight">
                  <button
                    className="primary-btn"
                    onClick={() => openModal('LOG_HOURS', employee as Employee)}
                  >
                    {t.logHours}
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}

export default ProfilePage
