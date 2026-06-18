import React from 'react'
import { useLanguage } from '../../hooks/useLanguage'
import { translations } from '../../utils/translations'

interface Props {
  value: string
  onChange: (value: string) => void
}

// The labelled department-name input shared by the add and edit department modals;
// only their heading and submit button differ, so just the field lives here.
const DepartmentNameField: React.FC<Props> = ({ value, onChange }) => {
  const { language } = useLanguage()
  const t = translations[language].modals

  return (
    <label>
      {t.nameOfDepartment}
      <input
        type="text"
        placeholder={t.deptNamePlaceholder}
        value={value}
        onChange={e => onChange(e.target.value)}
        maxLength={100}
      />
    </label>
  )
}

export default DepartmentNameField
