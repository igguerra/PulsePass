import clsx from 'clsx'
import type { ComponentProps } from 'react'

export default function Card({ className, ...rest }: ComponentProps<'div'>) {
  return <div className={clsx('rounded-xl bg-white p-4 shadow-sm dark:bg-slate-900', className)} {...rest} />
}