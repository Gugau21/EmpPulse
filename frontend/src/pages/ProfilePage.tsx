import React, { useState } from 'react'
import { Navigate, useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { Employee } from '../types'
import type { OutletContext } from '../components/AppLayout'
import { useAuth } from '../context/useAuth'
import { useUserDetail } from '../hooks/useUserDetail'
import { useDepartmentsList } from '../hooks/useDepartmentsList'
import { useLoggedHours } from '../hooks/useLoggedHours'
import { useEmployeeDefaultHours } from '../hooks/useDefaultHours'
import { scheduleFromDays } from '../utils/defaultHours'
import { landingPath } from '../utils/guards'
import { describeActiveLeave } from '../utils/activeLeave'
import { isoToDisplayDate } from '../utils/date'
import { buildLoggedHoursDays, formatDuration, formatInterval } from '../utils/loggedHours'
import blackTriangleIcon from '../assets/black_triangle.png'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import { useWeekdayLabels, WEEKDAYS } from '../hooks/useWeekdayLabels'
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
  const weekdayLabels = useWeekdayLabels()
  // The owner's own profile is a stripped-down view: no department/admin/status
  // banner details and no working-hours/logged-hours tables (none apply to them).
  const isOwnerPersonal = isMyProfile && isOwner
  const [loggedExpanded, setLoggedExpanded] = useState(true)
  const [workingHoursExpanded, setWorkingHoursExpanded] = useState(true)

  // Parse userId as number, but guard against NaN (e.g., /employees/not-a-number)
  const parsedId = userId ? Number(userId) : null
  const employeeId = parsedId && !isNaN(parsedId) ? parsedId : null
  const { data: employeeUser, isLoading, error } = useUserDetail(employeeId)

  // Logged hours are read for the profile's own user id (the API keys them by user
  // id): the viewed employee, or the signed-in user on their own profile. The flat
  // rows are regrouped into one box per day that has logged hours, newest first.
  const profileEmployeeId = isMyProfile ? (currentUser?.id ?? null) : employeeId
  const { data: loggedHoursData, error: loggedHoursError } = useLoggedHours(profileEmployeeId)
  const loggedHoursDays = buildLoggedHoursDays(loggedHoursData ?? [])

  // Default working hours are admin-only to read, so only fetch when an admin is
  // viewing an employee's profile (not on one's own). Empty until set.
  const { data: defaultHoursData } = useEmployeeDefaultHours(isMyProfile ? null : employeeId)
  const defaultHoursSchedule = scheduleFromDays(defaultHoursData ?? [])

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
  // Translated label for the status badge; the raw status still drives the CSS class.
  const statusLabels: Record<NonNullable<typeof status>, string> = {
    Vacation: t.statusVacation,
    Sick: t.statusSick,
    Personal: t.statusPersonal
  }

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
                  <label>{t.statusLabel}</label>
                  {status ? (
                    <span className="banner-status">
                      <span className={`badge badge-${status.toLowerCase()}`}>
                        {statusLabels[status]}
                      </span>
                      {untilDate && (
                        <span className="until-text">
                          {t.until} {untilDate}
                        </span>
                      )}
                    </span>
                  ) : (
                    <span>{t.working}</span>
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
              {t.daysLeft} {employeeProfile.vacationBalance}
              {/* Granting bonus days is an admin action: only shown when viewing
                someone else's profile (the employees list is gated to owners/
                admins), so an employee never sees it on their own profile. */}
              {!isMyProfile && (
                <button
                  className="primary-btn full-width"
                  onClick={() => openModal('ADD_BONUS_DAYS', employee as Employee)}
                >
                  add bonus days
                </button>
              )}
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
              {/* One column per weekday; the day's interval shows when set. */}
              <div className="card-box working-hours-grid">
                {WEEKDAYS.map(day => {
                  const shift = defaultHoursSchedule[day]?.[0]
                  return (
                    <div className="shifts-stack" key={day}>
                      <div className="day-label">{weekdayLabels[day] || day}</div>
                      {shift && (
                        <div className="shift-pill-row">
                          <span className="time-range-display">
                            {shift.start} - {shift.end}
                          </span>
                        </div>
                      )}
                    </div>
                  )
                })}
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
              {loggedHoursError && (
                <p className="form-error form-error-block">{loggedHoursError.message}</p>
              )}
              <div className="card-box table-box">
                <div className="table-header-grid logged-hours-grid">
                  <span>{t.dateCol}</span>
                  <span>{t.timeIntervalCol}</span>
                  <span>{t.durationCol}</span>
                </div>

                {/* One box per day that has logged hours; days with nothing logged
                  are not shown. On multi-interval days only the first row carries
                  the date and the day's total duration. */}
                {loggedHoursDays.map(day => (
                  <div className="card-box table-box" key={day.date}>
                    {day.intervals.map((interval, idx) => (
                      <LoggedHoursRow
                        key={interval.id}
                        isMyProfile={isMyProfile}
                        onEdit={() =>
                          openModal(
                            'EDIT_LOGGED_HOURS',
                            employee as Employee,
                            undefined,
                            undefined,
                            interval
                          )
                        }
                      >
                        <span>{idx === 0 ? isoToDisplayDate(day.date) : ''}</span>
                        <span>{formatInterval(interval.startTime, interval.endTime)}</span>
                        <span>{idx === 0 ? formatDuration(day.durationMinutes) : ''}</span>
                      </LoggedHoursRow>
                    ))}
                  </div>
                ))}
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
