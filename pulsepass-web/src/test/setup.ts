import '@testing-library/jest-dom/vitest'
import { cleanup, configure } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { setToken } from '../lib/http'
import { server } from './mocks/server'

// Las páginas se cargan con lazy(): en la primera prueba de cada archivo la importación
// puede tardar más de 1 s en equipos lentos. Se da hasta 5 s a los findBy… antes de fallar.
configure({ asyncUtilTimeout: 5000 })

// 'error': si un componente llama a un endpoint sin handler, la prueba falla (nada de red real)
beforeAll(() => server.listen({ onUnhandledFrame: 'error' }))
afterEach(() => {
  cleanup()
  server.resetHandlers()
  sessionStorage.clear()
  setToken(null)
})
afterAll(() => server.close())