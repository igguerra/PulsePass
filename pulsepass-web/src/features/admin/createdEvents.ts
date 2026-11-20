import { z } from 'zod'

// GAP-01: la API no tiene un listado completo de eventos. Los que crea el ADMIN se
// recuerdan en sessionStorage (se pierden al cerrar la pestaña: limitación documentada, 9.16).

const CreatedEventSchema = z.object({ eventCode: z.string(), name: z.string() })
export type CreatedEvent = z.infer<typeof CreatedEventSchema>

const keyFor = (username: string) => `pulsepass.admin.created.${username}`

export function readCreatedEvents(username: string): CreatedEvent[] {
  try {
    const result = z.array(CreatedEventSchema).safeParse(JSON.parse(sessionStorage.getItem(keyFor(username)) ?? '[]'))
    return result.success ? result.data : []
  } catch {
    return []
  }
}

export function rememberCreatedEvent(username: string, event: CreatedEvent): void {
  const others = readCreatedEvents(username).filter((item) => item.eventCode !== event.eventCode)
  sessionStorage.setItem(keyFor(username), JSON.stringify([event, ...others]))
}
