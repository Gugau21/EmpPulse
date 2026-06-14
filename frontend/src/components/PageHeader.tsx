import React from 'react'

interface Props {
  title: string
  searchValue: string
  onSearchChange: (value: string) => void
  searchPlaceholder: string
  searchMaxLength?: number
  // Width override; the employees search box is narrower than the departments one.
  searchStyle?: React.CSSProperties
  // Extra controls rendered next to the search box (e.g. a filter dropdown).
  children?: React.ReactNode
}

// The shared page chrome for the Departments and Employees screens: the title plus
// a search box and any extra header controls passed as children. The surrounding
// `screen-container` stays in each page so its body content follows the header.
const PageHeader: React.FC<Props> = ({
  title,
  searchValue,
  onSearchChange,
  searchPlaceholder,
  searchMaxLength,
  searchStyle,
  children
}) => (
  <header className="page-header">
    <h2>{title}</h2>
    <div className="header-actions">
      <div className="search-bar">
        <input
          type="text"
          placeholder={searchPlaceholder}
          value={searchValue}
          onChange={e => onSearchChange(e.target.value)}
          maxLength={searchMaxLength}
          style={searchStyle}
        />
      </div>
      {children}
    </div>
  </header>
)

export default PageHeader
