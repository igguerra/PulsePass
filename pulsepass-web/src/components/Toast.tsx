import clsx from 'clsx'
import { useCallback, useMemo, useRef, useState, type ReactNode } from 'react'
import { ToastContext, type ToastType } from './toastContext'

interface ToastItem {
  id: number
  type: ToastType
  message: string
}

const TONES: Record<ToastType, string> = {
  success: 'bg-emerald-600 text-white',
  error: 'bg-red-600 text-white',
  info: 'bg-slate-800 text-white',
}

const DURATION_MS = 4000

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])
  const nextId = useRef(0)

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    (type: ToastType, message: string) => {
      nextId.current += 1
      const id = nextId.current
      setToasts((current) => [...current, { id, type, message }])
      // 9.20: éxito/info desaparecen a los 4 s; error persiste hasta cerrarlo
      if (type !== 'error') setTimeout(() => dismiss(id), DURATION_MS)
    },
    [dismiss],
  )

  const value = useMemo(() => ({ showToast }), [showToast])

  return (
    <ToastContext value={value}>
      {children}
      <div
        role="status"
        aria-live="polite"
        className="fixed inset-x-4 bottom-4 z-50 flex flex-col gap-2 sm:left-auto sm:w-96"
      >
        {toasts.map((toast) => (
          <div key={toast.id} className={clsx('flex items-start gap-3 rounded-lg p-4 shadow-lg', TONES[toast.type])}>
            <p className="flex-1 text-sm">{toast.message}</p>
            <button
              type="button"
              aria-label="Cerrar aviso"
              onClick={() => dismiss(toast.id)}
              className="rounded px-1 focus-visible:outline-2 focus-visible:outline-white"
            >
              ✕
            </button>
          </div>
        ))}
      </div>
    </ToastContext>
  )
}