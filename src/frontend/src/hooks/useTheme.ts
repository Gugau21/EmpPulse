import { useState, useEffect } from 'react'
import { preferencesService } from '../services/api'
import {
  DEFAULT_THEME,
  THEME_STORAGE_KEY,
  THEME_CHANGED_EVENT,
  applyTheme,
  themeToApi,
  type Theme
} from '../utils/preferences'

export const useTheme = () => {
  const [theme, setTheme] = useState<Theme>(() => {
    return (localStorage.getItem(THEME_STORAGE_KEY) as Theme) || DEFAULT_THEME
  })

  // 1. Listen for the custom event so ALL components (and the on-login hydration
  //    in AuthContext) update instantly.
  useEffect(() => {
    const handleSync = () => {
      setTheme((localStorage.getItem(THEME_STORAGE_KEY) as Theme) || DEFAULT_THEME)
    }

    window.addEventListener(THEME_CHANGED_EVENT, handleSync)
    return () => window.removeEventListener(THEME_CHANGED_EVENT, handleSync)
  }, [])

  // 2. Ensure the HTML tag gets set properly on the very first page load
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
  }, [theme])

  // 3. Update the state, save it locally + broadcast (applyTheme), then persist
  //    to the signed-in user's profile. The save is best-effort: the toggle has
  //    already taken effect locally, so a failed request just isn't remembered
  //    server-side rather than blocking or reverting the UI.
  const toggleTheme = () => {
    const newTheme: Theme = theme === 'light' ? 'dark' : 'light'

    setTheme(newTheme)
    applyTheme(newTheme)
    preferencesService.update({ theme: themeToApi(newTheme) }).catch(() => {})
  }

  return { theme, toggleTheme }
}
