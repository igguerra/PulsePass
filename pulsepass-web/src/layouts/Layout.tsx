import { useEffect, useEffectEvent } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../features/auth/hooks'
import { ROUTES } from '../lib/routes'
import Footer from './Footer'
import Navbar from './Navbar'

// Si la sesión expira (401 o temporizador) estando en cualquier página, se va a /login
function useRedirectOnExpiry() {
  const { session } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const expired = session.status === 'anonymous' && session.reason === 'expired'

  const goToLogin = useEffectEvent(() => {
    if (location.pathname === ROUTES.login) return
    navigate(ROUTES.login, { replace: true, state: { from: location.pathname, reason: 'expired' } })
  })

  useEffect(() => {
    if (expired) goToLogin()
  }, [expired])
}

export default function Layout() {
  useRedirectOnExpiry()

  return (
    <div className="flex min-h-dvh flex-col">
      <a
        href="#content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded focus:bg-marca-600 focus:px-3 focus:py-2 focus:text-white"
      >
        Saltar al contenido
      </a>
      <Navbar />
      <main id="content" className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}