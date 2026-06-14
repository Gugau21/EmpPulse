import React, { useState } from 'react'
import type { Department, Employee, MeUser } from '../../types'
import { useAuth } from '../../context/useAuth'
import { useUserDetail } from '../../hooks/useUserDetail'
import { useUpdateUser, useDeleteEmployee } from '../../hooks/useEmployeeMutations'
import type { UserUpdatePayload } from '../../services/api'
import { IdentityFields, RoleSection } from '../helpers/employeeFormParts'
import { isValidEmail } from '../../utils/validation'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

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
  const { language } = useLanguage()
  const t = translations[language].modals

  const userId = selectedEmployee?.id ? Number(selectedEmployee.id) : null
  const { data: userDetail } = useUserDetail(userId)

  if (userId === null || !userDetail) {
    return (
      <div className="modal-form">
        <h2 style={{ marginBottom: '24px' }}>{t.editEmpProfile}</h2>
        <p>{t.loading}</p>
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
  const { isOwner } = useAuth()
  const { language } = useLanguage()
  const t = translations[language].modals

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
  // Pre-filled with the existing balance for current employees, 0 otherwise.
  const [vacationDays, setVacationDays] = useState(
    String(user.employeeProfile?.yearlyVacationBalance ?? 0)
  )
  const [adminDeptIds, setAdminDeptIds] = useState<number[]>(user.adminProfile?.departmentIds ?? [])

  const [editError, setEditError] = useState<string | null>(null)
  // When set, the edit would leave the user with no roles: we confirm, then
  // delete rather than persist an unusable account.
  const [confirmDeleteUser, setConfirmDeleteUser] = useState(false)
  const updateUser = useUpdateUser()
  const deleteUser = useDeleteEmployee()

  const submit = (payload: UserUpdatePayload) =>
    updateUser.mutate({ userId, payload }, { onSuccess: () => closeModal() })

  const handleEditUser = () => {
    setEditError(null)

    if (isEmployeeChecked && employeeDeptId === null) {
      setEditError(t.errSelectEmpDeptEdit)
      return
    }
    if (isOwner && isAdminChecked && adminDeptIds.length === 0) {
      setEditError(t.errSelectAdminDeptEdit)
      return
    }

    // Both roles off leaves the account unusable — delete it instead of saving.
    if (!isEmployeeChecked && !isAdminChecked) {
      setConfirmDeleteUser(true)
      return
    }

    // Owner-only fields (name/surname/email/password/admin) are rejected for admin
    // callers by the server, so only owners include them in the payload.
    const payload: UserUpdatePayload = {}
    if (isOwner) {
      if (!newName.trim() || !newSurname.trim()) {
        setEditError(t.errFirstSurnameRequired)
        return
      }
      if (newEmail.trim() && !isValidEmail(newEmail)) {
        setEditError(t.errInvalidEmail)
        return
      }
      payload.name = newName.trim()
      payload.surname = newSurname.trim()
      if (newEmail.trim()) payload.email = newEmail.trim()
      if (newPassword) payload.password = newPassword

      // [] detaches the admin from every department; the backend treats an
      // empty array for a never-admin user as a no-op.
      payload.adminDepartmentIds = isAdminChecked ? adminDeptIds : []
    }

    if (isEmployeeChecked) {
      payload.changeEmployeeDepartment = true
      payload.employeeDepartmentId = employeeDeptId
      const parsed = Number(vacationDays)
      payload.yearlyVacationBalance =
        vacationDays.trim() === '' || Number.isNaN(parsed) ? 0 : parsed
    } else if (user.employeeProfile !== null) {
      // Employee role unchecked for someone who currently has it: detach the
      // profile (null department) so the user actually loses the identity,
      // mirroring how an empty adminDepartmentIds removes the admin role above.
      payload.changeEmployeeDepartment = true
      payload.employeeDepartmentId = null
    }

    submit(payload)
  }

  if (confirmDeleteUser) {
    const err = deleteUser.error ?? updateUser.error
    return (
      <div className="modal-confirm">
        <h3>{t.confirmDetachAll}</h3>
        {err && <p className="form-error">{err.message}</p>}
        <div className="modal-buttons">
          <button
            className="btn-secondary"
            onClick={() => deleteUser.mutate(userId, { onSuccess: () => closeModal() })}
            disabled={deleteUser.isPending}
          >
            {t.yes}
          </button>
          <button className="btn-outline-primary" onClick={() => setConfirmDeleteUser(false)}>
            {t.no}
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="modal-form">
      <h2 style={{ marginBottom: '24px' }}>{t.editEmpProfile}</h2>

      {/* Identity fields (name/surname/email/password) are editable by owners
          only; admins can change just the department and vacation balance. */}
      {isOwner && (
        <IdentityFields
          name={newName}
          onName={setNewName}
          surname={newSurname}
          onSurname={setNewSurname}
          email={newEmail}
          onEmail={setNewEmail}
          password={newPassword}
          onPassword={setNewPassword}
          passwordPlaceholder={t.leaveBlankKeepPwd}
        />
      )}

      {/* Administrator role is assignable by owners only. */}
      <RoleSection
        employeeChecked={isEmployeeChecked}
        onToggleEmployee={() => setIsEmployeeChecked(!isEmployeeChecked)}
        employeeDeptId={employeeDeptId}
        onEmployeeDept={setEmployeeDeptId}
        vacationDays={vacationDays}
        onVacationDays={setVacationDays}
        showEmployeeToggle={isOwner}
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
          {t.editEmployeeBtn}
        </button>
      </div>
    </div>
  )
}

export default EditEmployeeModal