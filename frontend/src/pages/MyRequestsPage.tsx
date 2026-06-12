import React, { useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import type { LeaveRequest } from '../types'
import type { OutletContext } from '../components/AppLayout'
import AccordionScreen from '../components/AccordionScreen'
import trashIcon from '../assets/bin_icon_dark.png'
import trashIconLight from '../assets/bin_icon_light.png'
import crossIcon from '../assets/cross_icon_dark.png'
import crossIconLight from '../assets/cross_icon_light.png'
import { useTheme } from '../hooks/useTheme'

// Placeholder leave records: there is no leave/requests API yet, so this page
// renders static mock data to exercise the layout. Replace with a real query
// (e.g. GET /api/me/leave-requests) once the leave feature is wired up.
const myRecordsData: LeaveRequest[] = [
  {
    id: '1',
    employeeName: 'Me',
    type: 'Vacation',
    dateRange: '20.06.2026 - 30.06.2026',
    status: 'PENDING'
  },
  {
    id: '2',
    employeeName: 'Me',
    type: 'Personal',
    dateRange: '28.05.2026 - 30.05.2026',
    status: 'REJECTED'
  },
  {
    id: '3',
    employeeName: 'Me',
    type: 'Sick',
    dateRange: '17.03.2026 - 21.03.2026',
    status: 'APPROVED'
  },
  {
    id: '4',
    employeeName: 'Me',
    type: 'Vacation',
    dateRange: '20.12.2025 - 26.12.2025',
    status: 'CANCELLED'
  }
]

const MyRequestsScreen: React.FC = () => {
  const { openModal } = useOutletContext<OutletContext>()
  const [expanded, setExpanded] = useState(true)
  const { theme } = useTheme()

  return (
    <AccordionScreen
      pageTitle="My requests"
      accordionTitle="Last requests"
      expanded={expanded}
      onToggle={() => setExpanded(!expanded)}
      footer={
        <div className="center-action">
          <button className="primary-btn" onClick={() => openModal('ADD_LEAVE')}>
            + add request
          </button>
        </div>
      }
    >
      <div className="card-box list-box">
        {myRecordsData.map(req => (
          <div
            key={req.id}
            className={`employee-row hover-slide-container clickable ${req.status === 'PENDING' ? 'dashed-active-row' : ''}`}
            onClick={() => openModal('EDIT_LEAVE_FORM', undefined, req)}
          >
            <span className={`badge badge-${req.type.toLowerCase()}`}>{req.type}</span>
            <span className="date-span">{req.dateRange}</span>
            <div className="emp-meta">
              <span className={`status-label status-${req.status.toLowerCase()}`}>
                {req.status}
              </span>
            </div>

            <button
              className="slide-bin-btn"
              onClick={e => {
                e.stopPropagation() // Don't also open the row's edit modal
                openModal(
                  req.status === 'APPROVED' ? 'CANCEL_LEAVE' : 'DELETE_LEAVE',
                  undefined,
                  req
                )
              }}
              title={req.status === 'APPROVED' ? 'Cancel Approved Leave' : 'Delete Record'}
            >
              {req.status === 'APPROVED' ? (
                <img src={theme === 'dark' ? crossIconLight : crossIcon} alt="Delete" width={30} height={30} />
              ) : (
                <img src={theme === 'dark' ? trashIconLight : trashIcon} alt="Delete" width={30} height={30} />
              )}
            </button>
          </div>
        ))}
      </div>
    </AccordionScreen>
  )
}

export default MyRequestsScreen
