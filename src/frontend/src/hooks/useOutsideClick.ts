import { useEffect, type RefObject } from 'react'

// Dismiss-on-click-away for pop-over menus. While `active` (wired to the menu's
// open state), a mousedown landing outside the referenced element runs
// `onOutside`; the listener is torn down once the menu closes. Shared by the
// filter/sort dropdowns and the employee type-ahead so each doesn't re-implement
// the same effect.
export function useOutsideClick<T extends HTMLElement>(
  ref: RefObject<T | null>,
  active: boolean,
  onOutside: () => void
) {
  useEffect(() => {
    if (!active) return
    const handleClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onOutside()
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [ref, active, onOutside])
}
