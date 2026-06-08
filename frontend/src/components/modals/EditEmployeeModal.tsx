import React, { useState } from 'react'
import type { Department, Employee, MeUser } from '../../types'
import { useAuth } from '../../context/useAuth'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useUpdateUser } from '../../hooks/useEmployeeMutations'
import type { UserUpdatePayload } from '../../services/api'
import { IdentityFields, RoleSection } from '../helpers/employeeFormParts'

interface Props {
  closeModal: () => void
  departments: Department[]
  selectedEmployee: Employee | null
}

// The employee list row carries only name/surname, so fetch the full profile
// (GET /api/users/{id}) and render the form once it's available. The form is
// keyed on the user id so its state initialises from the loaded values without
// an effect syncing props into state.
const EditEmployeeModal: React.FC<Props> = ({ closeModal, departments, selectedEmployee }) => {
  const userId = selectedEmployee?.id ? Number(selectedEmployee.id) : null
  const { data: userDetail } = useUserDetail(userId)

  if (userId === null || !userDetail) {
    return (
      <div className="modal-form">
        <h2 style={{ marginBottom: '24px' }}>Edit employee’s profile</h2>
        <p>Loading…</p>
      </div>
    )
  }

  return (
    <EditEmployeeForm
      key={userDetail.id}
      userId={userId}
      user={userDetail}
      departments={departments}
      closeModal={closeModal}
    />
  )
}

interface FormProps {
  userId: number
  user: MeUser
  departments: Department[]
  closeModal: () => void
}

const EditEmployeeForm: React.FC<FormProps> = ({ userId, user, departments, closeModal }) => {
  const { userRole } = useAuth()
  const isOwner = userRole === 'OWNER'

  const [newName, setNewName] = useState(user.name)
  const [newSurname, setNewSurname] = useState(user.surname)
  const [newEmail, setNewEmail] = useState(user.email)
  // Blank means "keep current password"; only a non-empty value triggers a change.
  const [newPassword, setNewPassword] = useState('')

  const [isEmployeeChecked, setIsEmployeeChecked] = useState(user.employeeProfile !== null)
  const [isAdminChecked, setIsAdminChecked] = useState(user.adminProfile !== null)

  const [employeeDeptId, setEmployeeDeptId] = useState<number | null>(
    user.employeeProfile?.departmentId ?? null
  )
  const [adminDeptIds, setAdminDeptIds] = useState<number[]>(user.adminProfile?.departmentIds ?? [])

  const [editError, setEditError] = useState<string | null>(null)
  const updateUser = useUpdateUser()

  const handleEditUser = () => {
    setEditError(null)

    if (isEmployeeChecked && employeeDeptId === null) {
      setEditError('Please select a department for the employee role.')
      return
    }
    if (isOwner && isAdminChecked && adminDeptIds.length === 0) {
      setEditError('Please select at least one department for the administrator role.')
      return
    }

    // Owner-only fields (name/surname/email/password/admin) are rejected for admin
    // callers by the server, so only owners include them in the payload.
    const payload: UserUpdatePayload = {}
    if (isOwner) {
      if (!newName.trim() || !newSurname.trim()) {
        setEditError('First name and surname are required.')
        return
      }
      payload.name = newName.trim()
      payload.surname = newSurname.trim()
      if (newEmail.trim()) payload.email = newEmail.trim()
      if (newPassword) payload.password = newPassword
      if (isAdminChecked) payload.adminDepartmentIds = adminDeptIds
    }
    if (isEmployeeChecked && employeeDeptId !== null) {
      payload.employeeDepartmentId = employeeDeptId
    }

    updateUser.mutate({ userId, payload }, { onSuccess: () => closeModal() })
  }

  return (
    <div className="modal-form">
      <h2 style={{ marginBottom: '24px' }}>Edit employee’s profile</h2>

      {/* Email & password are editable by owners only. */}
      <IdentityFields
        name={newName}
        onName={setNewName}
        surname={newSurname}
        onSurname={setNewSurname}
        {...(isOwner
          ? {
              email: newEmail,
              onEmail: setNewEmail,
              password: newPassword,
              onPassword: setNewPassword,
              passwordPlaceholder: 'Leave blank to keep current password'
            }
          : {})}
      />

      {/* Administrator role is assignable by owners only. */}
      <RoleSection
        employeeChecked={isEmployeeChecked}
        onToggleEmployee={() => setIsEmployeeChecked(!isEmployeeChecked)}
        employeeDeptId={employeeDeptId}
        onEmployeeDept={setEmployeeDeptId}
        showAdmin={isOwner}
        adminChecked={isAdminChecked}
        onToggleAdmin={() => setIsAdminChecked(!isAdminChecked)}
        adminDeptIds={adminDeptIds}
        onAdminDepts={setAdminDeptIds}
        departments={departments}
      />

      {(editError || updateUser.error) && (
        <p className="form-error">{editError ?? updateUser.error?.message}</p>
      )}

      <div className="modal-actions single">
        <button
          className="btn-modal-action fixed-width"
          onClick={handleEditUser}
          disabled={updateUser.isPending}
        >
          edit employee
        </button>
      </div>
    </div>
  )
}

export default EditEmployeeModal
