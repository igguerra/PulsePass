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

Colección **PulsePass E2E - Valeria**, carpeta `03`. Escribir antes `userAPassword`, `userBPassword` y `minorPassword` como Current value.

- [ ] TC-AUTH-001 Registrar USER_A (201)
- [ ] TC-BIZ-006 Registrar USER_B (201)
- [ ] E2E-USR-01 Registro con email mal formado (400)
- [ ] E2E-USR-02 Registro con password de 11 caracteres (400)
- [ ] E2E-USR-03 Registro con firstName vacío (400)
- [ ] TC-BIZ-006 Duplicado: mismo username (409)
- [ ] TC-BIZ-006 Duplicado: email de A en mayúsculas (409)
- [ ] TC-SEC-016 Registro con role ADMIN inyectado (201) → verificar en la base que el rol es solo USER
- [ ] E2E-USR-04 Registrar usuario menor (2012) (201)
- [ ] E2E-USR-05 Login usuario menor (200) → copiar accessToken a `minorToken`

El login de USER_A y USER_B (sección de Isabella) va antes de las carpetas 04 y 06.

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

Carpeta `06`. En este orden exacto: T1 se cancela y T2 se usa; nunca reutilizar T1 para use.

**06a — propiedad, cancelación y uso**

- [ ] TC-BIZ-007 USER_A compra T1 en EVT-E2E-PUB (201) → copiar ticketCode a `ticketCancelableCode`
- [ ] TC-BIZ-008 USER_A lee T1 (200)
- [ ] TC-SEC-009 USER_B lee T1 de A (403)
- [ ] TC-SEC-011 USER_B cancela T1 de A (403)
- [ ] TC-SEC-011 verificación: T1 sigue PAID (200)
- [ ] TC-BIZ-010 USER_A cancela T1 (200)
- [ ] TC-BIZ-010 verificación: T1 CANCELLED (200)
- [ ] TC-BIZ-011 Cancelar T1 otra vez (409)
- [ ] TC-BIZ-007 USER_A compra T2 (VIP) (201) → copiar ticketCode a `ticketUsableCode`
- [ ] TC-BIZ-009 STAFF lista pagados de EVT-E2E-PUB (200)
- [ ] TC-SEC-013 USER_A lista pagados (403)
- [ ] TC-SEC-012 USER_B usa T2 de A (403)
- [ ] TC-SEC-012 verificación: T2 sigue PAID (200)
- [ ] TC-SEC-014 STAFF marca T2 como usado (200)
- [ ] TC-BIZ-013 STAFF usa T2 otra vez (409)
- [ ] TC-BIZ-012 verificación: T2 USED (200)
- [ ] E2E-TKT-01 USER_A lista sus tickets (200)
- [ ] TC-SEC-010 USER_B lista tickets de A (403)
- [ ] E2E-TKT-02 ADMIN lista tickets de A (200)

**06b — compras rechazadas** (TC-SEC-008 primero: USER_B no debe tener tickets al verificarlo)

- [ ] TC-SEC-008 USER_A compra con el email de B (403)
- [ ] TC-SEC-008 verificación: B no tiene tickets (200)
- [ ] TC-BIZ-014 Compra en evento DRAFT (409)
- [ ] TC-BIZ-015 Compra en evento inexistente (404)
- [ ] TC-BIZ-014 Aforo: USER_A compra el único cupo (201)
- [ ] TC-BIZ-014 Aforo: USER_B intenta comprar (409)
- [ ] TC-BIZ-014 Aforo verificación: un solo pagado (200)
- [ ] TC-BIZ-014 Edad: menor compra en EVT-E2E-ADULT (409)
- [ ] TC-BIZ-014 Edad verificación: menor sin tickets (200)
- [ ] E2E-TKT-03 Compra sin token (401)
- [ ] E2E-TKT-04 ADMIN intenta comprar (403)

OJO: tras la compra de aforo, EVT-E2E-FULL queda SOLD_OUT y sale de `/api/events/published`.

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

Carpeta `08`. Los 400 y 409 del registro están en la carpeta `03` (Flujo B).

- [ ] E2E-VAL-01 Compra sin userEmail (400)
- [ ] E2E-VAL-02 Compra con type inválido (400)
- [ ] E2E-VAL-03 Compra con JSON mal formado (400)
- [ ] E2E-VAL-04 Ticket inexistente (404)
- [ ] E2E-VAL-05 Cancelar ticket inexistente (404)

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

Carpeta `04`. TC-SEC-008 a 014 se ejecutan dentro del Flujo D, porque dependen de su orden.

- [ ] E2E-USR-06 USER_A consulta su email (200)
- [ ] E2E-USR-07 USER_A consulta su username (200)
- [ ] TC-SEC-006 USER_B consulta email de A (403)
- [ ] TC-SEC-007 USER_B consulta username de A (403)
- [ ] E2E-USR-08 ADMIN consulta email de A (200)
- [ ] E2E-USR-09 ADMIN consulta username de A (200)
- [ ] E2E-USR-10 Sin token: by-email (401)
- [ ] E2E-USR-11 Sin token: by-username (401)
- [ ] E2E-USR-12 Email inexistente (404)

## Cierre

- [ ] Las 22 rutas tienen al menos un caso nominal ejecutado (matriz del README) — rutas 1 a 13 completas; faltan 14 a 22 (Valeria)
- [ ] Bitácora sin filas NOT RUN sin justificar — tablas de Isabella completas
- [x] Defectos registrados (DEF-01 a DEF-06, rutas de Isabella)
- [ ] Colección y ambiente exportados **sin** tokens ni contraseñas