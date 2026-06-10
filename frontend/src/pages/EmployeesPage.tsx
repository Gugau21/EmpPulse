import React, { useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import type { Employee } from '../types'
import type { OutletContext } from '../components/AppLayout'
import trashIcon from '../assets/trash-icon.png.webp'
import blackTriangleIcon from '../assets/black_triangle.png'
import { useEmployeesList } from '../hooks/useEmployeesList'


// Employees with no department are not shown — this page only lists department members.
function groupByDepartment(employees: Employee[]): Map<string, Employee[]> {
  const groups = new Map<string, Employee[]>()
  for (const emp of employees) {
    if (!emp.department) continue
    const existing = groups.get(emp.department)
    if (existing) existing.push(emp)
    else groups.set(emp.department, [emp])
  }
  return groups
}

const EmployeesPage: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const navigate = useNavigate()
  const { data: employees = [], isLoading } = useEmployeesList()
  const [collapsed, setCollapsed] = useState<Record<string, boolean>>({})
  const [search, setSearch] = useState('')

  const query = search.trim().toLowerCase()
  const filtered = query
    ? employees.filter(emp => (emp.surname ?? '').toLowerCase().startsWith(query))
    : employees

  const groups = groupByDepartment(filtered)

  const toggle = (dept: string) => setCollapsed(prev => ({ ...prev, [dept]: !prev[dept] }))

  const renderRow = (emp: Employee) => (
    <div
      key={emp.id}
      className="employee-row hover-slide-container"
      onClick={() => navigate(`/employees/${emp.id}`)}
    >
      <span className="emp-name">
        {[emp.name, emp.surname].filter(Boolean).join(' ')}
      </span>
      <div className="emp-meta">
        <button
          className="slide-bin-btn"
          onClick={e => {
            e.stopPropagation()
            openModal('DELETE_EMPLOYEE', emp)
          }}
          title="Remove from department"
        >
          <img src={trashIcon} alt="Delete" width={30} height={30} />
        </button>
      </div>
    </div>
  )

  return (
    <div className="screen-container">
      <header className="page-header">
        <h2>Employees</h2>
        <div className="header-actions">
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search by surname"
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
          <div className="filter-dropdown">
            <span>Filter by</span>...
          </div>
        </div>
      </header>

      {isLoading && <p className="muted">Loading employees…</p>}

      {[...groups.entries()].map(([dept, deptEmployees]) => {
        const expanded = !collapsed[dept]
        return (
          <div className="accordion-section" key={dept}>
            <h3 className="department-title" onClick={() => toggle(dept)}>
              {dept}
              <img
                src={blackTriangleIcon}
                alt=""
                className={`chevron ${expanded ? 'expanded' : ''}`}
              />
            </h3>

            {expanded && (
              <div className="card-box list-box">
                
                {/* Administrators Section */}
                <div className="role-section-title">
                  Administrators:
                </div>
                {deptEmployees.slice(0, 1).map(renderRow)}

                {/* Employees Section */}
                <div className="role-section-title secondary">
                  Employees:
                </div>
                {deptEmployees.slice(1).map(renderRow)}

              </div>
            )}
          </div>
        )
      })}

      <div className="center-action">
        <button className="primary-btn" onClick={() => openModal('ADD_EMPLOYEE')}>
          + add employee
        </button>
      </div>
    </div>
  )
}

export default EmployeesPage