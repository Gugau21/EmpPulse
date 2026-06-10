import React from 'react'
import type { Department } from '../../types'
import { isValidEmail } from '../../utils/validation'

// Shared building blocks for the Add/Edit employee modals. Both modals render the
// same role checkboxes and department selects, so the markup lives here once to
// avoid copy-paste drift between them. Styling is in global.css (.role-checkbox*).

interface RoleCheckboxProps {
  label: string
  checked: boolean
  onToggle: () => void
}

export const RoleCheckbox: React.FC<RoleCheckboxProps> = ({ label, checked, onToggle }) => (
  <div className="role-checkbox" onClick={onToggle}>
    <h3>{label}</h3>
    <div className="role-checkbox-box">{checked ? '✕' : ''}</div>
  </div>
)

interface DepartmentSelectProps {
  label: string
  value: number | null
  onChange: (value: number | null) => void
  departments: Department[]
  className?: string
}

export const DepartmentSelect: React.FC<DepartmentSelectProps> = ({
  label,
  value,
  onChange,
  departments,
  className
}) => (
  <label className={className}>
    {label}
    <select
      value={value ?? ''}
      onChange={e => onChange(e.target.value ? Number(e.target.value) : null)}
    >
      <option value="" disabled>
        select department
      </option>
      {departments.map(d => (
        <option key={d.id} value={d.id}>
          {d.name}
        </option>
      ))}
    </select>
  </label>
)

interface MultiDepartmentSelectProps {
  label: string
  value: number[]
  onChange: (value: number[]) => void
  departments: Department[]
}

// Pick several departments at once: chosen ones render as removable pills and the
// dropdown offers only those not yet picked, disappearing once all are selected so
// users aren't left with an empty control. Mirrors the admin picker in
// EditAdminsModal so the "assign to many" UX stays consistent across the app.
export const MultiDepartmentSelect: React.FC<MultiDepartmentSelectProps> = ({
  label,
  value,
  onChange,
  departments
}) => {
  const deptName = (id: number) => departments.find(d => d.id === id)?.name ?? `Department #${id}`
  const available = departments.filter(d => !value.includes(d.id))

  return (
    <>
      <div className="edit-admins-list">
        {value.map((id, idx) => (
          <div key={id} className="edit-admin-item">
            <span className="index">{idx + 1})</span>
            <div className="edit-admin-pill">{deptName(id)}</div>
            <button
              className="btn-remove"
              onClick={() => onChange(value.filter(x => x !== id))}
              title="Remove department"
            >
              —
            </button>
          </div>
        ))}
      </div>
      {available.length > 0 && (
        <label>
          {label}
          <select
            value=""
            onChange={e => {
              const id = Number(e.target.value)
              if (id) onChange(value.includes(id) ? value : [...value, id])
            }}
          >
            <option value="" disabled>
              add department
            </option>
            {available.map(d => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>
        </label>
      )}
    </>
  )
}

interface IdentityFieldsProps {
  name: string
  onName: (value: string) => void
  surname: string
  onSurname: (value: string) => void
  email?: string
  onEmail?: (value: string) => void
  password?: string
  onPassword?: (value: string) => void
  passwordPlaceholder?: string
}

export const IdentityFields: React.FC<IdentityFieldsProps> = ({
  name,
  onName,
  surname,
  onSurname,
  email,
  onEmail,
  password,
  onPassword,
  passwordPlaceholder
}) => (
  <>
    <label>
      First name
      <input type="text" value={name} onChange={e => onName(e.target.value)} maxLength={50} />
    </label>
    <label>
      Surname
      <input type="text" value={surname} onChange={e => onSurname(e.target.value)} maxLength={50} />
    </label>
    {onEmail && (
      <label>
        Email
        <input
          type="email"
          value={email ?? ''}
          onChange={e => onEmail(e.target.value)}
          maxLength={254}
        />
        {email && email.trim() !== '' && !isValidEmail(email) && (
          <span className="form-error">Please enter a valid email address.</span>
        )}
      </label>
    )}
    {onPassword && (
      <label>
        Password
        <input
          type="password"
          placeholder={passwordPlaceholder}
          value={password ?? ''}
          onChange={e => onPassword(e.target.value)}
          maxLength={128}
        />
      </label>
    )}
  </>
)

interface RoleSectionProps {
  // Employee role
  employeeChecked: boolean
  onToggleEmployee: () => void
  employeeDeptId: number | null
  onEmployeeDept: (value: number | null) => void
  // Yearly vacation days for the employee role, kept as a string for the input.
  vacationDays: string
  onVacationDays: (value: string) => void
  // When false the Employee toggle is hidden (the role is forced on); the
  // department select still shows so a department can be chosen. Defaults to true.
  showEmployeeToggle?: boolean
  // Administrator role; hidden entirely when `showAdmin` is false.
  showAdmin: boolean
  adminChecked: boolean
  onToggleAdmin: () => void
  // An admin can oversee several departments, so this is a multi-select.
  adminDeptIds: number[]
  onAdminDepts: (value: number[]) => void
  departments: Department[]
}

// The Employee + Administrator role toggles, each revealing a department select
// when checked. Identical between the add and edit modals, so it lives here once.
export const RoleSection: React.FC<RoleSectionProps> = ({
  employeeChecked,
  onToggleEmployee,
  employeeDeptId,
  onEmployeeDept,
  vacationDays,
  onVacationDays,
  showEmployeeToggle = true,
  showAdmin,
  adminChecked,
  onToggleAdmin,
  adminDeptIds,
  onAdminDepts,
  departments
}) => (
  <>
    {showEmployeeToggle && (
      <RoleCheckbox label="Employee" checked={employeeChecked} onToggle={onToggleEmployee} />
    )}
    {employeeChecked && (
      <>
        <DepartmentSelect
          label="Department"
          className="field-tight"
          value={employeeDeptId}
          onChange={onEmployeeDept}
          departments={departments}
        />
        <label className="field-tight">
          Yearly vacation days
          <input
            type="text"
            inputMode="numeric"
            value={vacationDays}
            onChange={e => onVacationDays(e.target.value.replace(/\D/g, ''))}
          />
        </label>
      </>
    )}

    {showAdmin && (
      <>
        <RoleCheckbox label="Administrator" checked={adminChecked} onToggle={onToggleAdmin} />
        {adminChecked && (
          <MultiDepartmentSelect
            label="Administrator of"
            value={adminDeptIds}
            onChange={onAdminDepts}
            departments={departments}
          />
        )}
      </>
    )}
  </>
)
