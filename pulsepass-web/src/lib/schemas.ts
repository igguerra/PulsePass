import { z } from 'zod'

// LocalDateTime de Java llega como "2026-12-12T20:00:00" (sin zona horaria)
export const DateTimeSchema = z.iso.datetime({ local: true })

// LocalDate de Java llega como "2008-05-20"
export const DateSchema = z.iso.date()