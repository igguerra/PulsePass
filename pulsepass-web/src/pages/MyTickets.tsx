import { useState } from 'react'
import clsx from 'clsx'
import { Link, useSearchParams } from 'react-router'
import Badge from '../components/Badge'
import Button from '../components/Button'
import Card from '../components/Card'
import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import PriceCOP from '../components/PriceCOP'
import CancelTicketModal from '../features/tickets/components/CancelTicketModal'
import { useMyTickets } from '../features/tickets/hooks'
import { TicketStatusSchema, type TicketStatus } from '../features/tickets/types'
import { formatDateTime, TICKET_STATUS_LABEL, TICKET_TYPE_LABEL } from '../lib/format'
import { getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

interface Filter {
  value: TicketStatus | null
  label: string
}

const FILTERS: Filter[] = [
  { value: null, label: 'Todos' },
  { value: 'PAID', label: 'Pagados' },
  { value: 'USED', label: 'Usados' },
  { value: 'CANCELLED', label: 'Cancelados' },
]

export default function MyTickets() {
  const [searchParams, setSearchParams] = useSearchParams()
  // El parámetro de la URL viene "de afuera": se valida con zod (FE-002)
  const parsed = TicketStatusSchema.safeParse(searchParams.get('status'))
  const activeStatus: TicketStatus | null = parsed.success ? parsed.data : null

  const [cancelCode, setCancelCode] = useState<string | null>(null)
  const query = useMyTickets()

  function selectFilter(value: TicketStatus | null) {
    setSearchParams(value ? { status: value } : {})
  }

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

  const tickets = query.data
  const visible = activeStatus ? tickets.filter((t) => t.status === activeStatus) : tickets
  const ticketToCancel = tickets.find((t) => t.ticketCode === cancelCode)

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">Mis tickets</h1>

      <div className="flex flex-wrap gap-2" role="group" aria-label="Filtrar por estado">
        {FILTERS.map((filter) => {
          const active = filter.value === activeStatus
          return (
            <button
              key={filter.label}
              type="button"
              aria-pressed={active}
              onClick={() => selectFilter(filter.value)}
              className={clsx(
                'min-h-11 rounded-full border px-4 text-sm font-medium',
                'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600',
                active
                  ? 'border-marca-600 bg-marca-600 text-white'
                  : 'border-slate-300 bg-white text-slate-700 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300',
              )}
            >
              {filter.label}
            </button>
          )
        })}
      </div>

      {tickets.length === 0 ? (
        <EmptyState
          message="Aún no tienes tickets"
          action={
            <Link to={ROUTES.events} className="font-medium text-marca-600 underline">
              Ver cartelera
            </Link>
          }
        />
      ) : visible.length === 0 ? (
        <EmptyState message="No tienes tickets con este estado" />
      ) : (
        <ul className="flex flex-col gap-3">
          {visible.map((ticket) => (
            <li key={ticket.ticketCode}>
              <Card>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold">{ticket.eventName}</p>
                    <p className="text-sm text-slate-600 dark:text-slate-400">
                      <span className="font-mono">{ticket.ticketCode}</span> ·{' '}
                      {TICKET_TYPE_LABEL[ticket.type]} · <PriceCOP value={ticket.price} /> ·{' '}
                      {formatDateTime(ticket.purchaseDate)}
                    </p>
                  </div>
                  <Badge tone={ticket.status}>{TICKET_STATUS_LABEL[ticket.status]}</Badge>
                </div>
                <div className="mt-3 flex justify-end gap-2">
                  <Link
                    to={ROUTES.ticket(ticket.ticketCode)}
                    className="inline-flex min-h-11 items-center rounded-lg border border-slate-300 px-4 text-sm font-semibold dark:border-slate-700"
                  >
                    Ver detalle
                  </Link>
                  {ticket.status === 'PAID' && (
                    <Button variant="danger" onClick={() => setCancelCode(ticket.ticketCode)}>
                      Cancelar
                    </Button>
                  )}
                </div>
              </Card>
            </li>
          ))}
        </ul>
      )}

      {ticketToCancel && (
        <CancelTicketModal open ticket={ticketToCancel} onClose={() => setCancelCode(null)} />
      )}
    </div>
  )
}