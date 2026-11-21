import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useLocation } from 'react-router'
import Button from '../components/Button'
import TextField from '../components/TextField'
import { useAuth } from '../features/auth/hooks'
import { LoginSchema, type LoginRequest } from '../features/auth/types'
import { ApiError, getErrorMessage } from '../lib/http'
import { readNavigationState, ROUTES } from '../lib/routes'

export default function Login() {
  const location = useLocation()
  const navState = readNavigationState(location.state)
  const { signIn } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>({ resolver: zodResolver(LoginSchema) })

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      await signIn(values.identifier, values.password)
      // GuestOnlyRoute redirige solo a "from"; aquí no hay que navegar
    } catch (error: unknown) {
      if (error instanceof ApiError && error.status === 401) {
        setFormError('Credenciales inválidas')
        return
      }
      setFormError(getErrorMessage(error))
    }
  })

  return (
    <div className="mx-auto max-w-md rounded-xl bg-white p-6 shadow-sm dark:bg-slate-900">
      <h1 className="text-center text-2xl font-semibold">Iniciar sesión</h1>

      {navState.reason === 'expired' && (
        <p
          role="status"
          className="mt-4 rounded-md bg-sky-100 p-3 text-sm text-sky-800"
        >
          Tu sesión expiró
        </p>
      )}
      {navState.registered && (
        <p
          role="status"
          className="mt-4 rounded-md bg-emerald-100 p-3 text-sm text-emerald-800"
        >
          Cuenta creada
        </p>
      )}

      <form onSubmit={onSubmit} noValidate className="mt-4 flex flex-col gap-4">
        <TextField
          label="Usuario o correo"
          autoComplete="username"
          error={errors.identifier?.message}
          {...register('identifier')}
        />
        <TextField
          label="Contraseña"
          type="password"
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
        />

        {formError && (
          <p role="alert" className="rounded-md bg-red-100 p-3 text-sm text-red-800">
            {formError}
          </p>
        )}

        <Button type="submit" loading={isSubmitting} className="w-full">
          Ingresar
        </Button>
      </form>

      <p className="mt-4 text-center text-sm">
        ¿No tienes cuenta?{' '}
        <Link to={ROUTES.register} className="font-medium text-marca-600">
          Regístrate
        </Link>
      </p>
    </div>
  )
}