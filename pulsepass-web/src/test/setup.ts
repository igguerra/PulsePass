import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { setToken } from '../lib/http'
import { server } from './mocks/server'

// 'error': si un componente llama a un endpoint sin handler, la prueba falla (nada de red real)
beforeAll(() => server.listen({ onUnhandledFrame: 'error' }))
afterEach(() => {
  cleanup()
  server.resetHandlers()
  sessionStorage.clear()
  setToken(null)
})
afterAll(() => server.close())