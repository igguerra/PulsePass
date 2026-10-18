# Registro de ejecución manual — PulsePass E2E

## Datos de la corrida

| Campo | Valor |
|---|---|
| Fecha | 2026-10-16 |
| runId | run01 |
| Ejecutado por | Isabella (Venues, Artists, Events, Auth/JWT) y Valeria (Users, Tickets) |
| Commit del backend | 19a264e |
| Base de datos | `pulsepass_e2e` (limpia + `seed-e2e.sql`) |

Resultados posibles: `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`.
No incluir JWT, contraseñas ni datos personales reales.

## Venues, Artists y Events — Isabella

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-00-01 | Anónimo | GET /api/venues/active | 200 | 200 | Arreglo de venues; app y base conectadas | PASS | |
| E2E-VEN-01 | Anónimo | GET /api/venues/active | 200 | 200 | Incluye VEN-E2E-01 y VEN-E2E-SMALL; sin VEN-E2E-OFF | PASS | |
| E2E-VEN-02 | Anónimo | GET /api/venues/VEN-E2E-01 | 200 | 200 | code VEN-E2E-01, "Arena E2E Santa Marta", activo | PASS | |
| E2E-VEN-03 | Anónimo | GET /api/venues/VEN-NO-EXISTE | 404 | 404 | "Venue not found: VEN-NO-EXISTE" | PASS | |
| E2E-ART-01 | Anónimo | GET /api/artists/active | 200 | 200 | Solo artistas activos; incluye Solar Beat (id copiado a artistId) | PASS | |
| E2E-ART-02 | Anónimo | GET /api/artists/{artistId} | 200 | 200 | stageName Solar Beat, mismo id | PASS | |
| E2E-ART-03 | Anónimo | GET /api/artists/by-stage-name?stageName=Solar Beat | 200 | 200 | stageName Solar Beat | PASS | |
| E2E-ART-04 | Anónimo | GET /api/artists/by-stage-name?stageName=solar beat | 200 | 200 | Mismo artista; búsqueda sin distinguir mayúsculas | PASS | |
| E2E-ART-05 | Anónimo | GET /api/artists/999999 | 404 | 404 | "Artist not found: 999999" | PASS | |
| E2E-EVT-01 | Anónimo | GET /api/events/published | 200 | 200 | EVT-E2E-PUB, EVT-E2E-ADULT, EVT-E2E-FULL; sin DRAFT ni TOPUB | PASS | |
| E2E-EVT-02 | Anónimo | GET /api/events/EVT-E2E-PUB | 200 | 200 | status PUBLISHED, venue VEN-E2E-01, artists [Solar Beat] | PASS | |
| TC-SEC-004 | Anónimo | GET /api/events/EVT-E2E-DRAFT | 404 | 404 | "Event not found: EVT-E2E-DRAFT"; sin datos del borrador | PASS | |
| TC-SEC-005 | Anónimo | GET /api/events/by-artist?stageName=Solar Beat | 200 | 200 | Solo EVT-E2E-PUB; DRAFT excluido | PASS | |
| TC-SEC-015 | ADMIN | GET /api/events/EVT-E2E-DRAFT | 200 | 200 | status DRAFT | PASS | |
| E2E-EVT-03 | STAFF | GET /api/events/EVT-E2E-DRAFT | 200 | 200 | status DRAFT | PASS | |
| E2E-EVT-04 | USER_A | GET /api/events/EVT-E2E-DRAFT | 404 | 404 | "Event not found: EVT-E2E-DRAFT"; igual que anónimo | PASS | |
| E2E-EVT-05 | ADMIN | GET /api/events/by-artist?stageName=Solar Beat | 200 | 200 | EVT-E2E-PUB y EVT-E2E-DRAFT | PASS | |
| TC-BIZ-003 | ADMIN | POST /api/events | 201 | 201 | eventCode EVT-NEW-01, status DRAFT, artists [] | PASS | |
| E2E-EVT-06 | ADMIN | GET /api/events/EVT-NEW-01 | 200 | 200 | status DRAFT; no aparece en /published | PASS | |
| TC-BIZ-005 | ADMIN | POST /api/events/EVT-NEW-01/artists/{artistId} | 200 | 200 | artists [Solar Beat] | PASS | |
| E2E-EVT-07 | ADMIN | POST /api/events/EVT-NEW-01/artists/{artistId} | 409 | 409 | "Artist is already associated with the event: Solar Beat" | PASS | |
| TC-BIZ-004 | ADMIN | PATCH /api/events/EVT-NEW-01/publish | 200 | 200 | status PUBLISHED | PASS | |
| E2E-EVT-08 | ADMIN | PATCH /api/events/EVT-NEW-01/publish | 409 | 409 | "Only DRAFT events can be published. Current status: PUBLISHED" | PASS | |
| E2E-EVT-09 | Anónimo | GET /api/events/EVT-NEW-01 | 200 | 200 | status PUBLISHED; aparece en /published y en by-artist | PASS | |
| TC-SEC-001a | Anónimo | POST /api/events | 401 | 401 | ErrorResponse 401 genérico | PASS | |
| TC-SEC-001b / TC-AUTH-014 | USER_A | POST /api/events | 403 | 403 | "You do not have permission to perform this operation" | PASS | |
| E2E-SEC-01 | STAFF | POST /api/events | 403 | 403 | ErrorResponse 403 | PASS | |
| E2E-SEC-02 | ADMIN | GET /api/events/EVT-DENY-01 | 404 | 404 | "Event not found: EVT-DENY-01"; ningún intento lo creó | PASS | |
| TC-SEC-002a | USER_A | PATCH /api/events/EVT-E2E-TOPUB/publish | 403 | 403 | ErrorResponse 403 | PASS | |
| TC-SEC-002b | STAFF | PATCH /api/events/EVT-E2E-TOPUB/publish | 403 | 403 | ErrorResponse 403 | PASS | |
| E2E-SEC-03 | ADMIN | GET /api/events/EVT-E2E-TOPUB | 200 | 200 | Sin cambios: status DRAFT | PASS | |
| TC-SEC-003 | USER_A | POST /api/events/EVT-E2E-TOPUB/artists/{artistId} | 403 | 403 | ErrorResponse 403 | PASS | |
| E2E-SEC-04 | STAFF | POST /api/events/EVT-E2E-TOPUB/artists/{artistId} | 403 | 403 | ErrorResponse 403 | PASS | |
| E2E-SEC-05 | ADMIN | GET /api/events/EVT-E2E-TOPUB | 200 | 200 | Sin cambios: artists [], status DRAFT | PASS | |
| VAL-EVT-01 | ADMIN | POST /api/events (body vacío) | 400 | 400 | "Validation Failed": eventCode, name, category, eventDate, minimumAge, venueCode | PASS | |
| VAL-EVT-02 | ADMIN | POST /api/events (minimumAge -1) | 400 | 400 | "Validation Failed": minimumAge | PASS | |
| VAL-EVT-03 | ADMIN | POST /api/events (category ROCK) | 400 | 400 | "Malformed JSON request" | PASS | |
| VAL-EVT-04 | ADMIN | POST /api/events (fecha mal formateada) | 400 | 400 | "Malformed JSON request" | PASS | |
| VAL-EVT-05 | ADMIN | POST /api/events (JSON roto) | 400 | 400 | "Malformed JSON request" | PASS | |
| VAL-EVT-06 | Anónimo | GET /api/events/EVT-NO-EXISTE | 404 | 404 | "Event not found: EVT-NO-EXISTE" | PASS | |
| VAL-EVT-07 | ADMIN | PATCH /api/events/EVT-NO-EXISTE/publish | 404 | 404 | "Event not found: EVT-NO-EXISTE" | PASS | |
| VAL-EVT-08 | ADMIN | POST /api/events/EVT-NO-EXISTE/artists/{artistId} | 404 | 404 | "Event not found: EVT-NO-EXISTE" | PASS | |
| VAL-EVT-09 | ADMIN | POST /api/events/EVT-E2E-TOPUB/artists/999999 | 404 | 404 | "Artist not found: 999999" | PASS | |
| VAL-EVT-10 | ADMIN | POST /api/events (venue VEN-NO-EXISTE) | 404 | 404 | "Venue not found: VEN-NO-EXISTE" | PASS | |
| VAL-ART-01 | Anónimo | GET /api/artists/by-stage-name?stageName=Artista Fantasma | 404 | 404 | "Artist not found: Artista Fantasma" | PASS | |
| VAL-EVT-11 | Anónimo | GET /api/events/by-artist?stageName=Artista Fantasma | 200 | 200 | Arreglo vacío [] | PASS | |
| VAL-EVT-12 | ADMIN | POST /api/events (código EVT-E2E-PUB) | 409 | 409 | "Event code already exists: EVT-E2E-PUB" | PASS | |
| VAL-EVT-13 | ADMIN | POST /api/events (venue VEN-E2E-OFF) | 409 | 409 | "Venue is not active: VEN-E2E-OFF" | PASS | |
| VAL-EVT-14 | ADMIN | POST /api/events (fecha 2020-01-01) | 409 | 409 | "Event date must be in the future." | PASS | |
| VAL-HTTP-01 | Anónimo | GET /api/artists/abc | 400 | 500 | "Unexpected internal error", sin traza | FAIL | DEF-02 |
| VAL-HTTP-02 | Anónimo | GET /api/artists/by-stage-name (sin stageName) | 400 | 500 | "Unexpected internal error", sin traza | FAIL | DEF-03 |
| VAL-HTTP-03 | Anónimo | GET /api/events/by-artist (sin stageName) | 400 | 500 | "Unexpected internal error", sin traza | FAIL | DEF-04 |
| VAL-HTTP-04 | ADMIN | PATCH /api/events/EVT-E2E-PUB | 405 | 500 | "Unexpected internal error", sin traza | FAIL | DEF-01 |
| VAL-HTTP-06 | ADMIN | GET /api/ruta-inexistente | 404 | 500 | "Unexpected internal error", sin traza | FAIL | DEF-06 |

## Auth: login, /me y JWT — Isabella

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| E2E-00-02 | Anónimo | POST /api/users (e2e_admin) | 201 | 201 | Usuario creado; sin password en la respuesta | PASS | Luego promote-roles.sql |
| E2E-00-03 | Anónimo | POST /api/users (e2e_staff) | 201 | 201 | Usuario creado; sin password en la respuesta | PASS | Luego promote-roles.sql |
| E2E-00-04 | Anónimo | POST /api/users (e2e_inactive) | 201 | 201 | Usuario creado; sin password en la respuesta | PASS | |
| E2E-AUTH-01 | ADMIN | POST /api/auth/login | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | |
| E2E-AUTH-02 | STAFF | POST /api/auth/login | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | |
| E2E-AUTH-03 | INACTIVO (aún activo) | POST /api/auth/login | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | Token usado en TC-AUTH-013 |
| TC-AUTH-002 | USER_A | POST /api/auth/login (username) | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | |
| TC-AUTH-003 | USER_A | POST /api/auth/login (email) | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | |
| E2E-AUTH-04 | USER_B | POST /api/auth/login | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | |
| TC-AUTH-004 | USER_A | POST /api/auth/login (contraseña incorrecta) | 401 | 401 | "Authentication is required or credentials are invalid" | PASS | |
| TC-AUTH-005 | Anónimo | POST /api/auth/login (usuario inexistente) | 401 | 401 | Mismo mensaje que TC-AUTH-004; no revela si el usuario existe | PASS | |
| VAL-AUTH-01 | Anónimo | POST /api/auth/login (campos vacíos) | 400 | 400 | "Validation Failed": identifier, password | PASS | |
| VAL-HTTP-05 | Anónimo | POST /api/auth/login (text/plain) | 415 | 500 | "Unexpected internal error", sin traza | FAIL | DEF-05 |
| E2E-AUTH-ME-01 | ADMIN | GET /api/auth/me | 200 | 200 | username e2e_admin, roles [ADMIN] | PASS | |
| E2E-AUTH-ME-02 | STAFF | GET /api/auth/me | 200 | 200 | username e2e_staff, roles [STAFF] | PASS | |
| TC-AUTH-006 | USER_A | GET /api/auth/me | 200 | 200 | username e2e_user_a, roles [USER] | PASS | |
| E2E-AUTH-ME-03 | USER_B | GET /api/auth/me | 200 | 200 | username e2e_user_b, roles [USER], id distinto al de A | PASS | |
| TC-AUTH-007 | Anónimo | GET /api/auth/me (sin header) | 401 | 401 | ErrorResponse 401 genérico | PASS | |
| TC-AUTH-008 | Bearer inventado | GET /api/auth/me | 401 | 401 | ErrorResponse 401 genérico | PASS | |
| TC-AUTH-009 | Token alterado | GET /api/auth/me | 401 | 401 | ErrorResponse 401; el token original sí dio 200 | PASS | |
| TC-AUTH-012 | Token en query string | GET /api/auth/me?access_token=… | 401 | 401 | ErrorResponse 401; el token en la URL no se acepta | PASS | |
| TC-AUTH-015 | Token alterado | GET /api/events/published | 401 | 401 | ErrorResponse 401; no se degrada a anónimo | PASS | |
| E2E-SEC-07 | INACTIVO (aún activo) | GET /api/auth/me | 200 | 200 | username e2e_inactive | PASS | Luego deactivate-user.sql |
| TC-AUTH-013 | INACTIVO (desactivado) | GET /api/auth/me (mismo token) | 401 | 401 | ErrorResponse 401; el mismo token ya no sirve | PASS | |
| E2E-SEC-08 | INACTIVO (desactivado) | POST /api/auth/login | 401 | 401 | 401 genérico; no revela que la cuenta está inactiva | PASS | |
| E2E-SEC-09 | STAFF | GET /api/events/EVT-E2E-PUB/tickets/paid | 200 | 200 | Arreglo de tickets pagados | PASS | Luego revoke-staff.sql |
| TC-SEC-017 | STAFF (rol revocado) | GET /api/events/EVT-E2E-PUB/tickets/paid (mismo token) | 403 | 403 | ErrorResponse 403; roles leídos de la base en cada petición | PASS | |
| E2E-SEC-10 | STAFF (rol revocado) | GET /api/auth/me | 200 | 200 | username e2e_staff, roles [] | PASS | Luego promote-roles.sql |
| E2E-SEC-11 | STAFF (rol restaurado) | GET /api/events/EVT-E2E-PUB/tickets/paid | 200 | 200 | Acceso restablecido con el mismo token | PASS | |
| TC-AUTH-011a | Token de otra clave | GET /api/auth/me | 401 | 401 | ErrorResponse 401 | PASS | Clave temporal no registrada |
| TC-AUTH-011b | Token de otro issuer | GET /api/auth/me | 401 | 401 | ErrorResponse 401 | PASS | issuer "otro-issuer" |
| TC-AUTH-010 | Token vencido | GET /api/auth/me | 401 | 401 | ErrorResponse 401 | PASS | Más de 16 min tras la emisión |

## Users — Valeria

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| TC-AUTH-001 Registrar USER_A | anónimo | POST `/api/users` | 201 | 201 | id presente, username e2e_user_a, active true; sin password, passwordHash ni accessToken | PASS | |
| TC-BIZ-006 Registrar USER_B | anónimo | POST `/api/users` | 201 | 201 | id distinto al de A, email de B, active true; sin password | PASS | |
| E2E-USR-01 Registro con email mal formado | anónimo | POST `/api/users` | 400 | 400 | "Validation Failed", details.email | PASS | Ningún usuario creado |
| E2E-USR-02 Registro con password de 11 caracteres | anónimo | POST `/api/users` | 400 | 400 | "Validation Failed", details.password (rango 12–72) | PASS | |
| E2E-USR-03 Registro con firstName vacío | anónimo | POST `/api/users` | 400 | 400 | details.firstName "First name is required" | PASS | |
| TC-BIZ-006 Duplicado: mismo username | anónimo | POST `/api/users` | 409 | 409 | "Username already exists." | PASS | |
| TC-BIZ-006 Duplicado: email de A en mayúsculas | anónimo | POST `/api/users` | 409 | 409 | "Email already exists." | PASS | Unicidad sin distinguir mayúsculas |
| TC-SEC-016 Registro con role ADMIN inyectado | anónimo | POST `/api/users` | 201 | 201 | Usuario creado; sin password | PASS | Verificado con SQL: rol en la base = USER, no ADMIN |
| E2E-USR-04 Registrar usuario menor (2012) | anónimo | POST `/api/users` | 201 | 201 | birthDate 2012-05-10, active true | PASS | |
| E2E-USR-05 Login usuario menor | anónimo | POST `/api/auth/login` | 200 | 200 | tokenType Bearer, expiresIn 900 | PASS | Token copiado a minorToken |
| E2E-USR-06 USER_A consulta su email | USER_A | GET `/api/users/by-email?email=…` | 200 | 200 | username y email de A; sin password | PASS | |
| E2E-USR-07 USER_A consulta su username | USER_A | GET `/api/users/by-username?username=…` | 200 | 200 | Mismo id que E2E-USR-06; sin password | PASS | |
| TC-SEC-006 USER_B consulta email de A | USER_B | GET `/api/users/by-email?email=…` | 403 | 403 | ErrorResponse 403; sin datos personales de A | PASS | |
| TC-SEC-007 USER_B consulta username de A | USER_B | GET `/api/users/by-username?username=…` | 403 | 403 | ErrorResponse 403; sin datos personales de A | PASS | |
| E2E-USR-08 ADMIN consulta email de A | ADMIN | GET `/api/users/by-email?email=…` | 200 | 200 | Datos de A; sin password | PASS | |
| E2E-USR-09 ADMIN consulta username de A | ADMIN | GET `/api/users/by-username?username=…` | 200 | 200 | Datos de A; sin password | PASS | |
| E2E-USR-10 Sin token: by-email | anónimo | GET `/api/users/by-email?email=…` | 401 | 401 | ErrorResponse 401 genérico | PASS | |
| E2E-USR-11 Sin token: by-username | anónimo | GET `/api/users/by-username?username=…` | 401 | 401 | ErrorResponse 401 genérico | PASS | |
| E2E-USR-12 Email inexistente | USER_B | GET `/api/users/by-email?email=…` | 404 | 404 | ErrorResponse 404 | PASS | Ver OBS-01 |

## Tickets — Valeria

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-007 USER_A compra T1 en EVT-E2E-PUB | USER_A | POST `/api/tickets` | 201 | 201 | status PAID, type GENERAL, price 100.00, userEmail de A | PASS | ticketCode copiado a ticketCancelableCode |
| TC-BIZ-008 USER_A lee T1 | USER_A | GET `/api/tickets/{T1}` | 200 | 200 | status PAID, userEmail de A | PASS | |
| TC-SEC-009 USER_B lee T1 de A | USER_B | GET `/api/tickets/{T1}` | 403 | 403 | ErrorResponse 403; sin ticketCode, precio ni email de A | PASS | |
| TC-SEC-011 USER_B cancela T1 de A | USER_B | PATCH `/api/tickets/{T1}/cancel` | 403 | 403 | ErrorResponse 403 | PASS | |
| TC-SEC-011 verificación: T1 sigue PAID | USER_A | GET `/api/tickets/{T1}` | 200 | 200 | Sin cambios: status PAID | PASS | |
| TC-BIZ-010 USER_A cancela T1 | USER_A | PATCH `/api/tickets/{T1}/cancel` | 200 | 200 | status CANCELLED | PASS | |
| TC-BIZ-010 verificación: T1 CANCELLED | USER_A | GET `/api/tickets/{T1}` | 200 | 200 | status CANCELLED | PASS | |
| TC-BIZ-011 Cancelar T1 otra vez | USER_A | PATCH `/api/tickets/{T1}/cancel` | 409 | 409 | message: solo se cancelan tickets PAID | PASS | |
| TC-BIZ-007 USER_A compra T2 (VIP) | USER_A | POST `/api/tickets` | 201 | 201 | status PAID, type VIP, price 200.00 | PASS | ticketCode copiado a ticketUsableCode |
| TC-BIZ-009 STAFF lista pagados de EVT-E2E-PUB | STAFF | GET `/api/events/EVT-E2E-PUB/tickets/paid` | 200 | 200 | Incluye T2; no incluye T1 (cancelado); todos PAID | PASS | |
| TC-SEC-013 USER_A lista pagados | USER_A | GET `/api/events/EVT-E2E-PUB/tickets/paid` | 403 | 403 | ErrorResponse 403 | PASS | |
| TC-SEC-012 USER_B usa T2 de A | USER_B | PATCH `/api/tickets/{T2}/use` | 403 | 403 | ErrorResponse 403 | PASS | |
| TC-SEC-012 verificación: T2 sigue PAID | USER_A | GET `/api/tickets/{T2}` | 200 | 200 | Sin cambios: status PAID | PASS | |
| TC-SEC-014 STAFF marca T2 como usado | STAFF | PATCH `/api/tickets/{T2}/use` | 200 | 200 | status USED | PASS | |
| TC-BIZ-013 STAFF usa T2 otra vez | STAFF | PATCH `/api/tickets/{T2}/use` | 409 | 409 | "Only PAID tickets can be marked as used: …" | PASS | |
| TC-BIZ-012 verificación: T2 USED | USER_A | GET `/api/tickets/{T2}` | 200 | 200 | status USED | PASS | |
| E2E-TKT-01 USER_A lista sus tickets | USER_A | GET `/api/tickets/by-user?email=…` | 200 | 200 | T1 (CANCELLED) y T2 (USED), todos de A | PASS | |
| TC-SEC-010 USER_B lista tickets de A | USER_B | GET `/api/tickets/by-user?email=…` | 403 | 403 | ErrorResponse 403; sin tickets de A | PASS | |
| E2E-TKT-02 ADMIN lista tickets de A | ADMIN | GET `/api/tickets/by-user?email=…` | 200 | 200 | Mismo contenido que E2E-TKT-01 | PASS | |
| TC-SEC-008 USER_A compra con el email de B | USER_A | POST `/api/tickets` | 403 | 403 | ErrorResponse 403 | PASS | |
| TC-SEC-008 verificación: B no tiene tickets | USER_B | GET `/api/tickets/by-user?email=…` | 200 | 200 | Arreglo vacío [] | PASS | No se creó ticket a nombre de B |
| TC-BIZ-014 Compra en evento DRAFT | USER_A | POST `/api/tickets` | 409 | 409 | "Event is not published: EVT-E2E-DRAFT" | PASS | |
| TC-BIZ-015 Compra en evento inexistente | USER_A | POST `/api/tickets` | 404 | 404 | "Event not found: EVT-E2E-NOPE" | PASS | |
| TC-BIZ-014 Aforo: USER_A compra el único cupo | USER_A | POST `/api/tickets` | 201 | 201 | status PAID, type STUDENT, price 70.00 | PASS | EVT-E2E-FULL pasa a SOLD_OUT |
| TC-BIZ-014 Aforo: USER_B intenta comprar | USER_B | POST `/api/tickets` | 409 | 409 | "Event is not published: EVT-E2E-FULL" | PASS | Ver OBS-02 |
| TC-BIZ-014 Aforo verificación: un solo pagado | STAFF | GET `/api/events/EVT-E2E-FULL/tickets/paid` | 200 | 200 | Exactamente 1 ticket, de USER_A | PASS | |
| TC-BIZ-014 Edad: menor compra en EVT-E2E-ADULT | MENOR | POST `/api/tickets` | 409 | 409 | "User does not meet minimum age for event: EVT-E2E-ADULT" | PASS | |
| TC-BIZ-014 Edad verificación: menor sin tickets | MENOR | GET `/api/tickets/by-user?email=…` | 200 | 200 | Arreglo vacío [] | PASS | |
| E2E-TKT-03 Compra sin token | anónimo | POST `/api/tickets` | 401 | 401 | ErrorResponse 401 genérico | PASS | |
| E2E-TKT-04 ADMIN intenta comprar | ADMIN | POST `/api/tickets` | 403 | 403 | ErrorResponse 403 | PASS | |
| E2E-VAL-01 Compra sin userEmail | USER_A | POST `/api/tickets` | 400 | 400 | "Validation Failed", details.userEmail | PASS | |
| E2E-VAL-02 Compra con type inválido | USER_A | POST `/api/tickets` | 400 | 400 | "Malformed JSON request" | PASS | Ningún ticket creado |
| E2E-VAL-03 Compra con JSON mal formado | USER_A | POST `/api/tickets` | 400 | 400 | "Malformed JSON request" | PASS | |
| E2E-VAL-04 Ticket inexistente | USER_A | GET `/api/tickets/TKT-NO-EXISTE` | 404 | 404 | "Ticket not found: TKT-NO-EXISTE" | PASS | |
| E2E-VAL-05 Cancelar ticket inexistente | USER_A | PATCH `/api/tickets/TKT-NO-EXISTE/cancel` | 404 | 404 | ErrorResponse 404 | PASS | |

## Flujos de negocio — Isabella y Valeria

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|
| FLOW-01 | ADMIN | POST /api/events | 201 | 201 | eventCode EVT-FLOW-01, status DRAFT, artists [] | PASS | |
| FLOW-02 | ADMIN | POST /api/events/EVT-FLOW-01/artists/{artistId} | 200 | 200 | artists [Solar Beat], status DRAFT | PASS | |
| FLOW-03 | ADMIN | PATCH /api/events/EVT-FLOW-01/publish | 200 | 200 | status PUBLISHED, artists [Solar Beat] | PASS | |
| FLOW-04 | Anónimo | GET /api/events/published | 200 | 200 | Incluye EVT-FLOW-01 con status PUBLISHED | PASS | |
| FLOW-05 | USER_A | POST /api/tickets | 201 | 201 | status PAID, type GENERAL, userEmail de A, eventCode EVT-FLOW-01 | PASS | ticketCode copiado a flowTicketCode. Un primer intento dio 403 por un token equivocado en la variable userAToken; se rehízo el login y pasó |
| FLOW-06 | USER_A | GET /api/tickets/{flowTicketCode} | 200 | 200 | status PAID, userEmail de A | PASS | |
| FLOW-07 | USER_B | GET /api/tickets/{flowTicketCode} | 403 | 403 | ErrorResponse 403; sin datos del ticket | PASS | |
| FLOW-08 | STAFF | GET /api/events/EVT-FLOW-01/tickets/paid | 200 | 200 | Un solo ticket (flowTicketCode), status PAID | PASS | |
| FLOW-09 | STAFF | PATCH /api/tickets/{flowTicketCode}/use | 200 | 200 | status USED | PASS | |
| FLOW-10 | USER_A | GET /api/tickets/{flowTicketCode} | 200 | 200 | status USED | PASS | |
| FLOW-11 | STAFF | PATCH /api/tickets/{flowTicketCode}/use | 409 | 409 | "Only PAID tickets can be marked as used: …" | PASS | |

| Módulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| Venues / Artists / Events | 49 | 5 | 0 | 0 |
| Auth / JWT | 31 | 1 | 0 | 0 |
| Users | 19 | 0 | 0 | 0 |
| Tickets | 35 | 0 | 0 | 0 |
| Flujos de negocio | 11 | 0 | 0 | 0 |
| **Total** | **145** | **6** | **0** | **0** |

## Defectos encontrados

| ID | Caso | Descripción | Pasos para reproducir | HTTP esperado | HTTP observado | Severidad |
|---|---|---|---|---|---|---|
| DEF-01 | VAL-HTTP-04 | Un método HTTP no soportado en una ruta existente devuelve 500 en lugar de 405. Causa: `GlobalExceptionHandler` no maneja `HttpRequestMethodNotSupportedException` y cae en el handler genérico de `Exception`. | `PATCH /api/events/EVT-E2E-PUB` con Bearer de ADMIN, sin body | 405 | 500 | Media |
| DEF-02 | VAL-HTTP-01 | Un path variable de tipo inválido devuelve 500 en lugar de 400. Causa: falta un handler para `MethodArgumentTypeMismatchException`. | `GET /api/artists/abc` sin token | 400 | 500 | Media |
| DEF-03 | VAL-HTTP-02 | Un query param obligatorio ausente devuelve 500 en lugar de 400. Causa: falta un handler para `MissingServletRequestParameterException`. | `GET /api/artists/by-stage-name` sin el parámetro `stageName` | 400 | 500 | Media |
| DEF-04 | VAL-HTTP-03 | Mismo defecto que DEF-03 en la búsqueda de eventos por artista. | `GET /api/events/by-artist` sin el parámetro `stageName` | 400 | 500 | Media |
| DEF-05 | VAL-HTTP-05 | Un body con Content-Type distinto de JSON devuelve 500 en lugar de 415. Causa: falta un handler para `HttpMediaTypeNotSupportedException`. | `POST /api/auth/login` con Body raw → Text (`text/plain`) | 415 | 500 | Media |
| DEF-06 | VAL-HTTP-06 | Una ruta inexistente (con usuario autenticado) devuelve 500 en lugar de 404. Causa: falta un handler para `NoResourceFoundException`. | `GET /api/ruta-inexistente` con Bearer de ADMIN | 404 | 500 | Media |
| OBS-01 | E2E-USR-12 | Observación (no es FAIL): `GET /api/users/by-email` responde 404 si el email no existe y 403 si existe pero es de otro usuario. La diferencia permite a un USER averiguar qué emails están registrados (enumeración). | Con token de USER_B: consultar un email inexistente (404) y el email de USER_A (403) | 403 uniforme (recomendado) | 404 | Observación |
| OBS-02 | TC-BIZ-014 Aforo | Observación (no es FAIL): al agotarse el aforo, el evento pasa a SOLD_OUT y una nueva compra responde "Event is not published: EVT-E2E-FULL" en lugar de un mensaje de capacidad agotada. El 409 es correcto, pero el mensaje confunde al cliente. | USER_A compra el único cupo de EVT-E2E-FULL; luego USER_B intenta comprar | 409 con mensaje de aforo agotado | 409 "Event is not published" | Observación |
