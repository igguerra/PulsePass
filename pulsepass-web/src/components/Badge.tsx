import type { ReactNode } from 'react'
import type { EventStatus } from '../features/events/types'
import type { TicketStatus } from '../features/tickets/types'

type Tone = EventStatus | TicketStatus

// Mapa de la sección 9.1 (CANCELLED existe en ambos enums con el mismo color)
const TONES: Record<Tone, string> = {
  DRAFT: 'bg-slate-200 text-slate-700',
  PUBLISHED: 'bg-emerald-100 text-emerald-800',
  SOLD_OUT: 'bg-amber-100 text-amber-800',
  CANCELLED: 'bg-red-100 text-red-800',
  FINISHED: 'bg-slate-300 text-slate-800',
  RESERVED: 'bg-sky-100 text-sky-800',
  PAID: 'bg-emerald-100 text-emerald-800',
  USED: 'bg-slate-300 text-slate-800',
}

interface BadgeProps {
  tone: Tone
  children: ReactNode
}

export default function Badge({ tone, children }: BadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${TONES[tone]}`}>
      {children}
    </span>
  )
}