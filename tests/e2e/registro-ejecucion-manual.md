# Registro de ejecución manual — PulsePass E2E

## Datos de la corrida

| Campo | Valor |
|---|---|
| Fecha | |
| runId | |
| Ejecutado por | |
| Commit del backend | |
| Base de datos | `pulsepass_e2e` (limpia + `seed-e2e.sql`) |

Resultados posibles: `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`.
No incluir JWT, contraseñas ni datos personales reales.

## Venues, Artists y Events — Isabella

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|

## Auth: login, /me y JWT — Isabella

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|

## Users — Valeria

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| TC-AUTH-001 Registrar USER_A | anónimo | POST `/api/users` | 201 | | | NOT RUN | |
| TC-BIZ-006 Registrar USER_B | anónimo | POST `/api/users` | 201 | | | NOT RUN | |
| E2E-USR-01 Registro con email mal formado | anónimo | POST `/api/users` | 400 | | | NOT RUN | |
| E2E-USR-02 Registro con password de 11 caracteres | anónimo | POST `/api/users` | 400 | | | NOT RUN | |
| E2E-USR-03 Registro con firstName vacío | anónimo | POST `/api/users` | 400 | | | NOT RUN | |
| TC-BIZ-006 Duplicado: mismo username | anónimo | POST `/api/users` | 409 | | | NOT RUN | |
| TC-BIZ-006 Duplicado: email de A en mayúsculas | anónimo | POST `/api/users` | 409 | | | NOT RUN | |
| TC-SEC-016 Registro con role ADMIN inyectado | anónimo | POST `/api/users` | 201 | | | NOT RUN | |
| E2E-USR-04 Registrar usuario menor (2012) | anónimo | POST `/api/users` | 201 | | | NOT RUN | |
| E2E-USR-05 Login usuario menor | anónimo | POST `/api/auth/login` | 200 | | | NOT RUN | |
| E2E-USR-06 USER_A consulta su email | USER_A | GET `/api/users/by-email?email=…` | 200 | | | NOT RUN | |
| E2E-USR-07 USER_A consulta su username | USER_A | GET `/api/users/by-username?username=…` | 200 | | | NOT RUN | |
| TC-SEC-006 USER_B consulta email de A | USER_B | GET `/api/users/by-email?email=…` | 403 | | | NOT RUN | |
| TC-SEC-007 USER_B consulta username de A | USER_B | GET `/api/users/by-username?username=…` | 403 | | | NOT RUN | |
| E2E-USR-08 ADMIN consulta email de A | ADMIN | GET `/api/users/by-email?email=…` | 200 | | | NOT RUN | |
| E2E-USR-09 ADMIN consulta username de A | ADMIN | GET `/api/users/by-username?username=…` | 200 | | | NOT RUN | |
| E2E-USR-10 Sin token: by-email | anónimo | GET `/api/users/by-email?email=…` | 401 | | | NOT RUN | |
| E2E-USR-11 Sin token: by-username | anónimo | GET `/api/users/by-username?username=…` | 401 | | | NOT RUN | |
| E2E-USR-12 Email inexistente | USER_B | GET `/api/users/by-email?email=…` | 404 | | | NOT RUN | |

## Tickets — Valeria

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-007 USER_A compra T1 en EVT-E2E-PUB | USER_A | POST `/api/tickets` | 201 | | | NOT RUN | |
| TC-BIZ-008 USER_A lee T1 | USER_A | GET `/api/tickets/{T1}` | 200 | | | NOT RUN | |
| TC-SEC-009 USER_B lee T1 de A | USER_B | GET `/api/tickets/{T1}` | 403 | | | NOT RUN | |
| TC-SEC-011 USER_B cancela T1 de A | USER_B | PATCH `/api/tickets/{T1}/cancel` | 403 | | | NOT RUN | |
| TC-SEC-011 verificación: T1 sigue PAID | USER_A | GET `/api/tickets/{T1}` | 200 | | | NOT RUN | |
| TC-BIZ-010 USER_A cancela T1 | USER_A | PATCH `/api/tickets/{T1}/cancel` | 200 | | | NOT RUN | |
| TC-BIZ-010 verificación: T1 CANCELLED | USER_A | GET `/api/tickets/{T1}` | 200 | | | NOT RUN | |
| TC-BIZ-011 Cancelar T1 otra vez | USER_A | PATCH `/api/tickets/{T1}/cancel` | 409 | | | NOT RUN | |
| TC-BIZ-007 USER_A compra T2 (VIP) | USER_A | POST `/api/tickets` | 201 | | | NOT RUN | |
| TC-BIZ-009 STAFF lista pagados de EVT-E2E-PUB | STAFF | GET `/api/events/EVT-E2E-PUB/tickets/paid` | 200 | | | NOT RUN | |
| TC-SEC-013 USER_A lista pagados | USER_A | GET `/api/events/EVT-E2E-PUB/tickets/paid` | 403 | | | NOT RUN | |
| TC-SEC-012 USER_B usa T2 de A | USER_B | PATCH `/api/tickets/{T2}/use` | 403 | | | NOT RUN | |
| TC-SEC-012 verificación: T2 sigue PAID | USER_A | GET `/api/tickets/{T2}` | 200 | | | NOT RUN | |
| TC-SEC-014 STAFF marca T2 como usado | STAFF | PATCH `/api/tickets/{T2}/use` | 200 | | | NOT RUN | |
| TC-BIZ-013 STAFF usa T2 otra vez | STAFF | PATCH `/api/tickets/{T2}/use` | 409 | | | NOT RUN | |
| TC-BIZ-012 verificación: T2 USED | USER_A | GET `/api/tickets/{T2}` | 200 | | | NOT RUN | |
| E2E-TKT-01 USER_A lista sus tickets | USER_A | GET `/api/tickets/by-user?email=…` | 200 | | | NOT RUN | |
| TC-SEC-010 USER_B lista tickets de A | USER_B | GET `/api/tickets/by-user?email=…` | 403 | | | NOT RUN | |
| E2E-TKT-02 ADMIN lista tickets de A | ADMIN | GET `/api/tickets/by-user?email=…` | 200 | | | NOT RUN | |
| TC-SEC-008 USER_A compra con el email de B | USER_A | POST `/api/tickets` | 403 | | | NOT RUN | |
| TC-SEC-008 verificación: B no tiene tickets | USER_B | GET `/api/tickets/by-user?email=…` | 200 | | | NOT RUN | |
| TC-BIZ-014 Compra en evento DRAFT | USER_A | POST `/api/tickets` | 409 | | | NOT RUN | |
| TC-BIZ-015 Compra en evento inexistente | USER_A | POST `/api/tickets` | 404 | | | NOT RUN | |
| TC-BIZ-014 Aforo: USER_A compra el único cupo | USER_A | POST `/api/tickets` | 201 | | | NOT RUN | |
| TC-BIZ-014 Aforo: USER_B intenta comprar | USER_B | POST `/api/tickets` | 409 | | | NOT RUN | |
| TC-BIZ-014 Aforo verificación: un solo pagado | STAFF | GET `/api/events/EVT-E2E-FULL/tickets/paid` | 200 | | | NOT RUN | |
| TC-BIZ-014 Edad: menor compra en EVT-E2E-ADULT | MENOR | POST `/api/tickets` | 409 | | | NOT RUN | |
| TC-BIZ-014 Edad verificación: menor sin tickets | MENOR | GET `/api/tickets/by-user?email=…` | 200 | | | NOT RUN | |
| E2E-TKT-03 Compra sin token | anónimo | POST `/api/tickets` | 401 | | | NOT RUN | |
| E2E-TKT-04 ADMIN intenta comprar | ADMIN | POST `/api/tickets` | 403 | | | NOT RUN | |
| E2E-VAL-01 Compra sin userEmail | USER_A | POST `/api/tickets` | 400 | | | NOT RUN | |
| E2E-VAL-02 Compra con type inválido | USER_A | POST `/api/tickets` | 400 | | | NOT RUN | |
| E2E-VAL-03 Compra con JSON mal formado | USER_A | POST `/api/tickets` | 400 | | | NOT RUN | |
| E2E-VAL-04 Ticket inexistente | USER_A | GET `/api/tickets/TKT-NO-EXISTE` | 404 | | | NOT RUN | |
| E2E-VAL-05 Cancelar ticket inexistente | USER_A | PATCH `/api/tickets/TKT-NO-EXISTE/cancel` | 404 | | | NOT RUN | |

## Consolidado

| Módulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| Venues / Artists / Events | | | | |
| Auth / JWT | | | | |
| Users | | | | |
| Tickets | | | | |
| **Total** | | | | |

## Defectos encontrados

| ID | Caso | Descripción | Pasos para reproducir | HTTP esperado | HTTP observado | Severidad |
|---|---|---|---|---|---|---|