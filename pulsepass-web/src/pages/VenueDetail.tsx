import { Link, useParams } from 'react-router'
import Card from '../components/Card'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import MessageScreen from '../components/MessageScreen'
import { useVenue } from '../features/venues/hooks'
import { ApiError, getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function VenueDetail() {
  const { code = '' } = useParams()
  const { data: venue, isPending, isError, error, refetch } = useVenue(code)

  if (isPending) return <Loading label="Cargando sede…" />

  if (isError) {
    // FR-FE-VEN-002: un 404 se muestra como "no encontrada", sin mensaje técnico
    if (error instanceof ApiError && error.status === 404) {
      return <MessageScreen code="404" title="No encontramos esta sede" />
    }
    return <ErrorState message={getErrorMessage(error)} onRetry={() => void refetch()} />
  }

  return (
    <section className="flex flex-col gap-6">
      <Link to={ROUTES.venues} className="text-sm text-marca-600 hover:underline">
        ← Volver a sedes
      </Link>

      <Card className="flex flex-col gap-4">
        <div>
          <h1 className="text-2xl font-bold">{venue.name}</h1>
          <p className="text-slate-600 dark:text-slate-400">{venue.city}</p>
        </div>

        <dl className="grid gap-3 sm:grid-cols-3">
          <div>
            <dt className="text-sm text-slate-600 dark:text-slate-400">Código</dt>
            <dd className="font-medium">{venue.code}</dd>
          </div>
          <div>
            <dt className="text-sm text-slate-600 dark:text-slate-400">Dirección</dt>
            <dd className="font-medium">{venue.address ?? 'Sin dirección registrada'}</dd>
          </div>
          <div>
            <dt className="text-sm text-slate-600 dark:text-slate-400">Capacidad</dt>
            <dd className="font-medium">{venue.capacity.toLocaleString('es-CO')} personas</dd>
          </div>
        </dl>
      </Card>
    </section>
  )
}
