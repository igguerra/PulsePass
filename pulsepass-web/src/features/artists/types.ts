import { z } from 'zod'

// Espejo de ArtistResponse (backend: dto/response/ArtistResponse.java)
export const ArtistResponseSchema = z.object({
  id: z.number().int(),
  stageName: z.string(),
  country: z.string().nullable(),
  genre: z.string().nullable(),
  active: z.boolean(),
})
export type ArtistResponse = z.infer<typeof ArtistResponseSchema>