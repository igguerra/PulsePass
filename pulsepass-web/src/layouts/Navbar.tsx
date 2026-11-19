import clsx from 'clsx'
import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router'
import { useAuth } from '../features/auth/hooks'
import { ROUTES } from '../lib/routes'
import UserMenu from './UserMenu'

interface NavItem {
  to: string
  text: string
}

const PUBLIC_LINKS: NavItem[] = [
  { to: ROUTES.events, text: 'Eventos' },
  { to: ROUTES.artists, text: 'Artistas' },
  { to: ROUTES.venues, text: 'Sedes' },
]

const linkClasses = ({ isActive }: { isActive: boolean }) =>
  clsx(
    'flex min-h-11 items-center rounded-lg px-3 text-sm font-medium focus-visible:outline-2 focus-visible:outline-marca-600',
    isActive
      ? 'bg-marca-50 text-marca-700 dark:bg-slate-800 dark:text-marca-500'
      : 'text-slate-700 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800',
  )

export default function Navbar() {
  const { session, hasRole, signOut } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  // Tabla 8.4: enlaces según rol (solo UX; la API vuelve a validar)
  const roleLinks: NavItem[] = [
    ...(hasRole('USER') ? [{ to: ROUTES.myTickets, text: 'Mis tickets' }] : []),
    ...(hasRole('STAFF', 'ADMIN') ? [{ to: ROUTES.staffValidation, text: 'Validar ingreso' }] : []),
    ...(hasRole('ADMIN') ? [{ to: ROUTES.adminEvents, text: 'Administrar eventos' }] : []),
  ]
  const links = [...PUBLIC_LINKS, ...roleLinks]
  const closeMenu = () => setMenuOpen(false)

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-950/90">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-2">
        <Link to={ROUTES.home} className="font-titulo text-lg font-bold text-marca-600">
          <span aria-hidden="true">♪ </span>PulsePass
        </Link>

        {/* Escritorio (≥ md) */}
        <nav aria-label="Principal" className="hidden flex-1 items-center gap-1 md:flex">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className={linkClasses}>
              {link.text}
            </NavLink>
          ))}
        </nav>
        <div className="hidden items-center gap-2 md:flex">
          {session.status === 'authenticated' ? (
            <UserMenu />
          ) : (
            <>
              <NavLink to={ROUTES.login} className={linkClasses}>
                Ingresar
              </NavLink>
              <Link
                to={ROUTES.register}
                className="flex min-h-11 items-center rounded-lg bg-marca-600 px-4 text-sm font-semibold text-white hover:bg-marca-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600"
              >
                Registrarse
              </Link>
            </>
          )}
        </div>

        {/* Móvil (< md) */}
        <button
          type="button"
          className="flex size-11 items-center justify-center rounded-lg text-xl hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-marca-600 md:hidden dark:hover:bg-slate-800"
          aria-expanded={menuOpen}
          aria-controls="mobile-menu"
          aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
          onClick={() => setMenuOpen((value) => !value)}
        >
          <span aria-hidden="true">{menuOpen ? '✕' : '☰'}</span>
        </button>
      </div>

      {menuOpen && (
        <nav id="mobile-menu" aria-label="Principal móvil" className="border-t border-slate-200 px-4 py-2 md:hidden dark:border-slate-800">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className={linkClasses} onClick={closeMenu}>
              {link.text}
            </NavLink>
          ))}
          <hr className="my-2 border-slate-200 dark:border-slate-800" />
          {session.status === 'authenticated' ? (
            <>
              <NavLink to={ROUTES.account} className={linkClasses} onClick={closeMenu}>
                Mi cuenta
              </NavLink>
              <button
                type="button"
                className="flex min-h-11 w-full items-center rounded-lg px-3 text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800"
                onClick={() => {
                  closeMenu()
                  signOut()
                  navigate(ROUTES.home)
                }}
              >
                Salir
              </button>
            </>
          ) : (
            <>
              <NavLink to={ROUTES.login} className={linkClasses} onClick={closeMenu}>
                Ingresar
              </NavLink>
              <NavLink to={ROUTES.register} className={linkClasses} onClick={closeMenu}>
                Registrarse
              </NavLink>
            </>
          )}
        </nav>
      )}
    </header>
  )
}