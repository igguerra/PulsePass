import Button from '../../../components/Button'
import Select from '../../../components/Select'
import TextField from '../../../components/TextField'
import { CATEGORY_LABEL } from '../../../lib/format'
import { useActiveArtists } from '../../artists/hooks'
import { EventCategorySchema } from '../types'
import type { EventFilters } from '../filters'

interface EventFiltersBarProps {
  filters: EventFilters
  onChange: (name: 'q' | 'categoria' | 'artista', value: string) => void
  onClear: () => void
}

// BarraFiltros de la sección 9.4. No guarda estado: todo vive en la URL.
export default function EventFiltersBar({ filters, onChange, onClear }: EventFiltersBarProps) {
  const { data: artists = [] } = useActiveArtists()
  const hasFilters = filters.q !== '' || filters.category !== null || filters.artist !== ''

  return (
    <form
      role="search"
      className="grid gap-3 sm:grid-cols-2 lg:grid-cols-[2fr_1fr_1fr_auto] lg:items-end"
      onSubmit={(event) => event.preventDefault()}
    >
      <TextField
        label="Buscar"
        type="search"
        placeholder="Buscar por nombre…"
        value={filters.q}
        onChange={(event) => onChange('q', event.target.value)}
      />
      <Select label="Categoría" value={filters.category ?? ''} onChange={(event) => onChange('categoria', event.target.value)}>
        <option value="">Todas</option>
        {EventCategorySchema.options.map((category) => (
          <option key={category} value={category}>
            {CATEGORY_LABEL[category]}
          </option>
        ))}
      </Select>
      <Select label="Artista" value={filters.artist} onChange={(event) => onChange('artista', event.target.value)}>
        <option value="">Todos</option>
        {artists.map((artist) => (
          <option key={artist.id} value={artist.stageName}>
            {artist.stageName}
          </option>
        ))}
      </Select>
      <Button variant="secondary" onClick={onClear} disabled={!hasFilters}>
        Limpiar
      </Button>
    </form>
  )
}
