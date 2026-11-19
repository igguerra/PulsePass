import Loading from './components/Loading'
import type { ComponentType } from 'react'
import { createBrowserRouter, type RouteObject } from 'react-router'
import GuestOnlyRoute from './layouts/GuestOnlyRoute'
import Layout from './layouts/Layout'
import ProtectedRoute from './layouts/ProtectedRoute'
import RoleRoute from './layouts/RoleRoute'
import ErrorPage from './pages/ErrorPage'
import Forbidden from './pages/Forbidden'
import Home from './pages/Home'
import NotFound from './pages/NotFound'

// FE-012: las páginas se descargan solo al visitarlas. Excepciones: Layout, Home
// y las de error (403/404), que ErrorPage necesita tener siempre a mano.
const lazyPage = (load: () => Promise<{ default: ComponentType }>) => async () => ({
  Component: (await load()).default,
})

export const appRoutes: RouteObject[] = [
  {
    path: '/',
    Component: Layout,
    errorElement: <ErrorPage />,
    hydrateFallbackElement: <Loading />,
    children: [
      { index: true, Component: Home },
      { path: 'eventos', lazy: lazyPage(() => import('./pages/EventCatalog')) },
      { path: 'eventos/:eventCode', lazy: lazyPage(() => import('./pages/EventDetail')) },
      { path: 'artistas', lazy: lazyPage(() => import('./pages/ArtistList')) },
      { path: 'artistas/:id', lazy: lazyPage(() => import('./pages/ArtistDetail')) },
      { path: 'sedes', lazy: lazyPage(() => import('./pages/VenueList')) },
      { path: 'sedes/:code', lazy: lazyPage(() => import('./pages/VenueDetail')) },
      {
        Component: GuestOnlyRoute,
        children: [
          { path: 'login', lazy: lazyPage(() => import('./pages/Login')) },
          { path: 'registro', lazy: lazyPage(() => import('./pages/Register')) },
        ],
      },
      {
        Component: ProtectedRoute,
        children: [
          { path: 'cuenta', lazy: lazyPage(() => import('./pages/Account')) },
          {
            element: <RoleRoute roles={['USER']} />,
            children: [
              { path: 'mis-tickets', lazy: lazyPage(() => import('./pages/MyTickets')) },
              { path: 'mis-tickets/:ticketCode', lazy: lazyPage(() => import('./pages/TicketDetail')) },
              {
                path: 'compra/confirmacion/:ticketCode',
                lazy: lazyPage(() => import('./pages/PurchaseConfirmation')),
              },
            ],
          },
          {
            element: <RoleRoute roles={['STAFF', 'ADMIN']} />,
            children: [
              { path: 'staff/validacion', lazy: lazyPage(() => import('./pages/EntryValidation')) },
              { path: 'staff/eventos/:eventCode/tickets', lazy: lazyPage(() => import('./pages/EventPaidTickets')) },
            ],
          },
          {
            element: <RoleRoute roles={['ADMIN']} />,
            children: [
              { path: 'admin/eventos', lazy: lazyPage(() => import('./pages/AdminEvents')) },
              { path: 'admin/eventos/nuevo', lazy: lazyPage(() => import('./pages/AdminCreateEvent')) },
              { path: 'admin/eventos/:eventCode', lazy: lazyPage(() => import('./pages/AdminManageEvent')) },
            ],
          },
        ],
      },
      { path: '403', Component: Forbidden },
      { path: '*', Component: NotFound },
    ],
  },
]

export const router = createBrowserRouter(appRoutes)