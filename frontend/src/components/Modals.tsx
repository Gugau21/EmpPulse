import React from 'react'
import type {
  ModalType,
  Employee,
  LeaveRequest,
  Department,
  LoggedHours,
  OpenModal
} from '../types'
import ConfirmModal from './modals/ConfirmModal'
import AddEmployeeModal from './modals/AddEmployeeModal'
import EditAdminsModal from './modals/EditAdminsModal'
import LogHoursModal from './modals/LogHoursModal'
import AddLeaveModal from './modals/AddLeaveModal'
import AcceptRequestModal from './modals/AcceptRequestModal'
import AddDepartmentModal from './modals/AddDepartmentModal'
import EditDepartmentModal from './modals/EditDepartmentModal'
import AddDefaultWorkingHoursModal from './modals/AddDefaultWorkingHoursModal'
import EditEmployeeModal from './modals/EditEmployeeModal'
import EditHoursModal from './modals/EditHoursModal'
import AddBonusVacationDaysModal from './modals/AddBonusVacationDaysModal'
import EditLeaveModal from './modals/EditLeaveModal'
import ViewLeaveModal from './modals/ViewLeaveModal'
import ChangePasswordFormModal from './modals/ChangePasswordModal'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

interface Props {
  activeModal: ModalType
  previousModal: ModalType
  openModal: OpenModal
  closeModal: () => void
  confirmModal: () => void
  selectedEmployee: Employee | null
  selectedRequest: LeaveRequest | null
  selectedLoggedHours: LoggedHours | null
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
  'LOGOUT'
]

const Modals: React.FC<Props> = ({
  activeModal,
  previousModal,
  closeModal,
  confirmModal,
  selectedEmployee,
  selectedRequest,
  selectedLoggedHours,
  selectedDepartment,
  departments,
  confirmError,
  onConfirmErrorClear,
  openModal
}) => {
  const { language } = useLanguage()
  const t = translations[language].modals

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

        {activeModal === 'ADD_EMPLOYEE' &&
          (departments.length === 0 ? (
            <div className="modal-form">
              <h2>{t.cantAddEmployee}</h2>
              <p>{t.noDeptsCreated}</p>
              <div className="modal-actions">
                <button className="btn-modal-action" onClick={closeModal}>
                  {t.ok}
                </button>
              </div>
            </div>
          ) : (
            <AddEmployeeModal
              closeModal={closeModal}
              departments={departments}
              openModal={openModal}
            />
          ))}

        {activeModal === 'EDIT_ADMINS' && (
          <EditAdminsModal closeModal={closeModal} selectedDepartment={selectedDepartment} />
        )}

        {activeModal === 'LOG_HOURS' && (
          <LogHoursModal closeModal={closeModal} selectedEmployee={selectedEmployee} />
        )}

        {(activeModal === 'ADD_LEAVE' || activeModal === 'CREATE_REQUEST') && (
          <AddLeaveModal activeModal={activeModal} closeModal={closeModal} />
        )}

        {activeModal === 'EDIT_LEAVE_FORM' && (
          <EditLeaveModal
            closeModal={closeModal}
            selectedRequest={selectedRequest}
            // Only show a back arrow when this form was reached from the
            // accept/reject modal, so it can return there.
            onBack={
              previousModal === 'ACCEPT_REQUEST' ? () => openModal('ACCEPT_REQUEST') : undefined
            }
          />
        )}

        {activeModal === 'VIEW_LEAVE_FORM' && (
          <ViewLeaveModal closeModal={closeModal} selectedRequest={selectedRequest} />
        )}

        {activeModal === 'ACCEPT_REQUEST' && (
          <AcceptRequestModal
            closeModal={closeModal}
            selectedRequest={selectedRequest}
            openModal={openModal}
          />
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
          <EditHoursModal
            closeModal={closeModal}
            selectedEmployee={selectedEmployee}
            selectedLoggedHours={selectedLoggedHours}
          />
        )}

        {activeModal === 'ADD_BONUS_DAYS' && (
          <AddBonusVacationDaysModal closeModal={closeModal} selectedEmployee={selectedEmployee} />
        )}

        {activeModal === 'CHANGE_PASSWORD_FORM' && (
          <ChangePasswordFormModal openModal={openModal} />
        )}

        {activeModal === 'CHANGE_PASSWORD_SUCCESS' && (
          <div className="modal-form">
            <h2>{t.passwordChanged}</h2>
            <p>{t.passwordChangedMsg}</p>
            <div className="modal-actions">
              <button className="btn-modal-action" onClick={closeModal}>
                {t.ok}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default Modals
