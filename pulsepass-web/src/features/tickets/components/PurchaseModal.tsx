import Modal from '../../../components/Modal'

// CONTRATO compartido: EventDetail (events) abre este modal; tickets lo implementa.
// No cambien las props sin avisarle a la otra persona.
export interface PurchaseModalProps {
  open: boolean
  onClose: () => void
  event: { eventCode: string; name: string }
}

export default function PurchaseModal({ open, onClose, event }: PurchaseModalProps) {
  return (
    <Modal open={open} title={`Comprar entrada · ${event.name}`} onClose={onClose}>
      <p className="text-sm text-slate-600 dark:text-slate-400">Formulario de compra en construcción.</p>
    </Modal>
  )
}