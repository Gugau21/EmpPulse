import React, { useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import type { Employee } from '../types'
import type { OutletContext } from '../components/AppLayout'
import trashIcon from '../assets/bin_icon_dark.png'
import trashIconLight from '../assets/bin_icon_light.png'
import blackTriangleIcon from '../assets/black_triangle.png'
import whiteTriangleIcon from '../assets/white_triangle.png'
import { useEmployeesList } from '../hooks/useEmployeesList'
import { useDepartmentsList } from '../hooks/useDepartmentsList'
import { useAuth } from '../context/useAuth'
import FilterDropdown from '../components/FilterDropdown'
import { useTheme } from '../hooks/useTheme'

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
  const { theme } = useTheme()
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
  // Department filter. Options are built from the departments the user can see —
  // the list is already scoped server-side (the owner gets all, an admin only the
  // departments they oversee), so it needs no extra role handling here.
  const [deptFilter, setDeptFilter] = useState<string[]>([])

  const isLoading = employeesLoading || departmentsLoading
  const isError = employeesError || departmentsError
  const error = employeesErr ?? departmentsErr

  const query = search.trim().toLowerCase()
  const matchesPerson = (surname: string) => !query || surname.toLowerCase().includes(query)

  const employeesByDept = groupByDepartment(
    employees.filter(emp => matchesPerson(emp.surname ?? ''))
  )

  const toggle = (deptId: number) => setCollapsed(prev => ({ ...prev, [deptId]: !prev[deptId] }))

  const departmentFilterOptions = departments.map(dept => ({
    value: String(dept.id),
    label: dept.name
  }))
  const visibleDepartments = deptFilter.length
    ? departments.filter(dept => deptFilter.includes(String(dept.id)))
    : departments

  // A single person row. `deleteEmployee` is passed only for employee rows owned
  // by the owner, who may remove them from the department; admin rows are never
  // deletable here (admin assignment is managed via the department's admins modal).
  // `status`/`untilDate` come from the employee's active leave (absent = working,
  // and admins are listed without a status).
  const renderPersonRow = (
    person: { id: string; name: string; surname: string } & Pick<Employee, 'status' | 'untilDate'>,
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
          {/* On-leave employees show a type badge and the date they return; a
              working employee (no active leave) shows nothing. */}
          {person.status && (
            <span className={`badge badge-${person.status.toLowerCase()}`}>{person.status}</span>
          )}
          {person.untilDate && <span className="until-text">until {person.untilDate}</span>}
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
              <img
                src={theme === 'dark' ? trashIconLight : trashIcon}
                alt="Delete"
                width={30}
                height={30}
              />
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
              style={{ width: '16ch' }}
            />
          </div>
          <FilterDropdown
            label="Filter by department"
            options={departmentFilterOptions}
            selected={deptFilter}
            onChange={setDeptFilter}
          />
        </div>
      </header>

      {isLoading && <p className="muted">Loading employees…</p>}
      {isError && <p className="form-error">{error?.message ?? 'Failed to load employees.'}</p>}

      {visibleDepartments.map(dept => {
        const expanded = !collapsed[dept.id]
        // A user who is both an admin and an employee of this department appears
        // in both lists by design.
        const admins = dept.admins.filter(a => matchesPerson(a.user.surname ?? ''))
        const deptEmployees = employeesByDept.get(dept.name) ?? []
        return (
          <div className="accordion-section" key={dept.id}>
            <h3 className="department-title" onClick={() => toggle(dept.id)}>
              {dept.name}
              <img
                src={theme === 'dark' ? whiteTriangleIcon : blackTriangleIcon}
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
