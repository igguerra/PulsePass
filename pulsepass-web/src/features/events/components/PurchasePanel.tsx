import { useState } from 'react'
import { Link, useLocation } from 'react-router'
import Button from '../../../components/Button'
import Card from '../../../components/Card'
import { ROUTES } from '../../../lib/routes'
import { useAuth } from '../../auth/hooks'
import PurchaseModal from '../../tickets/components/PurchaseModal'
import type { EventResponse, EventStatus } from '../types'

// Motivo visible cuando no se puede comprar (FR-FE-EVT-005)
const UNAVAILABLE_REASON: Record<Exclude<EventStatus, 'PUBLISHED'>, string> = {
  DRAFT: 'Este evento aún no está publicado.',
  SOLD_OUT: 'Las entradas están agotadas.',
  CANCELLED: 'Este evento fue cancelado.',
  FINISHED: 'Este evento ya finalizó.',
}

interface PurchasePanelProps {
  event: EventResponse
}

// PanelCompra de la sección 9.5. Solo decide QUÉ mostrar; la compra la valida el backend.
export default function PurchasePanel({ event }: PurchasePanelProps) {
  const { session, hasRole } = useAuth()
  const location = useLocation()
  const [modalOpen, setModalOpen] = useState(false)

  // STAFF/ADMIN sin rol USER: el panel se oculta
  if (session.status === 'authenticated' && !hasRole('USER')) return null

  return (
    <Card className="flex flex-col gap-4">
      <h2 className="text-lg font-semibold">Entradas</h2>

      {event.status !== 'PUBLISHED' ? (
        <>
          <Button disabled>Comprar entrada</Button>
          <p className="text-sm text-slate-600 dark:text-slate-400">{UNAVAILABLE_REASON[event.status]}</p>
        </>
      ) : session.status !== 'authenticated' ? (
        // Anónimo: va a /login y, al iniciar sesión, GuestOnlyRoute lo devuelve aquí (AC-FE-004)
        <Link
          to={ROUTES.login}
          state={{ from: location.pathname }}
          className="flex min-h-11 items-center justify-center rounded-lg bg-marca-600 px-4 text-sm font-semibold text-white hover:bg-marca-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600"
        >
          Inicia sesión para comprar
        </Link>
      ) : (
        <>
          <Button onClick={() => setModalOpen(true)}>Comprar entrada</Button>
          <p className="text-sm text-slate-600 dark:text-slate-400">El precio lo define el sistema al confirmar.</p>
          <PurchaseModal
            open={modalOpen}
            onClose={() => setModalOpen(false)}
            event={{ eventCode: event.eventCode, name: event.name }}
          />
        </>
      )}
    </Card>
  )
}
