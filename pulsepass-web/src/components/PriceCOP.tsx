import { formatCOP } from '../lib/format'

export default function PriceCOP({ value }: { value: number }) {
  return <span className="tabular-nums">{formatCOP(value)}</span>
}