import { QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { ToastProvider } from '../components/Toast'
import { AuthProvider } from '../features/auth/AuthContext'
import { createQueryClient } from '../lib/queryClient'
import { appRoutes } from '../router'
import { USERS, type DemoUserName } from './mocks/data'

interface Options {
  /** URL inicial, p. ej. '/eventos/CMF-2026' */
  route?: string
  /** Si se indica, la prueba arranca con esa persona autenticada */
  as?: DemoUserName
}

// Renderiza la APP REAL (mismas rutas, guardas y layout) en memoria.
export function renderWithProviders({ route = '/', as }: Options = {}) {
  if (as) {
    const { token } = USERS[as]
    // Igual que tras un login real: AuthProvider lo restaura y llama /me + by-username (MSW)
    sessionStorage.setItem('pulsepass.session', JSON.stringify({ token, expiresAt: Date.now() + 900_000 }))
  }
  const queryClient = createQueryClient()
  queryClient.setDefaultOptions({ queries: { retry: false }, mutations: { retry: false } })
  const router = createMemoryRouter(appRoutes, { initialEntries: [route] })

  const result = render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <RouterProvider router={router} />
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>,
  )
  return { ...result, router, queryClient }
}