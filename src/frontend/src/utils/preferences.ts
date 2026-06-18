import type { Language } from './translations'

// The frontend models theme as a plain light|dark toggle; the backend's UserTheme
// enum is broader (light|dark|system). The UI has no `system` control, so values
// are mapped at the API boundary by the helpers below.
export type Theme = 'light' | 'dark'

// localStorage keys and the custom events that keep every mounted useTheme /
// useLanguage instance — and the on-login hydration step — in sync. Centralised
// here so those consumers can't drift on the string literals.
export const THEME_STORAGE_KEY = 'app-theme'
export const LANGUAGE_STORAGE_KEY = 'app-language'
export const THEME_CHANGED_EVENT = 'theme-changed'
export const LANGUAGE_CHANGED_EVENT = 'language-changed'

export const DEFAULT_THEME: Theme = 'light'
export const DEFAULT_LANGUAGE: Language = 'en'

// --- API value mapping -----------------------------------------------------

// Backend UserTheme is light|dark|system; with no `system` option in the UI it
// collapses to the closest concrete theme (dark stays dark, light/system → light).
export function themeFromApi(value: string | undefined): Theme {
  return value === 'dark' ? 'dark' : 'light'
}

export function themeToApi(theme: Theme): string {
  return theme
}

// Backend UserLanguage is en|ukr; the UI (and translations) is keyed by en|uk.
export function languageFromApi(value: string | undefined): Language {
  return value === 'ukr' ? 'uk' : 'en'
}

export function languageToApi(language: Language): string {
  return language === 'uk' ? 'ukr' : 'en'
}

// --- Local application -----------------------------------------------------

// Write a preference into the store the UI reads from and broadcast it, so every
// mounted useTheme / useLanguage hook re-reads immediately. Shared by the hooks'
// own setters and the on-login server-hydration step in AuthContext.
export function applyTheme(theme: Theme): void {
  localStorage.setItem(THEME_STORAGE_KEY, theme)
  document.documentElement.setAttribute('data-theme', theme)
  window.dispatchEvent(new Event(THEME_CHANGED_EVENT))
}

export function applyLanguage(language: Language): void {
  localStorage.setItem(LANGUAGE_STORAGE_KEY, language)
  document.documentElement.lang = language
  window.dispatchEvent(new Event(LANGUAGE_CHANGED_EVENT))
}
