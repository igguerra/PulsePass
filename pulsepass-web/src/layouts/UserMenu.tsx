import { useEffect, useId, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { useAuth } from '../features/auth/hooks'
import { ROUTES } from '../lib/routes'

const ITEM_CLASSES =
  'block w-full rounded px-3 py-2 text-left text-sm hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-marca-600 dark:hover:bg-slate-800'

export default function UserMenu() {
  const { session, signOut, hasRole } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const menuId = useId()
  const containerRef = useRef<HTMLDivElement>(null)

  // Cierra al hacer clic fuera o presionar Esc
  useEffect(() => {
    if (!open) return
    function handleClick(event: MouseEvent) {
      if (!containerRef.current?.contains(event.target as Node)) setOpen(false)
    }
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('mousedown', handleClick)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  if (session.status !== 'authenticated') return null

  function handleSignOut() {
    signOut()
    navigate(ROUTES.home)
  }

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        aria-expanded={open}
        aria-controls={menuId}
        onClick={() => setOpen((value) => !value)}
        className="flex min-h-11 items-center gap-1 rounded-lg px-3 text-sm font-semibold hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-marca-600 dark:hover:bg-slate-800"
      >
        {session.user.firstName} <span aria-hidden="true">▾</span>
      </button>
      {open && (
        <div id={menuId} className="absolute right-0 z-40 mt-2 w-48 rounded-lg bg-white p-1 shadow-lg dark:bg-slate-900">
          <Link to={ROUTES.account} className={ITEM_CLASSES} onClick={() => setOpen(false)}>
            Mi cuenta
          </Link>
          {hasRole('USER') && (
            <Link to={ROUTES.myTickets} className={ITEM_CLASSES} onClick={() => setOpen(false)}>
              Mis tickets
            </Link>
          )}
          <button type="button" className={ITEM_CLASSES} onClick={handleSignOut}>
            Salir
          </button>
        </div>
      )}
    </div>
  )
}