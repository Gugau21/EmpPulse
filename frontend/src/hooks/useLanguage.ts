import { useState, useEffect } from 'react'
import type { Language } from '../utils/translations.ts'

export const useLanguage = () => {
  const [language, setLanguage] = useState<Language>(() => {
    return (localStorage.getItem('app-language') as Language) || 'en'
  })

  useEffect(() => {
    const handleSync = () => {
      setLanguage((localStorage.getItem('app-language') as Language) || 'en')
    }

    window.addEventListener('language-changed', handleSync)
    return () => window.removeEventListener('language-changed', handleSync)
  }, [])

  useEffect(() => {
    document.documentElement.lang = language
  }, [language])

  const changeLanguage = (newLang: Language) => {
    setLanguage(newLang)
    localStorage.setItem('app-language', newLang)
    document.documentElement.lang = newLang

    window.dispatchEvent(new Event('language-changed'))
  }

  return { language, changeLanguage }
}
