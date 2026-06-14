import React, { useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import trashIcon from '../assets/bin_icon_dark.png'
import trashIconLight from '../assets/bin_icon_light.png'
import type { OutletContext } from '../components/AppLayout'
import { useAuth } from '../context/useAuth'
import { useDepartmentsList } from '../hooks/useDepartmentsList'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import { useTheme } from '../hooks/useTheme'

const DepartmentsScreen: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const navigate = useNavigate()
  const { theme } = useTheme()
  const { currentUser, isOwner } = useAuth()
  const { language } = useLanguage()
  const t = translations[language].departmentsPage
  const departmentsQuery = useDepartmentsList()
  const departments = departmentsQuery.data ?? []
  const loading = departmentsQuery.isLoading
  const [searchTerm, setSearchTerm] = useState('')

  // Admins may only see the departments they administer.
  const visibleDepartments = isOwner
    ? departments
    : departments.filter(dept => currentUser?.adminProfile?.departmentIds.includes(dept.id))

  const filteredDepartments = visibleDepartments.filter(dept =>
    dept.name.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="screen-container">
      <header className="page-header">
        <h2>{t.title}</h2>
        <div className="header-actions">
          <div className="search-bar">
            <input
              type="text"
              placeholder={t.searchPlaceholder}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              maxLength={100}
            />
          </div>
        </div>
      </header>

      {(loading || filteredDepartments.length > 0) && (
        <div className="card-box list-box">
          {loading && (
            <div className="employee-row">
              <span className="emp-name">{t.loading}</span>
            </div>
          )}
          {!loading &&
            filteredDepartments.map(dept => (
              <div
                key={dept.id}
                className="employee-row hover-slide-container clickable"
                onClick={() => navigate(`/departments/${dept.id}`)}
              >
                <div className="dept-info-stack">
                  <span className="emp-name">{dept.name}</span>
                </div>

                {isOwner && (
                  <button
                    className="slide-bin-btn"
                    onClick={e => {
                      e.stopPropagation()
                      openModal('DELETE_DEPARTMENT', dept)
                    }}
                    title={t.deleteTitle}
                  >
                    <img
                      src={theme === 'dark' ? trashIconLight : trashIcon}
                      alt="Delete"
                      width={30}
                      height={30}
                    />
                  </button>
                )}
              </div>
            ))}
        </div>
      )}

      {isOwner && (
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('ADD_DEPARTMENT')}>
            {t.addDepartment}
          </button>
        </div>
      )}
    </div>
  )
}

export default DepartmentsScreen
