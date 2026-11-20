import Modal from '../../../components/Modal'
import { TICKET_TYPE_LABEL } from '../../../lib/format'
import type { TicketType } from '../types'

// CONTRATO compartido: EventDetail (events) abre este modal con el tipo ya elegido;
// tickets lo implementa (confirmación + POST /api/tickets). No cambien las props sin avisar.
export interface PurchaseModalProps {
  open: boolean
  onClose: () => void
  event: { eventCode: string; name: string }
  type: TicketType
}

export default function PurchaseModal({ open, onClose, event, type }: PurchaseModalProps) {
  return (
    <Modal open={open} title="Confirmar compra" onClose={onClose}>
      <p className="text-sm">Evento: {event.name}</p>
      <p className="text-sm">Tipo: {TICKET_TYPE_LABEL[type]}</p>
      <p className="mt-4 text-sm text-slate-600 dark:text-slate-400">Confirmación en construcción.</p>
    </Modal>
  )
}
