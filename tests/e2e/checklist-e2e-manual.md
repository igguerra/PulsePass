# Checklist de ejecución manual — PulsePass E2E

Marcar `[x]` al ejecutar. El resultado detallado va en `registro-ejecucion-manual.md`.
Antes de empezar: ambiente limpio y sembrado, app corriendo con perfil `e2e`,
ambiente `PulsePass-local` seleccionado en Postman.

## 00 Preparación — Isabella

- [ ] E2E-00-01 Conectividad: `GET /api/venues/active` (200)
- [ ] E2E-00-02 Registrar ADMIN `e2e_admin` (201)
- [ ] E2E-00-03 Registrar STAFF `e2e_staff` (201)
- [ ] E2E-00-04 Registrar INACTIVO `e2e_inactive` (201)
- [ ] Ejecutar `fixtures/promote-roles.sql` → verificar `e2e_admin | ADMIN` y `e2e_staff | STAFF`

## Flujo A — Descubrimiento público (sin token) — Isabella

- [ ] E2E-VEN-01 Listar venues activos (200, sin VEN-E2E-OFF)
- [ ] E2E-VEN-02 Venue por código (200)
- [ ] E2E-VEN-03 Venue inexistente (404)

## Flujo B — Registro y JWT

### Registro de USER_A y USER_B — Valeria

<!-- Valeria: TC-AUTH-001, registro inválido, duplicados, TC-SEC-016 -->

### Login, /me y tokens — Isabella

<!-- Isabella: login de A, B, STAFF, ADMIN, inactivo; /me; TC-AUTH-002 a 006 -->

## Flujo C — Gestión del catálogo (ADMIN) — Isabella

<!-- Isabella: casos del Flujo C (PRD 9.3) -->

## Flujo D — Compra, propiedad, cancelación y uso — Valeria

<!-- Valeria: casos del Flujo D (PRD 9.4) -->

## Flujo E — Validaciones y errores HTTP

### Rutas de Isabella

<!-- Isabella: 400 / 404 / 409 de venues, artists, events y auth -->

### Rutas de Valeria

<!-- Valeria: 400 / 404 / 409 de users y tickets -->

## Seguridad — escenarios negativos

### JWT y roles — Isabella

<!-- Isabella: TC-AUTH-007 a 015, TC-SEC-001 a 005, 015, 017 -->

### Propiedad (IDOR) — Valeria

<!-- Valeria: TC-SEC-006 a 014 -->

## Cierre

- [ ] Las 22 rutas tienen al menos un caso nominal ejecutado (matriz del README)
- [ ] Bitácora sin filas NOT RUN sin justificar
- [ ] Defectos registrados
- [ ] Colección y ambiente exportados **sin** tokens ni contraseñas