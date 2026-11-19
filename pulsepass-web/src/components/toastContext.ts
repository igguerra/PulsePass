import { createContext, use } from 'react'

export type ToastType = 'success' | 'error' | 'info'

export interface ToastValue {
  showToast: (type: ToastType, message: string) => void
}

export const ToastContext = createContext<ToastValue | null>(null)

export function useToast(): ToastValue {
  const value = use(ToastContext)
  if (!value) throw new Error('useToast debe usarse dentro de <ToastProvider>')
  return value
}