import React from 'react'
import type { Department } from '../../types'

// Shared building blocks for the Add/Edit employee modals. Both modals render the
// same role checkboxes and department selects, so the markup lives here once to
// avoid copy-paste drift between them. Styling is in global.css (.role-checkbox*).

interface RoleCheckboxProps {
  label: string
  checked: boolean
  onToggle: () => void
}

// An "✕"-in-a-box toggle with a heading label (Employee / Administrator).
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

// A labelled department dropdown; null value means "nothing chosen yet".
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

interface IdentityFieldsProps {
  name: string
  onName: (value: string) => void
  surname: string
  onSurname: (value: string) => void
  // Email/password are optional: omit the handlers to hide those fields (e.g.
  // an admin editing a profile they may not change credentials on).
  email?: string
  onEmail?: (value: string) => void
  password?: string
  onPassword?: (value: string) => void
  passwordPlaceholder?: string
}

// Name / surname (always) plus optional email & password inputs, shared by the
// add and edit employee modals.
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
      <input type="text" value={name} onChange={e => onName(e.target.value)} />
    </label>
    <label>
      Surname
      <input type="text" value={surname} onChange={e => onSurname(e.target.value)} />
    </label>
    {onEmail && (
      <label>
        Email
        <input type="email" value={email ?? ''} onChange={e => onEmail(e.target.value)} />
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
  // When false the Employee toggle is hidden (the role is forced on); the
  // department select still shows so a department can be chosen. Defaults to true.
  showEmployeeToggle?: boolean
  // Administrator role; hidden entirely when `showAdmin` is false.
  showAdmin: boolean
  adminChecked: boolean
  onToggleAdmin: () => void
  adminDeptId: number | null
  onAdminDept: (value: number | null) => void
  departments: Department[]
}

// The Employee + Administrator role toggles, each revealing a department select
// when checked. Identical between the add and edit modals, so it lives here once.
export const RoleSection: React.FC<RoleSectionProps> = ({
  employeeChecked,
  onToggleEmployee,
  employeeDeptId,
  onEmployeeDept,
  showEmployeeToggle = true,
  showAdmin,
  adminChecked,
  onToggleAdmin,
  adminDeptId,
  onAdminDept,
  departments
}) => (
  <>
    {showEmployeeToggle && (
      <RoleCheckbox label="Employee" checked={employeeChecked} onToggle={onToggleEmployee} />
    )}
    {employeeChecked && (
      <DepartmentSelect
        label="Department"
        className="field-tight"
        value={employeeDeptId}
        onChange={onEmployeeDept}
        departments={departments}
      />
    )}

    {showAdmin && (
      <>
        <RoleCheckbox label="Administrator" checked={adminChecked} onToggle={onToggleAdmin} />
        {adminChecked && (
          <DepartmentSelect
            label="Administrator of"
            value={adminDeptId}
            onChange={onAdminDept}
            departments={departments}
          />
        )}
      </>
    )}
  </>
)
