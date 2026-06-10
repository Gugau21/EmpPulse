import React, { useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import type { Employee } from '../types'
import type { OutletContext } from '../components/AppLayout'
import trashIcon from '../assets/trash-icon.png.webp'
import blackTriangleIcon from '../assets/black_triangle.png'
import { useEmployeesList } from '../hooks/useEmployeesList'
import { useDepartmentsList } from '../hooks/useDepartmentsList'
import { useAuth } from '../context/useAuth'

// Employees grouped by their department NAME (admins are sourced separately from
// the departments list). Employees with no department are not shown — this page
// only lists department members.
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
  // Only owners may remove employees from a department; admins see a plain,
  // non-sliding row with no delete affordance.
  const { isOwner } = useAuth()
  const {
    data: employees = [],
    isLoading: employeesLoading,
    isError: employeesError,
    error: employeesErr
  } = useEmployeesList()
  // Departments carry the per-department admin lists; the employees summary does not.
  const {
    data: departments = [],
    isLoading: departmentsLoading,
    isError: departmentsError,
    error: departmentsErr
  } = useDepartmentsList()
  const [collapsed, setCollapsed] = useState<Record<number, boolean>>({})
  const [search, setSearch] = useState('')

  const isLoading = employeesLoading || departmentsLoading
  const isError = employeesError || departmentsError
  const error = employeesErr ?? departmentsErr

  const query = search.trim().toLowerCase()
  const matchesSearch = (surname: string) =>
    !query || surname.toLowerCase().startsWith(query)

  const employeesByDept = groupByDepartment(
    query ? employees.filter(emp => matchesSearch(emp.surname ?? '')) : employees
  )

  const toggle = (deptId: number) => setCollapsed(prev => ({ ...prev, [deptId]: !prev[deptId] }))

  // A single person row. `deleteEmployee` is passed only for employee rows owned
  // by the owner, who may remove them from the department; admin rows are never
  // deletable here (admin assignment is managed via the department's admins modal).
  const renderPersonRow = (
    person: { id: string; name: string; surname: string },
    keyPrefix: string,
    deleteEmployee?: Employee
  ) => {
    const deletable = isOwner && deleteEmployee != null
    return (
      <div
        key={`${keyPrefix}-${person.id}`}
        className={`employee-row ${deletable ? 'hover-slide-container' : 'clickable'}`}
        onClick={() => navigate(`/employees/${person.id}`)}
      >
        <span className="emp-name">{[person.name, person.surname].filter(Boolean).join(' ')}</span>
        <div className="emp-meta">
          {/* Removing an employee from a department is owner-only. */}
          {deletable && (
            <button
              className="slide-bin-btn"
              onClick={e => {
                e.stopPropagation()
                openModal('DELETE_EMPLOYEE', deleteEmployee)
              }}
              title="Remove from department"
            >
              <img src={trashIcon} alt="Delete" width={30} height={30} />
            </button>
          )}
        </div>
      </div>
    )
  }

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
      {isError && <p className="form-error">{error?.message ?? 'Failed to load employees.'}</p>}

      {departments.map(dept => {
        const expanded = !collapsed[dept.id]
        // A user who is both an admin and an employee of this department appears
        // in both lists by design.
        const admins = dept.admins.filter(a => matchesSearch(a.user.surname ?? ''))
        const deptEmployees = employeesByDept.get(dept.name) ?? []
        return (
          <div className="accordion-section" key={dept.id}>
            <h3 className="department-title" onClick={() => toggle(dept.id)}>
              {dept.name}
              <img
                src={blackTriangleIcon}
                alt=""
                className={`chevron ${expanded ? 'expanded' : ''}`}
              />
            </h3>

            {expanded && (
              <div className="card-box list-box">
                <div className="role-section-title">Administrators:</div>
                {admins.length ? (
                  admins.map(a =>
                    renderPersonRow(
                      { id: String(a.user.id), name: a.user.name, surname: a.user.surname },
                      'admin'
                    )
                  )
                ) : (
                  <div className="muted role-empty">None</div>
                )}

                <div className="role-section-title secondary">Employees:</div>
                {deptEmployees.length ? (
                  deptEmployees.map(emp => renderPersonRow(emp, 'emp', emp))
                ) : (
                  <div className="muted role-empty">None</div>
                )}
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
