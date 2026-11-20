import { EventCategorySchema, type EventCategory, type EventSummaryResponse } from './types'

export interface EventFilters {
  q: string
  category: EventCategory | null
  artist: string
}

// Lee los filtros desde la URL. Lo que no sea válido se ignora (FE-002).
export function readEventFilters(params: URLSearchParams): EventFilters {
  const category = EventCategorySchema.safeParse(params.get('categoria'))
  return {
    q: params.get('q') ?? '',
    category: category.success ? category.data : null,
    artist: params.get('artista') ?? '',
  }
}

// "Música" y "musica" deben coincidir: sin tildes y en minúsculas
function normalize(text: string): string {
  return text.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().trim()
}

// Texto y categoría se filtran en el cliente (sección 9.4)
export function filterEvents(events: EventSummaryResponse[], filters: EventFilters): EventSummaryResponse[] {
  const query = normalize(filters.q)
  return events.filter(
    (event) =>
      (query === '' || normalize(event.name).includes(query)) &&
      (filters.category === null || event.category === filters.category),
  )
}
