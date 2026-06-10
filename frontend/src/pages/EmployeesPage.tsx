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

  // Search matches a surname *prefix* only (not a substring, and not the first
  // name) — the box is labelled "Search by surname" and users expect to type the
  // start of a last name, as in a phone-book lookup.
  const query = search.trim().toLowerCase()
  const filtered = query
    ? employees.filter(emp => (emp.surname ?? '').toLowerCase().startsWith(query))
    : employees

  const groups = groupByDepartment(filtered)

  const toggle = (dept: string) => setCollapsed(prev => ({ ...prev, [dept]: !prev[dept] }))

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
              maxLength={50}
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
                {deptEmployees.map(emp => (
                  <div
                    key={emp.id}
                    className="employee-row hover-slide-container"
                    onClick={() => navigate(`/employees/${emp.id}`)}
                  >
                    <span className="emp-name">
                      {[emp.name, emp.surname].filter(Boolean).join(' ')}
                    </span>
                    <div className="emp-meta">
                      {/*
                        HIDDEN FOR NOW: leave status badge + "until" date.
                        GET /api/employees returns no leave/status data, so these are
                        intentionally not rendered. Restore once a leave API is wired:
                        re-enable the badge/until-text below and populate emp.status /
                        emp.untilDate from that endpoint.

                        {emp.status && emp.status !== 'Working' && (
                          <span className={`badge badge-${emp.status.toLowerCase()}`}>{emp.status}</span>
                        )}
                        {emp.untilDate && <span className="until-text">untill {emp.untilDate}</span>}
                      */}
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
                ))}
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
