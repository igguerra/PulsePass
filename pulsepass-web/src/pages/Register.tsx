import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from 'react-router'
import Button from '../components/Button'
import TextField from '../components/TextField'
import { applyFieldErrors } from '../features/users/form-errors'
import { useRegisterUser } from '../features/users/hooks'
import { RegisterUserRequestSchema, type RegisterUserRequest } from '../features/users/types'
import { ApiError, getErrorMessage } from '../lib/http'
import { ROUTES } from '../lib/routes'

export default function Register() {
  const navigate = useNavigate()
  const registerUser = useRegisterUser()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegisterUserRequest>({ resolver: zodResolver(RegisterUserRequestSchema) })

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      await registerUser.mutateAsync(values)
      navigate(ROUTES.login, { state: { registered: true } })
    } catch (error: unknown) {
      if (error instanceof ApiError && error.status === 409) {
        setFormError('No pudimos crear la cuenta con esos datos')
        return
      }
      if (applyFieldErrors(error, setError)) return
      setFormError(getErrorMessage(error))
    }
  })

  return (
    <div className="mx-auto max-w-lg rounded-xl bg-white p-6 shadow-sm dark:bg-slate-900">
      <h1 className="text-center text-2xl font-semibold">Crea tu cuenta</h1>

      <form onSubmit={onSubmit} noValidate className="mt-4 flex flex-col gap-4">
        <TextField
          label="Nombre de usuario"
          autoComplete="username"
          error={errors.username?.message}
          {...register('username')}
        />
        <TextField
          label="Correo electrónico"
          type="email"
          autoComplete="email"
          error={errors.email?.message}
          {...register('email')}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <TextField
            label="Nombres"
            autoComplete="given-name"
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <TextField
            label="Apellidos"
            autoComplete="family-name"
            error={errors.lastName?.message}
            {...register('lastName')}
          />
        </div>
        <TextField
          label="Fecha de nacimiento"
          type="date"
          error={errors.birthDate?.message}
          {...register('birthDate')}
        />
        <TextField
          label="Contraseña (mín. 12 caracteres)"
          type="password"
          autoComplete="new-password"
          error={errors.password?.message}
          {...register('password')}
        />

        {formError && (
          <p role="alert" className="rounded-md bg-red-100 p-3 text-sm text-red-800">
            {formError}
          </p>
        )}

        <Button type="submit" loading={registerUser.isPending} className="w-full">
          Crear cuenta
        </Button>
      </form>

      <p className="mt-4 text-center text-sm">
        ¿Ya tienes cuenta?{' '}
        <Link to={ROUTES.login} className="font-medium text-marca-600">
          Inicia sesión
        </Link>
      </p>
    </div>
  )
}