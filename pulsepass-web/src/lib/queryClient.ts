import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

// Sección 14.3: 4xx nunca se reintenta; red/5xx una vez; mutaciones jamás (evita compras dobles)
export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: (failureCount, error) => {
          if (error instanceof ApiError && error.status >= 400 && error.status < 500) return false
          return failureCount < 1
        },
        refetchOnWindowFocus: false,
      },
      mutations: { retry: false },
    },
  })
}

export const queryClient = createQueryClient()