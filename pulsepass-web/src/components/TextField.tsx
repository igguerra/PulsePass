import clsx from 'clsx'
import { useId, type ComponentProps } from 'react'
import { CONTROL_CLASSES } from './styles'

interface TextFieldProps extends ComponentProps<'input'> {
  label: string
  error?: string | undefined
}

export default function TextField({ label, error, id, className, ...rest }: TextFieldProps) {
  const generatedId = useId()
  const fieldId = id ?? generatedId
  const errorId = `${fieldId}-error`

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={fieldId} className="text-sm font-medium">
        {label}
      </label>
      <input
        id={fieldId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={clsx(CONTROL_CLASSES, className)}
        {...rest}
      />
      {error && (
        <p id={errorId} className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      )}
    </div>
  )
}