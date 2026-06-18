import { useMutation } from '@tanstack/react-query'
import { exportService } from '../services/api'

// Triggers a browser "Save as" for `blob` under `filename` via a short-lived
// object-URL anchor, then revokes the URL so it isn't leaked.
function saveBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

// Downloads the full data export (a ZIP of departments/employees/users CSVs)
// from GET /api/export and saves it to disk. It's a one-shot action rather than
// cached data, so it's modelled as a mutation with no cache invalidation.
export function useExportData() {
  return useMutation({
    mutationFn: () => exportService.download(),
    onSuccess: blob => saveBlob(blob, 'export.zip')
  })
}
