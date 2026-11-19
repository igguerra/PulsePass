import { Navigate, Outlet, useLocation } from 'react-router'
import Loading from '../components/Loading'
import { useAuth } from '../features/auth/hooks'
import { ROUTES } from '../lib/routes'

// Solo experiencia de usuario (FE-004): la seguridad real la aplica la API
export default function ProtectedRoute() {
  const { session } = useAuth()
  const location = useLocation()

  if (session.status === 'loading') return <Loading label="Verificando sesión…" />
  if (session.status === 'anonymous') {
    return <Navigate to={ROUTES.login} replace state={{ from: location.pathname, reason: session.reason }} />
  }
  return <Outlet />
}