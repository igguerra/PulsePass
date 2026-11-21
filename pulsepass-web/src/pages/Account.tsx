import { Link } from 'react-router'
import Badge from '../components/Badge'
import Button from '../components/Button'
import Card from '../components/Card'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import { useAuth } from '../features/auth/hooks'
import { useUserByEmail } from '../features/users/hooks'
import { getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function Account() {
  const { session, signOut, hasRole } = useAuth()
  const email = session.status === 'authenticated' ? session.user.email : undefined
  const roles = session.status === 'authenticated' ? session.user.roles : []
  const query = useUserByEmail(email)

  if (query.isPending) return <Loading />
  if (query.isError) {
    return (
      <ErrorState
        message={getErrorMessage(query.error)}
        onRetry={() => {
          void query.refetch()
        }}
      />
    )
  }

  const user = query.data

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <h1 className="text-2xl font-semibold">Mi cuenta</h1>

      <Card className="flex flex-col gap-2 p-6">
        <p className="text-lg font-semibold">
          {user.firstName} {user.lastName}
        </p>
        <p className="text-sm text-slate-600 dark:text-slate-400">
          @{user.username} · {user.email}
        </p>
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Nacimiento: {user.birthDate}
        </p>
        <p className="text-sm text-slate-600 dark:text-slate-400">Rol: {roles.join(', ')}</p>
        <div>
          <Badge tone={user.active ? 'PUBLISHED' : 'CANCELLED'}>
            {user.active ? 'Activa' : 'Inactiva'}
          </Badge>
        </div>
      </Card>

      <div className="flex items-center justify-between gap-3">
        {hasRole('USER') ? (
          <Link to={ROUTES.myTickets} className="font-medium text-marca-600 underline">
            Ver mis tickets
          </Link>
        ) : (
          <span />
        )}
        <Button variant="secondary" onClick={signOut}>
          Cerrar sesión
        </Button>
      </div>
    </div>
  )
}