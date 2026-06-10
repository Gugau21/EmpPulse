import React from 'react'
import blackTriangleIcon from '../assets/black_triangle.png'

// The requests pages (My requests / Requests) share the same outer shell: a
// page header with a "Filter by" dropdown, and a single collapsible accordion
// section. Kept in one place so the two pages don't drift (and don't trip the
// copy-paste linter). Each page supplies its own list as `children` plus an
// optional `footer` (e.g. a "+ create request" button) below the accordion.
interface AccordionScreenProps {
  pageTitle: string
  accordionTitle: string
  expanded: boolean
  onToggle: () => void
  children: React.ReactNode
  footer?: React.ReactNode
}

const AccordionScreen: React.FC<AccordionScreenProps> = ({
  pageTitle,
  accordionTitle,
  expanded,
  onToggle,
  children,
  footer
}) => (
  <div className="screen-container">
    <header className="page-header">
      <h2>{pageTitle}</h2>
      <div className="filter-dropdown">
        <span>Filter by</span>...
      </div>
    </header>

    <div className="accordion-section">
      <h3 className="department-title" onClick={onToggle}>
        {accordionTitle}{' '}
        <img
          src={blackTriangleIcon}
          alt={`Toggle ${accordionTitle.toLowerCase()}`}
          className={`chevron ${expanded ? 'expanded' : ''}`}
        />
      </h3>

      {expanded && children}
    </div>

    {footer}
  </div>
)

export default AccordionScreen
