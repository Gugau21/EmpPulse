import React, { useState } from 'react'
import type { Department, Employee } from '../../types'
import { useAuth } from '../../context/useAuth'
import { IdentityFields, RoleSection } from '../helpers/employeeFormParts'

interface Props {
  closeModal: () => void
  departments: Department[]
  selectedEmployee: Employee | null
}

const EditEmployeeModal: React.FC<Props> = ({ closeModal, departments, selectedEmployee }) => {
  const { userRole } = useAuth()
  const isOwner = userRole === 'OWNER'

  const [newName, setNewName] = useState(selectedEmployee?.name || '')
  const [newSurname, setNewSurname] = useState(selectedEmployee?.surname || '')
  const [newEmail, setNewEmail] = useState(selectedEmployee?.email || '')
  // Blank means "keep current password"; only a non-empty value triggers a change.
  const [newPassword, setNewPassword] = useState('')

  const [isEmployeeChecked, setIsEmployeeChecked] = useState(true)
  const [isAdminChecked, setIsAdminChecked] = useState(false)

  const [employeeDeptId, setEmployeeDeptId] = useState<number | null>(null)
  const [adminDeptId, setAdminDeptId] = useState<number | null>(null)

  const [editError, setEditError] = useState<string | null>(null)

  const handleEditUser = () => {
    setEditError(null)
    if (!newName.trim() || !newSurname.trim()) {
      setEditError('First name and surname are required.')
      return
    }

    // TODO: Connect to your update mutation hook here
    // e.g., updateUser.mutate({ id: selectedEmployee.id, ...data })

    closeModal()
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
        adminDeptId={adminDeptId}
        onAdminDept={setAdminDeptId}
        departments={departments}
      />

      {editError && <p className="form-error">{editError}</p>}

      <div className="modal-actions single">
        <button className="btn-modal-action fixed-width" onClick={handleEditUser}>
          edit employee
        </button>
      </div>
    </div>
  )
}

export default EditEmployeeModal
