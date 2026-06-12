import React from 'react'
import { useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { OutletContext } from '../components/AppLayout'
import { useAuth } from '../context/useAuth'
import { useDepartmentDetail } from '../hooks/useDepartmentDetail'
import blackTriangleIcon from '../assets/black_triangle.png'
import whiteTriangleIcon from '../assets/white_triangle.png'
import EditIcon from '../assets/edit_icon.png' 
import EditIconLight from '../assets/edit_icon_light.png'
import { useTheme } from '../hooks/useTheme'

// NOTE: The default working-hours feature is not wired to the API yet, so the
// working-hours table and its button are commented out below (kept for later).
// const allDaysOrdered: ('Monday' | 'Tuesday' | 'Wednesday' | 'Thursday' | 'Friday' | 'Saturday' | 'Sunday')[] = [
//   'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'
// ];

const DepartmentDetailScreen: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const navigate = useNavigate()
  const { deptId } = useParams()
  const { isOwner } = useAuth()
  const { theme } = useTheme()
  // Guard against NaN from invalid route params
  const parsedId = deptId ? Number(deptId) : null
  const validId = parsedId && !isNaN(parsedId) ? parsedId : null
  const detailQuery = useDepartmentDetail(validId)
  const department = detailQuery.data ?? null
  const loading = detailQuery.isLoading
  const onBack = () => navigate('/departments')
  if (loading && !department) {
    return (
      <div className="screen-container">
        <p>Loading department…</p>
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
          🡄 Back
        </button>
      </div>
    )
  }

  return (
    <div className="screen-container department-detail-screen">
      <header className="page-header">
        <div className="department-detail-title">
          <button className="btn-pill-secondary" onClick={onBack}>
            <img src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon} alt="Back to departments list" />
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
          <h3 className="column-section-title">Administrators</h3>
          <div className="card-box list-box department-detail-card">
            {department.admins.length === 0 && (
              <div className="admin-block-item">No administrators assigned.</div>
            )}
            {department.admins.map(admin => (
              <div key={admin.id} className="admin-block-item">
                {admin.user.name} {admin.user.surname}
              </div>
            ))}
          </div>
          {isOwner && (
            <div className="detail-action-row">
              <button className="primary-btn" onClick={() => openModal('EDIT_ADMINS', department)}>
                edit admins
              </button>
            </div>
          )}
        </div>

        {/*
        <div className="detail-column">
          <h3 className="column-section-title">Default working hours</h3>
          <div className="card-box schedule-matrix-card">
            {allDaysOrdered.map((day) => {
              const shifts = getShiftsForDay(day);
              if (shifts.length === 0) return null;

              return (
                <div key={day} className="day-schedule-group">
                  <span className="day-label">{day}</span>
                  <div className="shifts-stack">
                    {shifts.map((shift, sIdx) => (
                      <div key={sIdx} className="shift-pill-row">
                        <span className="shift-index">{sIdx + 1})</span>
                        <div className="time-range-display">
                          <span>{shift.start}</span>
                          <span className="muted-separator">—</span>
                          <span>{shift.end}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
          <div className="detail-action-row">
            <button
              className="primary-btn"
              onClick={() => openModal('EDIT_WORKING_HOURS', department)}
            >
              edit working hours
            </button>
          </div>
        </div>
        */}
      </div>
    </div>
  )
}

export default DepartmentDetailScreen
