import Button from '../../../components/Button'
import Modal from '../../../components/Modal'
import { useToast } from '../../../components/toastContext'
import { TICKET_TYPE_LABEL } from '../../../lib/format'
import { getErrorMessage } from '../../../lib/http'
import { useCancelTicket } from '../hooks'
import type { TicketResponse } from '../types'

interface CancelTicketModalProps {
  open: boolean
  onClose: () => void
  ticket: TicketResponse
}

export default function CancelTicketModal({ open, onClose, ticket }: CancelTicketModalProps) {
  const cancel = useCancelTicket()
  const { showToast } = useToast()

  function handleClose() {
    cancel.reset()
    onClose()
  }

  async function handleConfirm() {
    try {
      await cancel.mutateAsync({ ticketCode: ticket.ticketCode, eventCode: ticket.eventCode })
      showToast('success', 'Ticket cancelado')
      handleClose()
    } catch {
      // El error (por ejemplo un 409) queda en cancel.error y se muestra abajo; el modal sigue abierto
    }
  }

  return (
    <Modal open={open} title="Cancelar ticket" onClose={handleClose}>
      <p className="text-sm">
        ¿Quieres cancelar el ticket <strong className="font-mono">{ticket.ticketCode}</strong> (
        {TICKET_TYPE_LABEL[ticket.type]}) de {ticket.eventName}?
      </p>

      {cancel.error && (
        <p role="alert" className="mt-4 rounded-md bg-red-100 p-3 text-sm text-red-800">
          {getErrorMessage(cancel.error)}
        </p>
      )}

      <div className="mt-6 flex justify-end gap-3">
        <Button variant="secondary" onClick={handleClose}>
          Volver
        </Button>
        <Button variant="danger" onClick={handleConfirm} loading={cancel.isPending}>
          Cancelar ticket
        </Button>
      </div>
    </Modal>
  )
}