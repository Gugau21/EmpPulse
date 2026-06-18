import React from 'react'

// The requests pages (My requests / Requests) share the same outer shell: a page
// header with a "Filter by" dropdown, the list itself, and an optional `footer`
// (e.g. a "+ create request" button) below it. Kept in one place so the two pages
// don't drift (and don't trip the copy-paste linter).
interface AccordionScreenProps {
  pageTitle: string
  children: React.ReactNode
  footer?: React.ReactNode
  // The page's "Filter by" control, rendered in the header. Each page owns its
  // own filter state, so the dropdown is passed in rather than built here.
  filter?: React.ReactNode
}

const AccordionScreen: React.FC<AccordionScreenProps> = ({
  pageTitle,
  children,
  footer,
  filter
}) => (
  <div className="screen-container">
    <header className="page-header">
      <h2>{pageTitle}</h2>
      {filter}
    </header>

    {children}

    {footer}
  </div>
)

export default AccordionScreen
