import React, { useRef, useState } from 'react'
import { useOutsideClick } from '../hooks/useOutsideClick'
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

const SortDropdown: React.FC<SortDropdownProps> = ({
  options,
  selected,
  onChange,
  label
}) => {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  
  const { language } = useLanguage()
  const t = translations[language].dropdowns
  
  const displayLabel = label || t.sortBy

  useOutsideClick(ref, open, () => setOpen(false))

  const pick = (value: string) => {
    onChange(selected === value ? null : value)
    setOpen(false)
  }

  const activeLabel = options.find(opt => opt.value === selected)?.label

  return (
    <div className="filter-dropdown" ref={ref}>
      <button type="button" className="filter-toggle" onClick={() => setOpen(o => !o)}>
        {displayLabel}
        {activeLabel ? `: ${activeLabel}` : ''}
      </button>

      {open && (
        <div className="filter-menu">
          {options.map(opt => (
            <label key={opt.value} className="filter-option">
              <input
                type="radio"
                checked={selected === opt.value}
                onChange={() => pick(opt.value)}
              />
              <span>{opt.label}</span>
            </label>
          ))}
        </div>
      )}
    </div>
  )
}

export default SortDropdown