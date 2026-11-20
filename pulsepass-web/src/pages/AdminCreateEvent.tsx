import { Link } from 'react-router'
import Card from '../components/Card'
import CreateEventForm from '../features/admin/components/CreateEventForm'
import { ROUTES } from '../lib/routes'

export default function AdminCreateEvent() {
  return (
    <section className="mx-auto flex max-w-2xl flex-col gap-6">
      <Link to={ROUTES.adminEvents} className="text-sm text-marca-600 hover:underline">
        ← Eventos
      </Link>
      <h1 className="text-2xl font-bold">Nuevo evento</h1>
      <Card>
        <CreateEventForm />
      </Card>
    </section>
  )
}
