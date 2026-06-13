import React from 'react'

interface Props {
  isLoading: boolean
  error: Error | null
  // True once the request has loaded; when it is false and we are neither loading
  // nor erroring, the request simply wasn't found (or the caller may not see it).
  loaded: boolean
}

// The shared loading / error / not-found block for the view and edit leave modals.
// Renders nothing once the request has loaded — the parent renders the body then.
const LeaveRequestLoadState: React.FC<Props> = ({ isLoading, error, loaded }) => {
  if (isLoading) return <p>Loading request…</p>
  if (error) return <p className="form-error">{error.message}</p>
  if (!loaded) return <p className="form-error">This leave request could not be loaded.</p>
  return null
}

export default LeaveRequestLoadState
