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

- [ ] E2E-ART-01 Listar artistas activos (200) → copiar artistId
- [ ] E2E-ART-02 Artista por ID (200)
- [ ] E2E-ART-03 Artista por nombre (200)
- [ ] E2E-ART-04 Nombre sin distinguir mayúsculas (200)
- [ ] E2E-ART-05 Artista inexistente (404)

- [ ] E2E-EVT-01 Eventos publicados, anónimo (200, sin borradores)
- [ ] E2E-EVT-02 Evento publicado por código, anónimo (200)
- [ ] TC-SEC-004 Borrador por código, anónimo (404 sin datos)
- [ ] TC-SEC-005 Eventos por artista, anónimo (solo EVT-E2E-PUB)
- [ ] TC-SEC-015 Borrador como ADMIN (200 DRAFT)
- [ ] E2E-EVT-03 Borrador como STAFF (200 DRAFT)
- [ ] E2E-EVT-04 Borrador como USER_A (404)
- [ ] E2E-EVT-05 Eventos por artista como ADMIN (PUB + DRAFT)

## Flujo B — Registro y JWT

### Registro de USER_A y USER_B — Valeria

<!-- Valeria: TC-AUTH-001, registro inválido, duplicados, TC-SEC-016 -->

### Login, /me y tokens — Isabella

- [ ] E2E-AUTH-01 Login ADMIN (200) → copiar adminToken
- [ ] E2E-AUTH-02 Login STAFF (200) → copiar staffToken
- [ ] E2E-AUTH-03 Login INACTIVO (200) → copiar inactiveToken (no usar aún)
- [ ] TC-AUTH-002 Login USER_A con username (200) → copiar userAToken
- [ ] TC-AUTH-003 Login USER_A con email (200)
- [ ] E2E-AUTH-04 Login USER_B (200) → copiar userBToken
- [ ] TC-AUTH-004 Contraseña incorrecta (401 genérico)
- [ ] TC-AUTH-005 Usuario inexistente (401, mismo mensaje que 004)
- [ ] E2E-AUTH-ME-01 /me ADMIN (roles ["ADMIN"])
- [ ] E2E-AUTH-ME-02 /me STAFF (roles ["STAFF"])
- [ ] TC-AUTH-006 /me USER_A (identidad de A, roles ["USER"])
- [ ] E2E-AUTH-ME-03 /me USER_B (roles ["USER"])

## Flujo C — Gestión del catálogo (ADMIN) — Isabella

- [ ] Login ADMIN fresco (E2E-AUTH-01)
- [ ] TC-BIZ-003 Crear evento newEventCode (201 DRAFT)
- [ ] E2E-EVT-06 Leer evento nuevo como ADMIN (200 DRAFT) → reenviar E2E-EVT-01: NO aparece
- [ ] TC-BIZ-005 Asociar artista (200)
- [ ] E2E-EVT-07 Asociar el mismo artista (409)
- [ ] TC-BIZ-004 Publicar (200 PUBLISHED)
- [ ] E2E-EVT-08 Publicar otra vez (409)
- [ ] E2E-EVT-09 Leer evento nuevo, anónimo (200) → reenviar E2E-EVT-01 y TC-SEC-005: SÍ aparece

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