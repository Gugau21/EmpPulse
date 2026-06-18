import React from 'react'
import { useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import { useAuth } from '../context/useAuth'
import { useDepartmentDetail } from '../hooks/useDepartmentDetail'
import blackTriangleIcon from '../assets/black_triangle.png'
import whiteTriangleIcon from '../assets/white_triangle.png'
import EditIcon from '../assets/edit_icon.png'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import EditIconLight from '../assets/edit_icon_light.png'
import { useTheme } from '../hooks/useTheme'
import { useWeekdayLabels, WEEKDAYS } from '../hooks/useWeekdayLabels'
import { useDepartmentDefaultHours } from '../hooks/useDefaultHours'
import { scheduleFromDays } from '../utils/defaultHours'

const DepartmentDetailScreen: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const navigate = useNavigate()
  const { deptId } = useParams()
  const { isOwner } = useAuth()
  const { language } = useLanguage()
  const t = translations[language].departmentDetail
  const weekdayLabels = useWeekdayLabels()
  const { theme } = useTheme()
  // Guard against NaN from invalid route params
  const parsedId = deptId ? Number(deptId) : null
  const validId = parsedId && !isNaN(parsedId) ? parsedId : null
  const detailQuery = useDepartmentDetail(validId)
  const department = detailQuery.data ?? null
  const loading = detailQuery.isLoading
  const { data: defaultHoursData } = useDepartmentDefaultHours(validId)
  const defaultHoursSchedule = scheduleFromDays(defaultHoursData ?? [])
  const onBack = () => navigate('/departments')
  if (loading && !department) {
    return (
      <div className="screen-container">
        <p>{t.loading}</p>
      </div>
    )
  }

  if (!department) {
    return (
      <div className="screen-container">
        {detailQuery.error ? (
          <p className="form-error form-error-block">{detailQuery.error.message}</p>
        ) : (
          <p>No department context selected.</p>
        )}
        <button className="btn-pill-secondary" onClick={onBack}>
          {t.back}
        </button>
      </div>
    )
  }

  return (
    <div className="screen-container department-detail-screen">
      <header className="page-header">
        <div className="department-detail-title">
          <button className="btn-pill-secondary" onClick={onBack}>
            <img
              src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon}
              alt="Back to departments list"
            />
          </button>
          <div className="department-detail-title-row">
            <h2>{department.name}</h2>
            {isOwner && (
              <button
                className="btn-edit-action"
                onClick={() => openModal('EDIT_DEPARTMENT', department)}
              >
                <img
                  src={theme === 'dark' ? EditIconLight : EditIcon}
                  alt="Edit department name"
                  width={30}
                  height={30}
                />
              </button>
            )}
          </div>
        </div>
      </header>

      <div className="department-detail-grid">
        <div className="detail-column">
          <h3 className="column-section-title">{t.adminsTitle}</h3>
          <div className="card-box list-box department-detail-card">
            {department.admins.length === 0 && <div className="admin-block-item">{t.noAdmins}</div>}
            {department.admins.map(admin => (
              <div key={admin.id} className="admin-block-item">
                {admin.user.name} {admin.user.surname}
              </div>
            ))}
          </div>
          {isOwner && (
            <div className="detail-action-row">
              <button className="primary-btn" onClick={() => openModal('EDIT_ADMINS', department)}>
                {t.editAdmins}
              </button>
            </div>
          )}
        </div>

        <div className="detail-column">
          <h3 className="column-section-title">{t.defaultWorkingHours}</h3>
          {/* One row per weekday; the day's interval shows when set. */}
          <div className="card-box schedule-matrix-card">
            {WEEKDAYS.map(day => {
              const shift = defaultHoursSchedule[day]?.[0]
              return (
                <div key={day} className="day-schedule-group">
                  <span className="day-label">{weekdayLabels[day] || day}</span>
                  <div className="shifts-stack">
                    {shift && (
                      <div className="shift-pill-row">
                        <span className="time-range-display">
                          {shift.start} - {shift.end}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
          {isOwner && (
            <div className="detail-action-row">
              <button
                className="primary-btn"
                onClick={() => openModal('EDIT_DEPARTMENT_WORKING_HOURS', department)}
              >
                {t.editWorkingHours}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default DepartmentDetailScreen
