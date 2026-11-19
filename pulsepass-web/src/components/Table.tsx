import type { Key, ReactNode } from 'react'

export interface Column<T> {
  header: string
  cell: (row: T) => ReactNode
}

interface TableProps<T> {
  columns: Column<T>[]
  rows: T[]
  getKey: (row: T) => Key
  caption?: string
}

// Componente genérico: T se infiere de "rows"
export default function Table<T>({ columns, rows, getKey, caption }: TableProps<T>) {
  return (
    <div className="overflow-x-auto rounded-xl bg-white shadow-sm dark:bg-slate-900">
      <table className="w-full text-left text-sm">
        {caption && <caption className="sr-only">{caption}</caption>}
        <thead className="border-b border-slate-200 text-slate-600 dark:border-slate-800 dark:text-slate-400">
          <tr>
            {columns.map((column) => (
              <th key={column.header} scope="col" className="px-4 py-3 font-semibold">
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={getKey(row)} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              {columns.map((column) => (
                <td key={column.header} className="px-4 py-3">
                  {column.cell(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}