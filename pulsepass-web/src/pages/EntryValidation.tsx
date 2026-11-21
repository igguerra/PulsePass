import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import Badge from '../components/Badge'
import Button from '../components/Button'
import Card from '../components/Card'
import Loading from '../components/Loading'
import TextField from '../components/TextField'
import { useMarkTicketUsed, useTicket } from '../features/tickets/hooks'
import {
  EntryValidationFormSchema,
  type EntryValidationForm,
} from '../features/tickets/types'
import { TICKET_STATUS_LABEL, TICKET_TYPE_LABEL } from '../lib/format'
import { ApiError, getErrorMessage } from '../lib/http'

function getSearchErrorMessage(error: unknown): string {
  if (error instanceof ApiError && error.status === 404) return 'Ticket no encontrado'
  if (error instanceof ApiError && error.status === 403) return 'Sin permisos'
  return getErrorMessage(error)
}

export default function EntryValidation() {
  // Código que se está consultando (estado de interfaz, no copia de datos del servidor)
  const [searchedCode, setSearchedCode] = useState('')
  const ticketQuery = useTicket(searchedCode)
  const markUsed = useMarkTicketUsed()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<EntryValidationForm>({ resolver: zodResolver(EntryValidationFormSchema) })

  const onSearch = handleSubmit((values) => {
    markUsed.reset()
    setSearchedCode(values.ticketCode)
  })

  async function handleMarkUsed(ticketCode: string, eventCode: string) {
    try {
      await markUsed.mutateAsync({ ticketCode, eventCode })
      reset() // tras el éxito el campo se limpia
    } catch {
      // El 409 / 404 queda en markUsed.error y se muestra en pantalla
    }
  }

  const ticket = ticketQuery.data

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-4">
      <h1 className="text-2xl font-semibold">Validación de ingreso</h1>

      <form onSubmit={onSearch} noValidate className="flex items-start gap-3">
        <div className="flex-1">
          <TextField
            label="Código de ticket"
            autoFocus
            autoComplete="off"
            error={errors.ticketCode?.message}
            {...register('ticketCode')}
          />
        </div>
        <Button type="submit" className="mt-7">
          Buscar
        </Button>
      </form>

      {searchedCode && ticketQuery.isFetching && !ticket && <Loading lines={1} />}

      {ticketQuery.isError && (
        <p role="alert" className="rounded-lg bg-red-100 p-4 text-base font-semibold text-red-800">
          {getSearchErrorMessage(ticketQuery.error)}
        </p>
      )}

      {ticket && (
        <Card className="p-6">
          <div className="flex items-start justify-between gap-3">
            <h2 className="text-xl font-semibold">{ticket.eventName}</h2>
            <Badge tone={ticket.status}>{TICKET_STATUS_LABEL[ticket.status]}</Badge>
          </div>
          <p className="mt-1 text-slate-600 dark:text-slate-400">
            {TICKET_TYPE_LABEL[ticket.type]} ·{' '}
            <span className="font-mono">{ticket.ticketCode}</span>
          </p>
          <Button
            className="mt-4 w-full"
            loading={markUsed.isPending}
            onClick={() => handleMarkUsed(ticket.ticketCode, ticket.eventCode)}
          >
            Marcar como usado
          </Button>
        </Card>
      )}

      {markUsed.isSuccess && (
        <p
          role="status"
          className="rounded-lg bg-emerald-100 p-4 text-base font-semibold text-emerald-800"
        >
          Ingreso registrado.
        </p>
      )}
      {markUsed.isError && (
        <p role="alert" className="rounded-lg bg-amber-100 p-4 text-base font-semibold text-amber-800">
          {getErrorMessage(markUsed.error)}
        </p>
      )}
    </div>
  )
}