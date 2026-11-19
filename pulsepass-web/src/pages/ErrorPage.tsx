import { isRouteErrorResponse, useRouteError } from 'react-router'
import Button from '../components/Button'
import MessageScreen from '../components/MessageScreen'
import { ApiError, getErrorMessage } from '../lib/http'
import Forbidden from './Forbidden'
import NotFound from './NotFound'

// errorElement del layout raíz: nunca muestra stack traces ni detalles internos
export default function ErrorPage() {
  const error = useRouteError()
  const status = isRouteErrorResponse(error) ? error.status : error instanceof ApiError ? error.status : null

  if (status === 404) return <NotFound />
  if (status === 403) return <Forbidden />

  return (
    <MessageScreen
      title="¡Ups! Algo salió mal."
      description={getErrorMessage(error)}
      actions={
        <Button variant="secondary" onClick={() => window.location.reload()}>
          Reintentar
        </Button>
      }
    />
  )
}