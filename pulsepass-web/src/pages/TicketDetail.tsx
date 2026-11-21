import { useState } from 'react'
import { Link, useParams } from 'react-router'
import Badge from '../components/Badge'
import Button from '../components/Button'
import Card from '../components/Card'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import PriceCOP from '../components/PriceCOP'
import { useToast } from '../components/toastContext'
import CancelTicketModal from '../features/tickets/components/CancelTicketModal'
import TicketUnavailable from '../features/tickets/components/TicketUnavailable'
import { isUnavailableError } from '../features/tickets/errors'
import { useTicket } from '../features/tickets/hooks'
import { formatDateTime, TICKET_STATUS_LABEL, TICKET_TYPE_LABEL } from '../lib/format'
import { getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function TicketDetail() {
  const { ticketCode = '' } = useParams()
  const query = useTicket(ticketCode)
  const { showToast } = useToast()
  const [cancelOpen, setCancelOpen] = useState(false)

  if (query.isPending) return <Loading />
  if (query.isError) {
    // 403 (ticket ajeno) o 404: no se muestra ningún dato del ticket
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

  async function copyCode() {
    try {
      await navigator.clipboard.writeText(ticket.ticketCode)
      showToast('success', 'Código copiado')
    } catch {
      showToast('error', 'No se pudo copiar el código')
    }
  }

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <Link to={ROUTES.myTickets} className="text-sm font-medium text-marca-600">
        ← Mis tickets
      </Link>

      <Card className="p-6">
        <div className="flex items-start justify-between gap-3">
          <h1 className="text-2xl font-semibold">{ticket.eventName}</h1>
          <Badge tone={ticket.status}>{TICKET_STATUS_LABEL[ticket.status]}</Badge>
        </div>

        <dl className="mt-4 divide-y divide-slate-200 text-sm dark:divide-slate-700">
          <div className="flex items-center justify-between gap-3 py-2">
            <dt className="text-slate-600 dark:text-slate-400">Código</dt>
            <dd className="flex items-center gap-2">
              <span className="font-mono">{ticket.ticketCode}</span>
              <Button variant="secondary" onClick={copyCode}>
                Copiar
              </Button>
            </dd>
          </div>
          <div className="flex justify-between gap-3 py-2">
            <dt className="text-slate-600 dark:text-slate-400">Tipo</dt>
            <dd>{TICKET_TYPE_LABEL[ticket.type]}</dd>
          </div>
          <div className="flex justify-between gap-3 py-2">
            <dt className="text-slate-600 dark:text-slate-400">Precio</dt>
            <dd>
              <PriceCOP value={ticket.price} />
            </dd>
          </div>
          <div className="flex justify-between gap-3 py-2">
            <dt className="text-slate-600 dark:text-slate-400">Comprado el</dt>
            <dd>{formatDateTime(ticket.purchaseDate)}</dd>
          </div>
        </dl>

        <p className="mt-4 text-sm text-slate-600 dark:text-slate-400">
          Presenta este código en el ingreso.
        </p>
      </Card>

      {ticket.status === 'PAID' && (
        <div className="flex justify-end">
          <Button variant="danger" onClick={() => setCancelOpen(true)}>
            Cancelar ticket
          </Button>
        </div>
      )}

      <CancelTicketModal open={cancelOpen} ticket={ticket} onClose={() => setCancelOpen(false)} />
    </div>
  )
}