import type { ReactNode } from 'react'

interface EmptyStateProps {
  message: string
  action?: ReactNode
}

export default function EmptyState({ message, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-xl border border-dashed border-slate-300 p-8 text-center dark:border-slate-700">
      <span aria-hidden="true" className="text-3xl">
        🎫
      </span>
      <p className="text-slate-600 dark:text-slate-400">{message}</p>
      {action}
    </div>
  )
}