import React from 'react'
import DropdownShell from './DropdownShell'
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

const FilterDropdown: React.FC<FilterDropdownProps> = ({ options, selected, onChange, label }) => {
  const { language } = useLanguage()
  const t = translations[language].dropdowns

  const toggleOption = (value: string) =>
    onChange(selected.includes(value) ? selected.filter(v => v !== value) : [...selected, value])

  return (
    <DropdownShell
      caption={
        <>
          {label || t.filterBy}
          {selected.length ? ` (${selected.length})` : ''}
        </>
      }
    >
      {() =>
        options.map(opt => (
          <label key={opt.value} className="filter-option">
            <input
              type="checkbox"
              checked={selected.includes(opt.value)}
              onChange={() => toggleOption(opt.value)}
            />
            <span>{opt.label}</span>
          </label>
        ))
      }
    </DropdownShell>
  )
}

export default FilterDropdown
