import React, { useState } from 'react'
import type { Department, OpenModal } from '../../types'
import { useAuth } from '../../context/useAuth'
import { useCreateUser } from '../../hooks/useCreateUser'
import { IdentityFields, RoleSection } from '../helpers/employeeFormParts'
import { isValidEmail } from '../../utils/validation'

interface Props {
  closeModal: () => void
  departments: Department[]
  openModal: OpenModal
}

const AddEmployeeModal: React.FC<Props> = ({ closeModal, departments, openModal }) => {
  const { isOwner, isAdmin } = useAuth()
  // An admin can only ever create plain employees (no admin accounts, no role choice).
  const isAdminCreator = isAdmin && !isOwner

  // Both roles start unchecked so nothing is pre-selected on a fresh form. The one
  // exception: an admin creator can only ever make plain employees and their toggle
  // is hidden, so the employee role is forced on for them.
  const [isEmployeeChecked, setIsEmployeeChecked] = useState(isAdminCreator)
  // An admin creator can't assign the admin role, so it stays off either way.
  const [isAdminChecked, setIsAdminChecked] = useState(false)

  const [newName, setNewName] = useState('')
  const [newSurname, setNewSurname] = useState('')
  const [newEmail, setNewEmail] = useState('')
  const [newPassword, setNewPassword] = useState('')
  // Department selections (employee: null = none chosen yet; admin: empty = none)
  const [employeeDeptId, setEmployeeDeptId] = useState<number | null>(null)
  const [vacationDays, setVacationDays] = useState('0')
  const [adminDeptIds, setAdminDeptIds] = useState<number[]>([])
  const [createError, setCreateError] = useState<string | null>(null)
  const createUser = useCreateUser()

  const resetCreateUserForm = () => {
    setNewName('')
    setNewSurname('')
    setNewEmail('')
    setNewPassword('')
    setEmployeeDeptId(null)
    setVacationDays('0')
    setAdminDeptIds([])
    setCreateError(null)
  }

  const handleCreateUser = () => {
    setCreateError(null)
    if (!newName.trim() || !newSurname.trim() || !newEmail.trim() || !newPassword) {
      setCreateError('Name, surname, email and password are required.')
      return
    }
    if (!isValidEmail(newEmail)) {
      setCreateError('Please enter a valid email address.')
      return
    }
    // A user with no role is meaningless — must be employee and/or admin.
    if (!isEmployeeChecked && !isAdminChecked) {
      setCreateError('User must be assigned at least one role: Employee or Administrator.')
      return
    }
    // A department must be chosen for every selected role — creating without one is not allowed.
    if (isEmployeeChecked && employeeDeptId === null) {
      setCreateError('Please select a department for the employee role before creating the user.')
      return
    }
    if (isAdminChecked && adminDeptIds.length === 0) {
      setCreateError(
        'Please select at least one department for the administrator role before creating the user.'
      )
      return
    }

    createUser.mutate(
      {
        name: newName.trim(),
        surname: newSurname.trim(),
        email: newEmail.trim(),
        password: newPassword,
        ...(isEmployeeChecked
          ? {
              employeeDepartmentId: employeeDeptId,
              yearlyVacationBalance:
                vacationDays.trim() === '' || Number.isNaN(Number(vacationDays))
                  ? 0
                  : Number(vacationDays)
            }
          : {}),
        // undefined = no admin role; array = admin assigned to the chosen departments
        adminDepartmentIds: isAdminChecked && adminDeptIds.length > 0 ? adminDeptIds : undefined
      },
      {
        onSuccess: () => {
          resetCreateUserForm()
          closeModal()
        }
      }
    )
  }

  return (
    <div className="modal-form">
      <h2>Add employee</h2>

      <IdentityFields
        name={newName}
        onName={setNewName}
        surname={newSurname}
        onSurname={setNewSurname}
        email={newEmail}
        onEmail={setNewEmail}
        password={newPassword}
        onPassword={setNewPassword}
      />

      {/* Admin creators may only create plain employees: hide the role toggles. */}
      <RoleSection
        employeeChecked={isEmployeeChecked}
        onToggleEmployee={() => setIsEmployeeChecked(!isEmployeeChecked)}
        employeeDeptId={employeeDeptId}
        onEmployeeDept={setEmployeeDeptId}
        vacationDays={vacationDays}
        onVacationDays={setVacationDays}
        showEmployeeToggle={!isAdminCreator}
        showAdmin={!isAdminCreator}
        adminChecked={isAdminChecked}
        onToggleAdmin={() => setIsAdminChecked(!isAdminChecked)}
        adminDeptIds={adminDeptIds}
        onAdminDepts={setAdminDeptIds}
        departments={departments}
      />

      {(createError || createUser.error) && (
        <p className="form-error">{createError ?? createUser.error?.message}</p>
      )}

      <div className="modal-actions">
        <button
          className="btn-modal-action"
          onClick={handleCreateUser}
          disabled={createUser.isPending}
        >
          + add without default working hours
        </button>
        <button
          className="btn-modal-action"
          onClick={() => {
            openModal('ADD_WORKING_HOURS')
          }}
        >
          + add default working hours
        </button>
      </div>
    </div>
  )
}

export default AddEmployeeModal
