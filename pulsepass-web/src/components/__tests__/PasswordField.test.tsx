import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import PasswordField from '../PasswordField'

describe('PasswordField', () => {
  it('muestra y oculta la contraseña sin borrar lo escrito', async () => {
    const user = userEvent.setup()
    render(<PasswordField label="Contraseña" />)
    const input = screen.getByLabelText('Contraseña')

    await user.type(input, 'ClaveSegura123')
    expect(input).toHaveAttribute('type', 'password')

    await user.click(screen.getByRole('button', { name: 'Mostrar contraseña' }))
    expect(input).toHaveAttribute('type', 'text')
    expect(input).toHaveValue('ClaveSegura123')

    await user.click(screen.getByRole('button', { name: 'Ocultar contraseña' }))
    expect(input).toHaveAttribute('type', 'password')
  })
})
