// Email validation shared across the login page and the add/edit employee
// modals so the rule stays consistent. The pattern requires a non-empty local
// part, a single @, a domain, and a top-level domain of at least two letters.
// Matching is case-insensitive since email domains are not case-sensitive in
// practice, so capitalised addresses are accepted as valid.
const EMAIL_PATTERN = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/i

export const isValidEmail = (email: string): boolean => EMAIL_PATTERN.test(email.trim())
