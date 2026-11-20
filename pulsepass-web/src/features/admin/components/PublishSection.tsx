import { useState } from 'react'
import Button from '../../../components/Button'
import Modal from '../../../components/Modal'
import { useToast } from '../../../components/toastContext'
import { getErrorMessage } from '../../../lib/http'
import { usePublishEvent } from '../../events/hooks'
import type { EventResponse } from '../../events/types'

// "Publicar" con modal de confirmación; deshabilitado si el estado ≠ DRAFT (9.18)
export default function PublishSection({ event }: { event: EventResponse }) {
  const publish = usePublishEvent()
  const { showToast } = useToast()
  const [confirmOpen, setConfirmOpen] = useState(false)

  function confirm() {
    setConfirmOpen(false)
    publish.mutate(event.eventCode, {
      onSuccess: () => showToast('success', 'Evento publicado'),
    })
  }

  return (
    <section aria-labelledby="publish" className="flex flex-col gap-3">
      <h2 id="publish" className="text-xl font-semibold">
        Publicación
      </h2>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-600 dark:text-slate-400">
          {event.status === 'DRAFT'
            ? 'El evento es visible al público una vez publicado.'
            : 'Solo se pueden publicar eventos en borrador.'}
        </p>
        <Button disabled={event.status !== 'DRAFT'} loading={publish.isPending} onClick={() => setConfirmOpen(true)}>
          Publicar evento
        </Button>
      </div>

      {/* El backend sigue siendo la autoridad: si responde 409, se muestra aquí */}
      {publish.isError && (
        <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-800 dark:bg-red-950/40 dark:text-red-300">
          {getErrorMessage(publish.error)}
        </p>
      )}

      <Modal open={confirmOpen} title="¿Publicar evento?" onClose={() => setConfirmOpen(false)}>
        <p className="text-sm">
          «{event.name}» quedará visible en la cartelera y se podrán comprar entradas.
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <Button variant="secondary" onClick={() => setConfirmOpen(false)}>
            Cancelar
          </Button>
          <Button onClick={confirm}>Publicar</Button>
        </div>
      </Modal>
    </section>
  )
}
