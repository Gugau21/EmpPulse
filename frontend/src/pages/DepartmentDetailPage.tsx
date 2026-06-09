import React from 'react'
import type { Department, OpenModal } from '../types'
import { useAuth } from '../context/useAuth'
import blackTriangleIcon from '../assets/black_triangle.png'
import EditIcon from '../assets/edit_icon.png'

interface Props {
  department: Department | null
  loading?: boolean
  openModal: OpenModal
  onBack: () => void
}

// NOTE: The default working-hours feature is not wired to the API yet, so the
// working-hours table and its button are commented out below (kept for later).
// const allDaysOrdered: ('Monday' | 'Tuesday' | 'Wednesday' | 'Thursday' | 'Friday' | 'Saturday' | 'Sunday')[] = [
//   'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'
// ];

const DepartmentDetailScreen: React.FC<Props> = ({
  department,
  loading = false,
  openModal,
  onBack
}) => {
  const { isOwner } = useAuth()
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
        <p>No department context selected.</p>
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
            <img src={blackTriangleIcon} alt="Back to departments list" />
          </button>
          <div className="department-detail-title_row">
            <h2>{department.name}</h2>
            {isOwner && (
              <button
                className="btn-edit-action"
                onClick={() => openModal('EDIT_DEPARTMENT', department)}
                style={{ marginLeft: '12px' }}
              >
                <img src={EditIcon} alt="Edit department name" />
              </button>
            )}
          </div>
        </div>
      </header>

      <div className="department-detail-grid">
        {/* Administrator Column */}
        <div className="detail-column">
          <h3 className="column-section-title">Admins</h3>
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
