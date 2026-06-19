import { useLanguage } from './useLanguage'
import { translations } from '../utils/translations'

// Maps the API's English leave type/status enums to the active language's label.
// Shared by the "My requests" and "Request manager" row renderers, which both show
// the same translated badges over the same English CSS classes.
export function useRequestLabels() {
  const { language } = useLanguage()
  const t = translations[language].requestFilters

  const typeMap: Record<string, string> = {
    Vacation: t.typeVacation,
    Sick: t.typeSick,
    Personal: t.typePersonal
  }

  const statusMap: Record<string, string> = {
    PENDING: t.statusPending,
    APPROVED: t.statusApproved,
    REJECTED: t.statusRejected,
    CANCELLED: t.statusCancelled
  }

  return { typeMap, statusMap }
}
