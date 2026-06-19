import React, { useState } from 'react'
import type { UseQueryResult } from '@tanstack/react-query'
import type { LeaveRequest } from '../types'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'
import { partitionByTimeframe, type Timeframe } from '../utils/requestTimeframe'
import blackTriangleIcon from '../assets/black_triangle.png'

interface Props {
  // The list-loading query straight from useLeaveRequests; this reads its
  // loading/error flags so both request pages render those states identically.
  state: Pick<UseQueryResult<LeaveRequest[]>, 'isLoading' | 'isError' | 'error'>
  requests: LeaveRequest[]
  // Each page renders its own row markup (the columns and click behaviour differ
  // between "My requests" and the manager view); this owns the surrounding
  // loading/error/empty states and the timeframe accordions they share.
  renderRow: (request: LeaveRequest) => React.ReactNode
}

// The timeframe boxes in display order. The incoming requests are already filtered
// and sorted, so each box just shows its slice in that same order. `titleKey` points
// at the requestList translation entry so the heading follows the active language.
const SECTIONS: { key: Timeframe; titleKey: 'currentLeaves' | 'future' | 'past' }[] = [
  { key: 'current', titleKey: 'currentLeaves' },
  { key: 'future', titleKey: 'future' },
  { key: 'past', titleKey: 'past' }
]

const RequestList: React.FC<Props> = ({ state, requests, renderRow }) => {
  const { language } = useLanguage()
  const t = translations[language].requestList

  // Past leaves are historical, so that box opens collapsed; the live ones expand.
  const [collapsed, setCollapsed] = useState<Partial<Record<Timeframe, boolean>>>({ past: true })
  const toggle = (key: Timeframe) => setCollapsed(prev => ({ ...prev, [key]: !prev[key] }))

  const buckets = partitionByTimeframe(requests)

  return (
    <>
      {state.isLoading && <p className="muted">{t.loading}</p>}
      {state.isError && <p className="form-error">{state.error?.message ?? t.failedLoad}</p>}

      {requests.length > 0 &&
        SECTIONS.map(({ key, titleKey }) => {
          const rows = buckets[key]
          const expanded = !collapsed[key]
          return (
            <div className="accordion-section" key={key}>
              {/* department-title is the shared accordion-title style (chevron + click) */}
              <h3 className="department-title" onClick={() => toggle(key)}>
                {t[titleKey]} ({rows.length})
                <img
                  src={blackTriangleIcon}
                  alt=""
                  className={`chevron ${expanded ? 'expanded' : ''}`}
                />
              </h3>

              {expanded && (
                <div className="card-box list-box">
                  {rows.length ? (
                    rows.map(renderRow)
                  ) : (
                    <div className="muted role-empty">{t.noLeavesHere}</div>
                  )}
                </div>
              )}
            </div>
          )
        })}
    </>
  )
}

export default RequestList
