import React, { useRef, useState } from 'react'
import { useOutsideClick } from '../hooks/useOutsideClick'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

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
  // Button caption. Defaults to "Filter by"; pages that show several dropdowns
  // side by side pass a specific label (e.g. "Filter by status") to tell them apart.
  label?: string
}

const FilterDropdown: React.FC<FilterDropdownProps> = ({
  options,
  selected,
  onChange,
  label
}) => {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  
  const { language } = useLanguage()
  const t = translations[language].dropdowns
  
  const displayLabel = label || t.filterBy

  useOutsideClick(ref, open, () => setOpen(false))

  const toggleOption = (value: string) =>
    onChange(selected.includes(value) ? selected.filter(v => v !== value) : [...selected, value])

  return (
    <div className="filter-dropdown" ref={ref}>
      <button type="button" className="filter-toggle" onClick={() => setOpen(o => !o)}>
        {displayLabel}
        {selected.length ? ` (${selected.length})` : ''}
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