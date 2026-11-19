import { Navigate, Outlet, useLocation } from 'react-router'
import Loading from '../components/Loading'
import { useAuth } from '../features/auth/hooks'
import type { Role } from '../features/auth/types'
import { ROUTES } from '../lib/routes'

interface RoleRouteProps {
  roles: Role[]
}

export default function RoleRoute({ roles }: RoleRouteProps) {
  const { session } = useAuth()
  const location = useLocation()

  if (session.status === 'loading') return <Loading label="Verificando permisos…" />
  if (session.status === 'anonymous') {
    return <Navigate to={ROUTES.login} replace state={{ from: location.pathname, reason: session.reason }} />
  }
  if (!roles.some((role) => session.user.roles.includes(role))) {
    return <Navigate to={ROUTES.forbidden} replace />
  }
  return <Outlet />
}