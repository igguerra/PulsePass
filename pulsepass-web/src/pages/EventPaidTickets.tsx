import { useParams } from 'react-router'
import Badge from '../components/Badge'
import Button from '../components/Button'
import EmptyState from '../components/EmptyState'
import ErrorState from '../components/ErrorState'
import Loading from '../components/Loading'
import PriceCOP from '../components/PriceCOP'
import Table from '../components/Table'
import { useToast } from '../components/toastContext'
import { useEventPaidTickets, useMarkTicketUsed } from '../features/tickets/hooks'
import type { TicketResponse } from '../features/tickets/types'
import { TICKET_STATUS_LABEL, TICKET_TYPE_LABEL } from '../lib/format'
import { getErrorMessage } from '../lib/http'

export default function EventPaidTickets() {
  const { eventCode = '' } = useParams()
  const query = useEventPaidTickets(eventCode)
  const markUsed = useMarkTicketUsed()
  const { showToast } = useToast()

  async function handleUse(ticket: TicketResponse) {
    try {
      await markUsed.mutateAsync({ ticketCode: ticket.ticketCode, eventCode })
      showToast('success', 'Ingreso registrado')
    } catch (error: unknown) {
      showToast('error', getErrorMessage(error))
    }
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
  if (query.data.length === 0) return <EmptyState message="No hay tickets pagados" />

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">Tickets pagados · {eventCode}</h1>
      <p className="text-sm text-slate-600 dark:text-slate-400">Total: {query.data.length}</p>

      <Table
        caption={`Tickets pagados del evento ${eventCode}`}
        rows={query.data}
        getKey={(ticket) => ticket.ticketCode}
        columns={[
          { header: 'Código', cell: (t) => <span className="font-mono">{t.ticketCode}</span> },
          { header: 'Tipo', cell: (t) => TICKET_TYPE_LABEL[t.type] },
          { header: 'Titular', cell: (t) => t.userEmail },
          { header: 'Precio', cell: (t) => <PriceCOP value={t.price} /> },
          {
            header: 'Estado',
            cell: (t) => <Badge tone={t.status}>{TICKET_STATUS_LABEL[t.status]}</Badge>,
          },
          {
            header: 'Acción',
            cell: (t) => (
              <Button
                loading={markUsed.isPending && markUsed.variables?.ticketCode === t.ticketCode}
                onClick={() => handleUse(t)}
              >
                Usar
              </Button>
            ),
          },
        ]}
      />
    </div>
  )
}