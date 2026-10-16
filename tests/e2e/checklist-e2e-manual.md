# Checklist de ejecución manual — PulsePass E2E

Marcar `[x]` al ejecutar. El resultado detallado va en `registro-ejecucion-manual.md`.
Antes de empezar: ambiente limpio y sembrado, app corriendo con perfil `e2e`,
ambiente `PulsePass-local` seleccionado en Postman.

## 00 Preparación — Isabella

- [ ] Conectividad: `GET /api/venues/active` responde 200
- [ ] Registrar `e2e_admin`, `e2e_staff`, `e2e_inactive` (`POST /api/users`)
- [ ] Ejecutar `fixtures/promote-roles.sql` y verificar roles

<!-- Isabella: casos adicionales de preparación -->

## Flujo A — Descubrimiento público (sin token) — Isabella

<!-- Isabella: casos del Flujo A (PRD 9.1) -->

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

<!-- Isabella: login de A, B, STAFF, ADMIN, inactivo; /me; TC-AUTH-002 a 006 -->

## Flujo C — Gestión del catálogo (ADMIN) — Isabella

<!-- Isabella: casos del Flujo C (PRD 9.3) -->

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

<!-- Isabella: 400 / 404 / 409 de venues, artists, events y auth -->

### Rutas de Valeria

Carpeta `08`. Los 400 y 409 del registro están en la carpeta `03` (Flujo B).

- [ ] E2E-VAL-01 Compra sin userEmail (400)
- [ ] E2E-VAL-02 Compra con type inválido (400)
- [ ] E2E-VAL-03 Compra con JSON mal formado (400)
- [ ] E2E-VAL-04 Ticket inexistente (404)
- [ ] E2E-VAL-05 Cancelar ticket inexistente (404)

## Seguridad — escenarios negativos

### JWT y roles — Isabella

<!-- Isabella: TC-AUTH-007 a 015, TC-SEC-001 a 005, 015, 017 -->

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

- [ ] Las 22 rutas tienen al menos un caso nominal ejecutado (matriz del README)
- [ ] Bitácora sin filas NOT RUN sin justificar
- [ ] Defectos registrados
- [ ] Colección y ambiente exportados **sin** tokens ni contraseñas