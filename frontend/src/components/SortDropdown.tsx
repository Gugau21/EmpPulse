import React from 'react'
import DropdownShell from './DropdownShell'
import { useLanguage } from '../hooks/useLanguage'
import { translations } from '../utils/translations'

// A "Sort by" picker sitting next to the "Filter by" dropdowns on the request
// lists. Unlike FilterDropdown it is single-select: choosing an option applies
// that ordering and closes the menu. Re-picking the active option clears the
// sort (back to the list's natural order). The selection lives in the parent.
interface SortOption {
  value: string
  label: string
}

interface SortDropdownProps {
  options: readonly SortOption[]
  selected: string | null
  onChange: (selected: string | null) => void
  label?: string
}

const SortDropdown: React.FC<SortDropdownProps> = ({ options, selected, onChange, label }) => {
  const { language } = useLanguage()
  const t = translations[language].dropdowns

  const activeLabel = options.find(opt => opt.value === selected)?.label

  return (
    <DropdownShell
      caption={
        <>
          {label || t.sortBy}
          {activeLabel ? `: ${activeLabel}` : ''}
        </>
      }
    >
      {close => {
        const pick = (value: string) => {
          onChange(selected === value ? null : value)
          close()
        }
        return options.map(opt => (
          <label key={opt.value} className="filter-option">
            <input type="radio" checked={selected === opt.value} onChange={() => pick(opt.value)} />
            <span>{opt.label}</span>
          </label>
        ))
      }}
    </DropdownShell>
  )
}

export default SortDropdown
