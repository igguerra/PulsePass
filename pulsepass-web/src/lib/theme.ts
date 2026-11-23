// Modo claro / oscuro. La elección se guarda en localStorage para que se mantenga
// al recargar y entre pestañas. Si nunca se ha elegido, se usa el del sistema.
export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'pulsepass.theme'

function readSavedTheme(): Theme | null {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved === 'light' || saved === 'dark' ? saved : null
  } catch {
    return null
  }
}

export function getInitialTheme(): Theme {
  return readSavedTheme() ?? (window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
}

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark')
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch {
    // Sin almacenamiento (modo privado estricto): el tema funciona igual, solo no se recuerda
  }
}
