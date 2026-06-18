import { useState, useEffect } from 'react'
import type { Language } from '../utils/translations.ts'
import { preferencesService } from '../services/api'
import {
  DEFAULT_LANGUAGE,
  LANGUAGE_STORAGE_KEY,
  LANGUAGE_CHANGED_EVENT,
  applyLanguage,
  languageToApi
} from '../utils/preferences'

export const useLanguage = () => {
  const [language, setLanguage] = useState<Language>(() => {
    return (localStorage.getItem(LANGUAGE_STORAGE_KEY) as Language) || DEFAULT_LANGUAGE
  })

  useEffect(() => {
    const handleSync = () => {
      setLanguage((localStorage.getItem(LANGUAGE_STORAGE_KEY) as Language) || DEFAULT_LANGUAGE)
    }

    window.addEventListener(LANGUAGE_CHANGED_EVENT, handleSync)
    return () => window.removeEventListener(LANGUAGE_CHANGED_EVENT, handleSync)
  }, [])

  useEffect(() => {
    document.documentElement.lang = language
  }, [language])

  // Update state, save locally + broadcast (applyLanguage), then persist to the
  // signed-in user's profile. Best-effort, like the theme toggle: a failed save
  // leaves the local choice in place rather than reverting it.
  const changeLanguage = (newLang: Language) => {
    setLanguage(newLang)
    applyLanguage(newLang)
    preferencesService.update({ language: languageToApi(newLang) }).catch(() => {})
  }

  return { language, changeLanguage }
}
