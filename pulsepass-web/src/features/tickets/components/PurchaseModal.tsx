import { useNavigate } from 'react-router'
import Button from '../../../components/Button'
import Modal from '../../../components/Modal'
import { TICKET_TYPE_LABEL } from '../../../lib/format'
import { getErrorMessage } from '../../../lib/http'
import { ROUTES } from '../../../lib/routes'
import { useAuth } from '../../auth/hooks'
import { usePurchaseTicket } from '../hooks'
import type { TicketType } from '../types'

// CONTRATO compartido: EventDetail (events) abre este modal con el tipo ya elegido.
// No cambien las props sin avisar.
export interface PurchaseModalProps {
  open: boolean
  onClose: () => void
  event: { eventCode: string; name: string }
  type: TicketType
}

export default function PurchaseModal({ open, onClose, event, type }: PurchaseModalProps) {
  const { session } = useAuth()
  const navigate = useNavigate()
  const purchase = usePurchaseTicket()

  // Sin sesión no hay cuenta desde donde comprar (EventDetail manda a login antes)
  if (session.status !== 'authenticated') return null
  const email = session.user.email

  function handleClose() {
    purchase.reset()
    onClose()
  }

  async function handleConfirm() {
    try {
      const ticket = await purchase.mutateAsync({
        userEmail: email,
        eventCode: event.eventCode,
        type,
      })
      navigate(ROUTES.purchaseConfirmation(ticket.ticketCode))
    } catch {
      // El error queda en purchase.error y se muestra abajo, dentro del modal
    }
  }

  return (
    <Modal open={open} title="Confirmar compra" onClose={handleClose}>
      <div className="flex flex-col gap-2 text-sm">
        <p>
          Evento: <strong>{event.name}</strong>
        </p>
        <p>
          Tipo: <strong>{TICKET_TYPE_LABEL[type]}</strong>
        </p>
        <p>
          Cuenta: <strong>{email}</strong>
        </p>
      </div>

      {purchase.error && (
        <p role="alert" className="mt-4 rounded-md bg-red-100 p-3 text-sm text-red-800">
          {getErrorMessage(purchase.error)}
        </p>
      )}

      <div className="mt-6 flex justify-end gap-3">
        <Button variant="secondary" onClick={handleClose}>
          Cancelar
        </Button>
        <Button onClick={handleConfirm} loading={purchase.isPending}>
          Confirmar
        </Button>
      </div>
    </Modal>
  )
}