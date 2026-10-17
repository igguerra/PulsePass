# Checklist de ejecución manual — PulsePass E2E

Marcar `[x]` al ejecutar. El resultado detallado va en `registro-ejecucion-manual.md`.
Antes de empezar: ambiente limpio y sembrado, app corriendo con perfil `e2e`,
ambiente `PulsePass-local` seleccionado en Postman.

## 00 Preparación — Isabella

- [x] E2E-00-01 Conectividad: `GET /api/venues/active` (200)
- [x] E2E-00-02 Registrar ADMIN `e2e_admin` (201)
- [x] E2E-00-03 Registrar STAFF `e2e_staff` (201)
- [x] E2E-00-04 Registrar INACTIVO `e2e_inactive` (201)
- [x] Ejecutar `fixtures/promote-roles.sql` → verificar `e2e_admin | ADMIN` y `e2e_staff | STAFF`

## Flujo A — Descubrimiento público (sin token) — Isabella

- [x] E2E-VEN-01 Listar venues activos (200, sin VEN-E2E-OFF)
- [x] E2E-VEN-02 Venue por código (200)
- [x] E2E-VEN-03 Venue inexistente (404)

- [x] E2E-ART-01 Listar artistas activos (200) → copiar artistId
- [x] E2E-ART-02 Artista por ID (200)
- [x] E2E-ART-03 Artista por nombre (200)
- [x] E2E-ART-04 Nombre sin distinguir mayúsculas (200)
- [x] E2E-ART-05 Artista inexistente (404)

- [x] E2E-EVT-01 Eventos publicados, anónimo (200, sin borradores)
- [x] E2E-EVT-02 Evento publicado por código, anónimo (200)
- [x] TC-SEC-004 Borrador por código, anónimo (404 sin datos)
- [x] TC-SEC-005 Eventos por artista, anónimo (solo EVT-E2E-PUB)
- [x] TC-SEC-015 Borrador como ADMIN (200 DRAFT)
- [x] E2E-EVT-03 Borrador como STAFF (200 DRAFT)
- [x] E2E-EVT-04 Borrador como USER_A (404)
- [x] E2E-EVT-05 Eventos por artista como ADMIN (PUB + DRAFT)

## Flujo B — Registro y JWT

### Registro de USER_A y USER_B — Valeria

<!-- Valeria: TC-AUTH-001, registro inválido, duplicados, TC-SEC-016 -->

### Login, /me y tokens — Isabella

- [x] E2E-AUTH-01 Login ADMIN (200) → copiar adminToken
- [x] E2E-AUTH-02 Login STAFF (200) → copiar staffToken
- [x] E2E-AUTH-03 Login INACTIVO (200) → copiar inactiveToken (no usar aún)
- [x] TC-AUTH-002 Login USER_A con username (200) → copiar userAToken
- [x] TC-AUTH-003 Login USER_A con email (200)
- [x] E2E-AUTH-04 Login USER_B (200) → copiar userBToken
- [x] TC-AUTH-004 Contraseña incorrecta (401 genérico)
- [x] TC-AUTH-005 Usuario inexistente (401, mismo mensaje que 004)
- [x] E2E-AUTH-ME-01 /me ADMIN (roles ["ADMIN"])
- [x] E2E-AUTH-ME-02 /me STAFF (roles ["STAFF"])
- [x] TC-AUTH-006 /me USER_A (identidad de A, roles ["USER"])
- [x] E2E-AUTH-ME-03 /me USER_B (roles ["USER"])

## Flujo C — Gestión del catálogo (ADMIN) — Isabella

- [x] Login ADMIN fresco (E2E-AUTH-01)
- [x] TC-BIZ-003 Crear evento newEventCode (201 DRAFT)
- [x] E2E-EVT-06 Leer evento nuevo como ADMIN (200 DRAFT) → reenviar E2E-EVT-01: NO aparece
- [x] TC-BIZ-005 Asociar artista (200)
- [x] E2E-EVT-07 Asociar el mismo artista (409)
- [x] TC-BIZ-004 Publicar (200 PUBLISHED)
- [x] E2E-EVT-08 Publicar otra vez (409)
- [x] E2E-EVT-09 Leer evento nuevo, anónimo (200) → reenviar E2E-EVT-01 y TC-SEC-005: SÍ aparece

## Flujo D — Compra, propiedad, cancelación y uso — Valeria

<!-- Valeria: casos del Flujo D (PRD 9.4) -->

## Flujo E — Validaciones y errores HTTP

### Rutas de Isabella

**400 — datos inválidos**
- [x] VAL-EVT-01 Body vacío (400, 6 campos en details)
- [x] VAL-EVT-02 minimumAge negativo (400)
- [x] VAL-EVT-03 Categoría inexistente (400 Malformed JSON)
- [x] VAL-EVT-04 Fecha mal formateada (400 Malformed JSON)
- [x] VAL-EVT-05 JSON roto (400 Malformed JSON)
- [x] VAL-AUTH-01 Login con campos vacíos (400)

**404 — recursos ausentes**
- [x] VAL-EVT-06 Evento inexistente (404)
- [x] VAL-EVT-07 Publicar inexistente (404)
- [x] VAL-EVT-08 Artista a evento inexistente (404)
- [x] VAL-EVT-09 Artista inexistente a evento (404)
- [x] VAL-EVT-10 Venue inexistente al crear (404)
- [x] VAL-ART-01 Stage name inexistente (404)
- [x] VAL-EVT-11 Eventos de artista inexistente (200 [])

**409 — reglas de negocio**
- [x] VAL-EVT-12 Código duplicado (409)
- [x] VAL-EVT-13 Venue inactivo (409)
- [x] VAL-EVT-14 Fecha pasada (409)
- [x] E2E-SEC-02 reenviado: EVT-DENY-01 sigue sin existir (404)

**Posibles 500 (registrar defecto si fallan)**
- [x] VAL-HTTP-01 /api/artists/abc (esperado 400) → 500, DEF-02
- [x] VAL-HTTP-02 by-stage-name sin parámetro (esperado 400) → 500, DEF-03
- [x] VAL-HTTP-03 by-artist sin parámetro (esperado 400) → 500, DEF-04
- [x] VAL-HTTP-04 PATCH no soportado (esperado 405) → 500, DEF-01
- [x] VAL-HTTP-05 Content-Type text/plain (esperado 415) → 500, DEF-05
- [x] VAL-HTTP-06 Ruta inexistente con token (esperado 404) → 500, DEF-06

### Rutas de Valeria

<!-- Valeria: 400 / 404 / 409 de users y tickets -->

## Seguridad — escenarios negativos

### JWT y roles — Isabella

**Token ausente o inválido**
- [x] TC-AUTH-007 /me sin header (401)
- [x] TC-AUTH-008 Bearer inventado (401)
- [x] TC-AUTH-009 JWT alterado (401) + token original da 200
- [x] TC-AUTH-012 Token en query string (401)
- [x] TC-AUTH-015 Token inválido en ruta pública (401)

**Roles insuficientes en catálogo**
- [x] TC-SEC-001a Crear evento anónimo (401)
- [x] TC-SEC-001b / TC-AUTH-014 Crear evento USER_A (403)
- [x] E2E-SEC-01 Crear evento STAFF (403)
- [x] E2E-SEC-02 Verificar que EVT-DENY-01 no existe (404)
- [x] TC-SEC-002a/b Publicar con USER_A y STAFF (403)
- [x] E2E-SEC-03 EVT-E2E-TOPUB sigue en DRAFT
- [x] TC-SEC-003 / E2E-SEC-04 Asociar artista USER_A y STAFF (403)
- [x] E2E-SEC-05 EVT-E2E-TOPUB sin artistas

**Usuario inactivo**
- [x] E2E-SEC-07 /me de e2e_inactive con el token de E2E-AUTH-03 (200)
- [x] deactivate-user.sql ejecutado
- [x] TC-AUTH-013 /me con token previo (401)
- [x] E2E-SEC-08 Login inactivo (401)

**Revocación de rol**
- [x] E2E-SEC-09 Pagados como STAFF (200)
- [x] revoke-staff.sql ejecutado
- [x] TC-SEC-017 Mismo token (403) + E2E-SEC-10 /me roles []
- [x] promote-roles.sql ejecutado + E2E-SEC-11 (200)

**Otra clave / otro issuer / expiración**
- [x] TC-AUTH-011a Firmado con otra clave (401)
- [x] TC-AUTH-011b Issuer distinto (401)
- [x] App reiniciada sin variables temporales; login ADMIN normal (200)
- [x] TC-AUTH-010 Token vencido, más de 16 min (401)

### Propiedad (IDOR) — Valeria

<!-- Valeria: TC-SEC-006 a 014 -->

## Cierre

- [ ] Las 22 rutas tienen al menos un caso nominal ejecutado (matriz del README) — rutas 1 a 13 completas; faltan 14 a 22 (Valeria)
- [ ] Bitácora sin filas NOT RUN sin justificar — tablas de Isabella completas
- [x] Defectos registrados (DEF-01 a DEF-06, rutas de Isabella)
- [ ] Colección y ambiente exportados **sin** tokens ni contraseñas