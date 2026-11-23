import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import ThemeToggle from '../ThemeToggle'

describe('ThemeToggle', () => {
  beforeEach(() => {
    localStorage.clear()
    document.documentElement.classList.remove('dark')
  })

  it('cambia entre claro y oscuro y recuerda la elección', async () => {
    const user = userEvent.setup()
    localStorage.setItem('pulsepass.theme', 'light')
    render(<ThemeToggle />)

    await user.click(screen.getByRole('button', { name: 'Activar modo oscuro' }))
    expect(document.documentElement).toHaveClass('dark')
    expect(localStorage.getItem('pulsepass.theme')).toBe('dark')

    await user.click(screen.getByRole('button', { name: 'Activar modo claro' }))
    expect(document.documentElement).not.toHaveClass('dark')
    expect(localStorage.getItem('pulsepass.theme')).toBe('light')
  })
})
