import { Navigate, Outlet, useLocation } from 'react-router'
import Loading from '../components/Loading'
import { useAuth } from '../features/auth/hooks'
import { readNavigationState, ROUTES } from '../lib/routes'

// /login y /registro: si ya hay sesión, no tiene sentido mostrarlos
export default function GuestOnlyRoute() {
  const { session } = useAuth()
  const location = useLocation()

  if (session.status === 'loading') return <Loading />
  if (session.status === 'authenticated') {
    const { from } = readNavigationState(location.state)
    return <Navigate to={from ?? ROUTES.home} replace />
  }
  return <Outlet />
}