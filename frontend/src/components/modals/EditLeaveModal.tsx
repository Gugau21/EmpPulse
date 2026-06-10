import React, { useState, useEffect } from 'react'
import type { LeaveRequest } from '../../types' // Adjust path if needed

interface Props {
  closeModal: () => void
  selectedRequest: LeaveRequest | null
}

const EditLeaveModal: React.FC<Props> = ({ closeModal, selectedRequest }) => {
  const [paymentType, setPaymentType] = useState('Paid')
  const [leaveType, setLeaveType] = useState('Vacation')
  const [from, setFrom] = useState('')
  const [till, setTill] = useState('')
  const [reason, setReason] = useState('')

  useEffect(() => {
    if (selectedRequest) {
      setLeaveType(selectedRequest.type)
      if (selectedRequest.reason) setReason(selectedRequest.reason)

      if (selectedRequest.dateRange) {
        const [startStr, endStr] = selectedRequest.dateRange.split(' - ')
        
        const formatDate = (dateStr?: string) => {
          if (!dateStr) return ''
          const [dd, mm, yyyy] = dateStr.split('.')
          if (dd && mm && yyyy) return `${yyyy}-${mm}-${dd}`
          return ''
        }

        setFrom(formatDate(startStr))
        setTill(formatDate(endStr))
      }
    }
  }, [selectedRequest])

  return (
    <div className="modal-form">
      <h2>Edit leave</h2>
      
      <label>
        Type of leave (by payment)
        <select value={paymentType} onChange={e => setPaymentType(e.target.value)}>
          <option value="Paid">Paid</option>
          <option value="Unpaid">Unpaid</option>
        </select>
      </label>
      
      <label>
        Type of leave
        <select value={leaveType} onChange={e => setLeaveType(e.target.value)}>
          <option value="Vacation">Vacation</option>
          <option value="Sick">Sick</option>
          <option value="Personal">Personal</option>
        </select>
      </label>

      {leaveType === 'Vacation' && (
        <div className="balance-hint">If vacation: you have 15 days left</div>
      )}

      <label>
        From
        <input type="date" value={from} onChange={e => setFrom(e.target.value)} />
      </label>
      
      <label>
        Till
        <input type="date" value={till} onChange={e => setTill(e.target.value)} />
      </label>
      
      <label>
        Reason
        <textarea 
          rows={2} 
          value={reason} 
          onChange={e => setReason(e.target.value)}
        ></textarea>
      </label>
      
      <button className="primary-btn full-width" onClick={closeModal}>
        edit leave
      </button>
    </div>
  )
}

export default EditLeaveModal