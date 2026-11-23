import { useState } from 'react'
import { applyTheme, getInitialTheme, type Theme } from '../lib/theme'

export default function ThemeToggle() {
  // index.html ya puso la clase "dark" antes de pintar; aquí solo se lee el mismo valor
  const [theme, setTheme] = useState<Theme>(getInitialTheme)
  const isDark = theme === 'dark'

  function toggle() {
    const next: Theme = isDark ? 'light' : 'dark'
    applyTheme(next)
    setTheme(next)
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={isDark ? 'Activar modo claro' : 'Activar modo oscuro'}
      title={isDark ? 'Modo claro' : 'Modo oscuro'}
      className="flex size-11 items-center justify-center rounded-lg text-slate-700 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-marca-600 dark:text-slate-300 dark:hover:bg-slate-800"
    >
      {isDark ? <SunIcon /> : <MoonIcon />}
    </button>
  )
}

function SunIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </svg>
  )
}

function MoonIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
    </svg>
  )
}
