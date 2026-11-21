import { Link } from 'react-router'
import Card from '../../../components/Card'
import { ROUTES } from '../../../lib/routes'

export default function TicketUnavailable() {
  return (
    <Card className="mx-auto max-w-md p-6 text-center">
      <h1 className="text-xl font-semibold">Ticket no disponible</h1>
      <p className="mt-2 text-slate-600 dark:text-slate-400">No pudimos mostrar este ticket.</p>
      <Link to={ROUTES.myTickets} className="mt-4 inline-block font-medium text-marca-600 underline">
        Volver a mis tickets
      </Link>
    </Card>
  )
}