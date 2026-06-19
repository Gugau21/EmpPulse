import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import { queryClient } from './lib/queryClient'
import { AuthProvider } from './context/AuthContext'
import ServerStatusGate from './components/ServerStatusGate'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        {/* Router lives inside AuthProvider: route guards call useAuth. */}
        <BrowserRouter>
          {/* Hides the entire app behind an offline screen when the backend is down. */}
          <ServerStatusGate>
            <App />
          </ServerStatusGate>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>
)
