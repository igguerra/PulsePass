import { QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { ToastProvider } from './components/Toast'
import { AuthProvider } from './features/auth/AuthContext'
import './index.css'
import { queryClient } from './lib/queryClient'
import { router } from './router'

const root = document.getElementById('root')
if (!root) throw new Error('No existe #root en index.html')

createRoot(root).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <RouterProvider router={router} />
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)
