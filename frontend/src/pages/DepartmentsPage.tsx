import React, { useState } from 'react'
import type { Department, OpenModal } from '../types'
import trashIcon from '../assets/trash-icon.png.webp'
import { useAuth } from '../context/useAuth'

interface Props {
  departments: Department[]
  loading?: boolean
  openModal: OpenModal
  onSelectDepartment: (dept: Department) => void
}

const DepartmentsScreen: React.FC<Props> = ({
  departments,
  loading = false,
  openModal,
  onSelectDepartment
}) => {
  const { currentUser, isOwner } = useAuth()
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
        <h2>Departments</h2>
        <div className="header-actions">
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      </header>

      <div className="card-box list-box">
        {loading && (
          <div className="employee-row">
            <span className="emp-name">Loading departments…</span>
          </div>
        )}
        {!loading && filteredDepartments.length === 0 && (
          <div className="employee-row">
            <span className="emp-name">No departments yet.</span>
          </div>
        )}
        {!loading &&
          filteredDepartments.map(dept => (
            <div
              key={dept.id}
              className="employee-row hover-slide-container clickable"
              onClick={() => onSelectDepartment(dept)}
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
                  title="Delete Department"
                >
                  <img src={trashIcon} alt="Delete" width={30} height={30} />
                </button>
              )}
            </div>
          ))}
      </div>

      {isOwner && (
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('ADD_DEPARTMENT')}>
            + add department
          </button>
        </div>
      )}
    </div>
  )
}

export default DepartmentsScreen
