import React, { useEffect, useRef, useState } from 'react'

// A "Filter by" tag picker shared by the Employees, My requests and Requests
// pages. Clicking the button opens a dropdown of checkboxes; toggling a checkbox
// keeps the menu open so several tags can be picked. Clicking the button again
// (or clicking outside) closes the menu while preserving the chosen tags — the
// selection lives in the parent, so it survives open/close.
interface FilterOption {
  value: string
  label: string
}

interface FilterDropdownProps {
  options: readonly FilterOption[]
  selected: string[]
  onChange: (selected: string[]) => void
}

const FilterDropdown: React.FC<FilterDropdownProps> = ({ options, selected, onChange }) => {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  // Close on an outside click; the chosen tags are kept.
  useEffect(() => {
    if (!open) return
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [open])

  const toggleOption = (value: string) =>
    onChange(selected.includes(value) ? selected.filter(v => v !== value) : [...selected, value])

  return (
    <div className="filter-dropdown" ref={ref}>
      <button type="button" className="filter-toggle" onClick={() => setOpen(o => !o)}>
        Filter by{selected.length ? ` (${selected.length})` : ''}
      </button>

      {open && (
        <div className="filter-menu">
          {options.map(opt => (
            <label key={opt.value} className="filter-option">
              <input
                type="checkbox"
                checked={selected.includes(opt.value)}
                onChange={() => toggleOption(opt.value)}
              />
              <span>{opt.label}</span>
            </label>
          ))}
        </div>
      )}
    </div>
  )
}

export default FilterDropdown
