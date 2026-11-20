import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import Button from '../../../components/Button'
import Select from '../../../components/Select'
import { CONTROL_CLASSES } from '../../../components/styles'
import TextField from '../../../components/TextField'
import { useToast } from '../../../components/toastContext'
import { CATEGORY_LABEL } from '../../../lib/format'
import { ApiError, getErrorMessage } from '../../../lib/http'
import { ROUTES } from '../../../lib/routes'
import { useAuth } from '../../auth/hooks'
import { useCreateEvent } from '../../events/hooks'
import { CreateEventRequestSchema, EventCategorySchema, type CreateEventRequest } from '../../events/types'
import { useActiveVenues } from '../../venues/hooks'
import { rememberCreatedEvent } from '../createdEvents'

const FIELDS = Object.keys(CreateEventRequestSchema.shape) as (keyof CreateEventRequest)[]

export default function CreateEventForm() {
  const navigate = useNavigate()
  const { session } = useAuth()
  const { showToast } = useToast()
  const createEvent = useCreateEvent()
  const venues = useActiveVenues()

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors, isSubmitting },
  } = useForm<CreateEventRequest>({
    resolver: zodResolver(CreateEventRequestSchema),
    defaultValues: { eventCode: '', name: '', description: '', eventDate: '', minimumAge: 0, venueCode: '' },
  })

  async function onSubmit(data: CreateEventRequest) {
    try {
      const event = await createEvent.mutateAsync(data)
      if (session.status === 'authenticated') {
        rememberCreatedEvent(session.user.username, { eventCode: event.eventCode, name: event.name })
      }
      showToast('success', 'Evento creado')
      navigate(ROUTES.adminEvent(event.eventCode))
    } catch (error) {
      if (error instanceof ApiError && error.status === 403) {
        navigate(ROUTES.forbidden)
        return
      }
      // 400 con details: cada mensaje va a su campo (sección 11)
      if (error instanceof ApiError && error.status === 400) {
        for (const [field, message] of Object.entries(error.details)) {
          if ((FIELDS as string[]).includes(field)) setError(field as keyof CreateEventRequest, { message })
        }
      }
      // 409 y demás: aviso arriba del formulario
      setError('root', { message: getErrorMessage(error) })
    }
  }

  // useWatch (no watch): solo re-renderiza cuando cambia la descripción
  const descriptionLength = useWatch({ control, name: 'description' }).length

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      {errors.root?.message && (
        <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-800 dark:bg-red-950/40 dark:text-red-300">
          {errors.root.message}
        </p>
      )}

      <TextField label="Código del evento" placeholder="CMF-2026" {...register('eventCode')} error={errors.eventCode?.message} />
      <TextField label="Nombre" {...register('name')} error={errors.name?.message} />

      <div className="flex flex-col gap-1">
        <label htmlFor="description" className="text-sm font-medium">
          Descripción
        </label>
        <textarea
          id="description"
          rows={4}
          aria-invalid={errors.description ? true : undefined}
          aria-describedby="description-count"
          className={`${CONTROL_CLASSES} py-2`}
          {...register('description')}
        />
        <p id="description-count" className="text-right text-xs text-slate-500">
          {descriptionLength}/1000
        </p>
        {errors.description && <p className="text-sm text-red-600">{errors.description.message}</p>}
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Select label="Categoría" defaultValue="" {...register('category')} error={errors.category?.message}>
          <option value="" disabled>
            Selecciona…
          </option>
          {EventCategorySchema.options.map((category) => (
            <option key={category} value={category}>
              {CATEGORY_LABEL[category]}
            </option>
          ))}
        </Select>
        <TextField
          label="Edad mínima"
          type="number"
          min={0}
          {...register('minimumAge', { valueAsNumber: true })}
          error={errors.minimumAge?.message}
        />
      </div>

      <TextField label="Fecha y hora" type="datetime-local" {...register('eventDate')} error={errors.eventDate?.message} />

      <Select label="Sede" {...register('venueCode')} error={errors.venueCode?.message} disabled={venues.isPending}>
        <option value="">{venues.isPending ? 'Cargando sedes…' : 'Selecciona…'}</option>
        {venues.data?.map((venue) => (
          <option key={venue.code} value={venue.code}>
            {venue.name} ({venue.code})
          </option>
        ))}
      </Select>

      <div className="flex justify-end gap-3">
        <Link
          to={ROUTES.adminEvents}
          className="flex min-h-11 items-center rounded-lg px-4 text-sm font-semibold text-slate-700 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
        >
          Cancelar
        </Link>
        <Button type="submit" loading={isSubmitting}>
          Crear evento
        </Button>
      </div>
    </form>
  )
}
