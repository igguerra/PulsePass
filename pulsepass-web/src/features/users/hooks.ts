import { useMutation, useQuery } from '@tanstack/react-query'
import { getUserByEmail, getUserByUsername, registerUser } from './api'

export function useRegisterUser() {
  return useMutation({ mutationFn: registerUser })
}

export function useUserByEmail(email: string | undefined) {
  return useQuery({
    queryKey: ['usuario', email],
    queryFn: () => getUserByEmail(email ?? ''),
    enabled: Boolean(email),
    staleTime: 60_000,
  })
}

export function useUserByUsername(username: string | undefined) {
  return useQuery({
    queryKey: ['usuario', 'username', username],
    queryFn: () => getUserByUsername(username ?? ''),
    enabled: Boolean(username),
    staleTime: 60_000,
  })
}