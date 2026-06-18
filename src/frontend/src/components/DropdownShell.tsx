import React, { useRef, useState } from 'react'
import { useOutsideClick } from '../hooks/useOutsideClick'

interface Props {
  // Button caption, already resolved to the active language by the caller (it also
  // carries each dropdown's own suffix — a selected count or the active sort).
  caption: React.ReactNode
  // The menu options. Receives `close` so a single-select dropdown can dismiss the
  // menu after a pick; multi-select dropdowns simply ignore it to stay open.
  children: (close: () => void) => React.ReactNode
}

// The shared open/close/outside-click scaffold for the filter and sort dropdowns.
// Each dropdown only differs in how it renders and toggles its options, so just the
// toggle button and the menu container live here.
const DropdownShell: React.FC<Props> = ({ caption, children }) => {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useOutsideClick(ref, open, () => setOpen(false))

  return (
    <div className="filter-dropdown" ref={ref}>
      <button type="button" className="filter-toggle" onClick={() => setOpen(o => !o)}>
        {caption}
      </button>

      {open && <div className="filter-menu">{children(() => setOpen(false))}</div>}
    </div>
  )
}

export default DropdownShell
