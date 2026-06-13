import { useState, useEffect } from 'react'

export const useTheme = () => {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('app-theme') || 'light'
  })

  // 1. Listen for the custom event so ALL components update instantly
  useEffect(() => {
    const handleSync = () => {
      setTheme(localStorage.getItem('app-theme') || 'light')
    }

    window.addEventListener('theme-changed', handleSync)
    return () => window.removeEventListener('theme-changed', handleSync)
  }, [])

  // 2. Ensure the HTML tag gets set properly on the very first page load
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
  }, [theme])

  // 3. Update the state, save it, and broadcast the change to the app
  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light'

    setTheme(newTheme)
    localStorage.setItem('app-theme', newTheme)
    document.documentElement.setAttribute('data-theme', newTheme)

    // This is the magic line that tells the Department Page to swap the icon!
    window.dispatchEvent(new Event('theme-changed'))
  }

  return { theme, toggleTheme }
}
