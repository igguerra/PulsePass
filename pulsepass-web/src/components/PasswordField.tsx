import clsx from 'clsx'
import { useId, useState, type ComponentProps } from 'react'
import { CONTROL_CLASSES } from './styles'

// Igual que TextField, pero con un botón para mostrar u ocultar la contraseña.
// Solo cambia el type del input: lo que se envía al backend es lo mismo.
interface PasswordFieldProps extends Omit<ComponentProps<'input'>, 'type'> {
  label: string
  error?: string | undefined
}

export default function PasswordField({ label, error, id, className, ...rest }: PasswordFieldProps) {
  const generatedId = useId()
  const fieldId = id ?? generatedId
  const errorId = `${fieldId}-error`
  const [visible, setVisible] = useState(false)

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={fieldId} className="text-sm font-medium">
        {label}
      </label>
      <div className="relative">
        <input
          id={fieldId}
          type={visible ? 'text' : 'password'}
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? errorId : undefined}
          className={clsx(CONTROL_CLASSES, 'pr-12', className)}
          {...rest}
        />
        <button
          type="button"
          onClick={() => setVisible((value) => !value)}
          aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          aria-controls={fieldId}
          className="absolute inset-y-0 right-0 flex w-11 items-center justify-center rounded-r-lg text-slate-500 hover:text-slate-800 focus-visible:outline-2 focus-visible:outline-marca-600 dark:text-slate-400 dark:hover:text-slate-100"
        >
          {visible ? <EyeOffIcon /> : <EyeIcon />}
        </button>
      </div>
      {error && (
        <p id={errorId} className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      )}
    </div>
  )
}

function EyeIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.6 5.1A10.8 10.8 0 0 1 12 5c6.5 0 10 7 10 7a17.6 17.6 0 0 1-2.4 3.4" />
      <path d="M6.6 6.6A17.3 17.3 0 0 0 2 12s3.5 7 10 7a10.4 10.4 0 0 0 5.4-1.6" />
      <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
      <path d="m2 2 20 20" />
    </svg>
  )
}
