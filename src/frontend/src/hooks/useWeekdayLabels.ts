import { useLanguage } from './useLanguage'
import { translations } from '../utils/translations'

// Canonical English weekday keys (Mon–Sun), kept verbatim as object/state keys.
// DO NOT translate — pair with useWeekdayLabels() for display. Shared by the
// working-hours editor and the profile's working-hours display.
export const WEEKDAYS = [
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday'
]

// Maps the canonical English weekday keys (kept verbatim as object/state keys, see
// AddDefaultWorkingHoursModal) to the active language's label. Shared by the
// working-hours editor and the profile's working-hours display.
export function useWeekdayLabels(): Record<string, string> {
  const { language } = useLanguage()
  const t = translations[language].modals

  return {
    Monday: t.monday,
    Tuesday: t.tuesday,
    Wednesday: t.wednesday,
    Thursday: t.thursday,
    Friday: t.friday,
    Saturday: t.saturday,
    Sunday: t.sunday
  }
}
