import { Link, useParams } from 'react-router'
import Badge from '../components/Badge'
import Card from '../components/Card'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import PriceCOP from '../components/PriceCOP'
import TicketUnavailable from '../features/tickets/components/TicketUnavailable'
import { isUnavailableError } from '../features/tickets/errors'
import { useTicket } from '../features/tickets/hooks'
import { TICKET_STATUS_LABEL, TICKET_TYPE_LABEL } from '../lib/format'
import { getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function PurchaseConfirmation() {
  const { ticketCode = '' } = useParams()
  const query = useTicket(ticketCode)

  if (query.isPending) return <Loading />
  if (query.isError) {
    if (isUnavailableError(query.error)) return <TicketUnavailable />
    return (
      <ErrorState
        message={getErrorMessage(query.error)}
        onRetry={() => {
          void query.refetch()
        }}
      />
    )
  }

  const ticket = query.data

  return (
    <Card className="mx-auto max-w-md p-6 text-center">
      <h1 className="text-2xl font-semibold">¡Compra realizada con éxito!</h1>
      <p className="mt-3 font-medium">{ticket.eventName}</p>
      <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
        Ticket <span className="font-mono">{ticket.ticketCode}</span> ·{' '}
        {TICKET_TYPE_LABEL[ticket.type]} · <PriceCOP value={ticket.price} />
      </p>
      <div className="mt-2">
        <Badge tone={ticket.status}>{TICKET_STATUS_LABEL[ticket.status]}</Badge>
      </div>
      <div className="mt-6 flex flex-wrap justify-center gap-4">
        <Link to={ROUTES.myTickets} className="font-medium text-marca-600 underline">
          Ver mis tickets
        </Link>
        <Link to={ROUTES.events} className="font-medium text-marca-600 underline">
          Seguir explorando
        </Link>
      </div>
    </Card>
  )
}