import React from 'react'
import type { ModalType, Employee, LeaveRequest, Department, OpenModal } from '../types'
import ConfirmModal from './modals/ConfirmModal'
import AddEmployeeModal from './modals/AddEmployeeModal'
import EditAdminsModal from './modals/EditAdminsModal'
import LogHoursModal from './modals/LogHoursModal'
import LeaveModal from './modals/LeaveModal'
import AcceptRequestModal from './modals/AcceptRequestModal'
import AddDepartmentModal from './modals/AddDepartmentModal'
import EditDepartmentModal from './modals/EditDepartmentModal'
import AddDefaultWorkingHoursModal from './modals/AddDefaultWorkingHoursModal'
import EditEmployeeModal from './modals/EditEmployeeModal'
import EditHoursModal from './modals/EditHoursModal'
import EditLeaveModal from './modals/EditLeaveModal'

interface Props {
  activeModal: ModalType
  openModal: OpenModal
  closeModal: () => void
  confirmModal: () => void
  selectedEmployee: Employee | null
  selectedRequest: LeaveRequest | null
  selectedDepartment: Department | null
  departments: Department[]
  confirmError?: string | null
  onConfirmErrorClear?: () => void
}

const CONFIRM_MODALS: ModalType[] = [
  'DELETE_EMPLOYEE',
  'DELETE_LEAVE',
  'CANCEL_LEAVE',
  'DELETE_DEPARTMENT',
  'LOGOUT',
  'CHANGE_PASSWORD'
]

const Modals: React.FC<Props> = ({
  activeModal,
  closeModal,
  confirmModal,
  selectedEmployee,
  selectedRequest,
  selectedDepartment,
  departments,
  confirmError,
  onConfirmErrorClear,
  openModal
}) => {
  if (!activeModal) return null

  const closeOnOverlayClick = CONFIRM_MODALS.includes(activeModal)

  return (
    <div className="modal-overlay" onClick={closeOnOverlayClick ? closeModal : undefined}>
      <div className="modal-container" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={closeModal}>
          ✕
        </button>

        {CONFIRM_MODALS.includes(activeModal) && (
          <ConfirmModal
            activeModal={activeModal}
            closeModal={closeModal}
            confirmModal={confirmModal}
            confirmError={confirmError}
            onConfirmErrorClear={onConfirmErrorClear}
          />
        )}

        {activeModal === 'ADD_EMPLOYEE' && (
          <AddEmployeeModal
            closeModal={closeModal}
            departments={departments}
            openModal={openModal}
          />
        )}

        {activeModal === 'EDIT_ADMINS' && (
          <EditAdminsModal closeModal={closeModal} selectedDepartment={selectedDepartment} />
        )}

        {activeModal === 'LOG_HOURS' && (
          <LogHoursModal closeModal={closeModal} selectedEmployee={selectedEmployee} />
        )}

        {(activeModal === 'ADD_LEAVE' ||
          activeModal === 'EDIT_LEAVE' ||
          activeModal === 'CREATE_REQUEST') && (
          <LeaveModal activeModal={activeModal} closeModal={closeModal} />
        )}

        {activeModal === 'EDIT_LEAVE_FORM' && (
          <EditLeaveModal 
            closeModal={closeModal} 
            selectedRequest={selectedRequest} 
          />
        )}

        {activeModal === 'ACCEPT_REQUEST' && (
          <AcceptRequestModal closeModal={closeModal} selectedRequest={selectedRequest} />
        )}

        {activeModal === 'ADD_DEPARTMENT' && <AddDepartmentModal closeModal={closeModal} />}

        {activeModal === 'EDIT_DEPARTMENT' && (
          <EditDepartmentModal closeModal={closeModal} selectedDepartment={selectedDepartment} />
        )}

        {(activeModal === 'ADD_WORKING_HOURS' || activeModal === 'EDIT_WORKING_HOURS') && (
          <AddDefaultWorkingHoursModal
            closeModal={closeModal}
            isEditMode={activeModal === 'EDIT_WORKING_HOURS'}
          />
        )}

        {activeModal === 'EDIT_EMPLOYEE' && (
          <EditEmployeeModal
            closeModal={closeModal}
            departments={departments}
            selectedEmployee={selectedEmployee}
          />
        )}

        {activeModal === 'EDIT_LOGGED_HOURS' && (
          <EditHoursModal closeModal={closeModal} selectedEmployee={selectedEmployee} />
        )}
      </div>
    </div>
  )
}

export default Modals
