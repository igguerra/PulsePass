import clsx from 'clsx'
import { useId, type ComponentProps } from 'react'
import { CONTROL_CLASSES } from './styles'

interface SelectProps extends ComponentProps<'select'> {
  label: string
  error?: string | undefined
}

export default function Select({ label, error, id, className, children, ...rest }: SelectProps) {
  const generatedId = useId()
  const fieldId = id ?? generatedId
  const errorId = `${fieldId}-error`

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={fieldId} className="text-sm font-medium">
        {label}
      </label>
      <select
        id={fieldId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={clsx(CONTROL_CLASSES, className)}
        {...rest}
      >
        {children}
      </select>
      {error && (
        <p id={errorId} className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      )}
    </div>
  )
}