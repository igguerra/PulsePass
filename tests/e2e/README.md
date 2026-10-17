# PulsePass — Pruebas E2E manuales con Postman (Fase 5)

Colección Postman para verificar de extremo a extremo las 22 rutas de PulsePass
(20 de negocio + login + me) contra la aplicación real y PostgreSQL, **sin mocks
y sin scripts**. Cada request se envía con **Send**, se inspecciona a mano y el
resultado se anota en `registro-ejecucion-manual.md`.

## Estructura

```
tests/e2e/
├── README.md                       # esta guía
├── checklist-e2e-manual.md         # orden de ejecución
├── registro-ejecucion-manual.md    # bitácora de resultados
├── postman/
│   ├── PulsePass-Manual-E2E.postman_collection.json
│   └── PulsePass-local.postman_environment.json
└── fixtures/                       # datos de prueba (ver fixtures/README.md)
```

## 1. Preparación del ambiente

1. Docker Desktop abierto. Base E2E limpia y sembrada según `fixtures/README.md`
   (secciones 1 a 3).
2. Archivo `.env` en la raíz del repo con `PULSEPASS_JWT_SECRET_BASE64` (clave
   exclusiva de E2E) y `PULSEPASS_E2E_DB_PASSWORD`. Plantilla: `.env.example`.
3. Levantar la app con el perfil `e2e`:

```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=e2e"
```

## 2. Postman

1. Instalar **Postman Desktop** (la versión web no alcanza `localhost` sin agente).
2. **Import** → `postman/PulsePass-local.postman_environment.json` y
   `postman/PulsePass-Manual-E2E.postman_collection.json`.
3. Seleccionar el ambiente **PulsePass-local** (arriba a la derecha).
4. Escribir las contraseñas en las variables `*Password` como **valor local**
   (Current value). Nunca en el Initial value.

### Reglas de secretos

- Contraseñas y tokens **solo** en valores locales de Postman.
- No exportar el ambiente después de llenarlo; el archivo del repo es una plantilla vacía.
- No incluir JWT ni contraseñas en capturas ni en la bitácora.
- La colección **no** tiene scripts (Pre-request, Post-response/Tests): la pestaña Scripts está vacía en todas las requests.

## 3. Copia manual de tokens y códigos

1. Ejecutar el login del actor (`03 Auth`).
2. Verificar `200`, `tokenType = Bearer`, `expiresIn = 900`.
3. Copiar **solo** el valor de `accessToken` a la variable del actor
   (`userAToken`, `userBToken`, `staffToken`, `adminToken`, `inactiveToken`).
4. Ejecutar `GET /api/auth/me` con ese token para confirmar la identidad.
5. Si el token vence (15 min), repetir el login y actualizar la variable.

Los códigos generados por la API (`artistId`, `ticketCancelableCode`,
`ticketUsableCode`) se copian igual: desde el Body de la respuesta a la variable.

## 4. Orden de ejecución

Seguir `checklist-e2e-manual.md`: Preparación → Flujo A (público) → Flujo B
(registro y JWT) → Flujo C (catálogo ADMIN) → Flujo D (tickets) → Flujo E
(errores) → Seguridad.

## 5. Criterios de resultado

- **PASS**: HTTP, JSON y efecto de negocio/seguridad coinciden con lo esperado.
- **FAIL**: algo no coincide, o aparece un `500` no esperado.
- **BLOCKED**: no se pudo ejecutar por falta de fixture, actor, token o respuesta previa.
- **NOT RUN**: aún no ejecutado. Nunca se cuenta como aprobado.

Un `2xx` por sí solo no es PASS: hay que revisar el contenido del JSON y, en
mutaciones, hacer un GET posterior.

## 6. Decisiones documentadas

| Decisión | Valor | Origen |
|---|---|---|
| Recurso ajeno (ticket, usuario) | **403** | `AuthorizationService` (Fase 4) |
| Evento no publicado para anónimo/USER | **404**, mismo mensaje que código inexistente | `EventServiceImpl` (Fase 4) |
| Token inválido en ruta pública | **401** (no se ignora) | `JwtAuthenticationFilter` (Fase 4) |
| STAFF en tickets | Global (todos los eventos) | Limitación del MVP, no apta para producción |
| Duración del token | 900 s | `pulsepass.security.jwt.access-token-ttl=PT15M` |

## 7. Matriz de cobertura — 22 rutas

| # | Método | Ruta | Responsable | Caso nominal | Casos de seguridad / error | Resultado |
|---|---|---|---|---|---|---|
| 1 | GET | `/api/venues/{code}` | Isabella | E2E-VEN-02 | E2E-VEN-03 (404) | PASS |
| 2 | GET | `/api/venues/active` | Isabella | E2E-VEN-01, E2E-00-01 | — (ruta pública sin parámetros) | PASS |
| 3 | POST | `/api/events` | Isabella | TC-BIZ-003 | TC-SEC-001a (401), TC-SEC-001b / TC-AUTH-014 (403), E2E-SEC-01 (403), VAL-EVT-01 a 05 (400), VAL-EVT-10 (404), VAL-EVT-12 a 14 (409), E2E-SEC-02 (integridad) | PASS |
| 4 | GET | `/api/events/{eventCode}` | Isabella | E2E-EVT-02, E2E-EVT-06, E2E-EVT-09 | TC-SEC-004 (404 borrador), E2E-EVT-04 (404 USER), TC-SEC-015 / E2E-EVT-03 (200 DRAFT con ADMIN/STAFF), VAL-EVT-06 (404), VAL-HTTP-04 (405) | FAIL (DEF-01) |
| 5 | GET | `/api/events/published` | Isabella | E2E-EVT-01 | TC-AUTH-015 (401 con token inválido) | PASS |
| 6 | PATCH | `/api/events/{eventCode}/publish` | Isabella | TC-BIZ-004 | E2E-EVT-08 (409), TC-SEC-002a/b (403), E2E-SEC-03 (integridad), VAL-EVT-07 (404) | PASS |
| 7 | POST | `/api/events/{eventCode}/artists/{artistId}` | Isabella | TC-BIZ-005 | E2E-EVT-07 (409), TC-SEC-003 / E2E-SEC-04 (403), E2E-SEC-05 (integridad), VAL-EVT-08 y 09 (404) | PASS |
| 8 | GET | `/api/events/by-artist` | Isabella | TC-SEC-005 | E2E-EVT-05 (ADMIN ve DRAFT), VAL-EVT-11 (200 []), VAL-HTTP-03 (400) | FAIL (DEF-04) |
| 9 | GET | `/api/artists/{id}` | Isabella | E2E-ART-02 | E2E-ART-05 (404), VAL-HTTP-01 (400) | FAIL (DEF-02) |
| 10 | GET | `/api/artists/by-stage-name` | Isabella | E2E-ART-03, E2E-ART-04 | VAL-ART-01 (404), VAL-HTTP-02 (400) | FAIL (DEF-03) |
| 11 | GET | `/api/artists/active` | Isabella | E2E-ART-01 | — (ruta pública sin parámetros) | PASS |
| 12 | POST | `/api/auth/login` | Isabella | E2E-AUTH-01 a 04, TC-AUTH-002, TC-AUTH-003 | TC-AUTH-004 y 005 (401), E2E-SEC-08 (401 inactivo), VAL-AUTH-01 (400), VAL-HTTP-05 (415) | FAIL (DEF-05)  |
| 13 | GET | `/api/auth/me` | Isabella | E2E-AUTH-ME-01 a 03, TC-AUTH-006 | TC-AUTH-007 a 013 (401), E2E-SEC-10 (roles [] tras revocación) | PASS |
| 14 | POST | `/api/users` | Valeria | TC-AUTH-001, TC-BIZ-006 (USER_B) | E2E-USR-01 a 03 (400), TC-BIZ-006 duplicados (409), TC-SEC-016 | NOT RUN |
| 15 | GET | `/api/users/by-email` | Valeria | E2E-USR-06, E2E-USR-08 (ADMIN) | TC-SEC-006 (403), E2E-USR-10 (401), E2E-USR-12 (404) | NOT RUN |
| 16 | GET | `/api/users/by-username` | Valeria | E2E-USR-07, E2E-USR-09 (ADMIN) | TC-SEC-007 (403), E2E-USR-11 (401) | NOT RUN |
| 17 | POST | `/api/tickets` | Valeria | TC-BIZ-007 (T1 y T2) | TC-SEC-008, TC-BIZ-014 (draft, aforo, edad), TC-BIZ-015, E2E-TKT-03 (401), E2E-TKT-04 (403), E2E-VAL-01 a 03 (400) | NOT RUN |
| 18 | GET | `/api/tickets/{ticketCode}` | Valeria | TC-BIZ-008 | TC-SEC-009 (403), E2E-VAL-04 (404) | NOT RUN |
| 19 | GET | `/api/tickets/by-user` | Valeria | E2E-TKT-01, E2E-TKT-02 (ADMIN) | TC-SEC-010 (403) | NOT RUN |
| 20 | GET | `/api/events/{eventCode}/tickets/paid` | Valeria | TC-BIZ-009 (STAFF) | TC-SEC-013 (403) | NOT RUN |
| 21 | PATCH | `/api/tickets/{ticketCode}/cancel` | Valeria | TC-BIZ-010 | TC-SEC-011 (403 sin mutación), TC-BIZ-011 (409), E2E-VAL-05 (404) | NOT RUN |
| 22 | PATCH | `/api/tickets/{ticketCode}/use` | Valeria | TC-SEC-014 / TC-BIZ-012 (STAFF) | TC-SEC-012 (403 sin mutación), TC-BIZ-013 (409) | NOT RUN |

Notas:
- La columna **Resultado** se llena después de la corrida oficial (PASS si el caso nominal y todos los casos de la fila pasan; FAIL si alguno falla, indicando el DEF-XX).
- VAL-HTTP-06 (ruta inexistente) no pertenece a ninguna de las 22 rutas; su resultado va solo en la bitácora.
- TC-SEC-017 y E2E-SEC-09/11 (revocación de STAFF) usan la ruta 20; se listan en la fila 20.

## 8. Pasos fuera de Postman (bloque de Isabella)

Algunos casos de seguridad necesitan modificar la base o reiniciar la app.
Ejecutarlos **en este orden**, después de todo lo demás, porque cambian el estado.

| Caso | Paso externo | Restaurar después |
|---|---|---|
| TC-AUTH-013, E2E-SEC-08 | `fixtures/deactivate-user.sql` | No (e2e_inactive queda inactivo) |
| TC-SEC-017 | `fixtures/revoke-staff.sql` | **Sí**: `fixtures/promote-roles.sql` |
| TC-AUTH-011a | Reiniciar la app con una clave JWT temporal | Sí: reiniciar sin la variable |
| TC-AUTH-011b | Reiniciar la app con otro issuer | Sí: reiniciar sin la variable |

Ejecutar un script SQL (desde la raíz del repo):

```bash
docker cp tests/e2e/fixtures/<script>.sql pulsepass-e2e-db:/tmp/<script>.sql
docker exec pulsepass-e2e-db psql -U pulsepass_e2e -d pulsepass_e2e -v ON_ERROR_STOP=1 -f /tmp/<script>.sql
```

Reinicio con clave temporal (PowerShell). La clave solo existe en esa ventana
y nunca se guarda en archivos ni en Postman:

```powershell
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
./mvnw spring-boot:run "-Dspring-boot.run.profiles=e2e"
# login ADMIN → copiar accessToken a otherKeyToken → Ctrl+C
Remove-Item Env:PULSEPASS_JWT_SECRET_BASE64
```

Reinicio con otro issuer:

```powershell
$env:PULSEPASS_JWT_ISSUER = "otro-issuer"
./mvnw spring-boot:run "-Dspring-boot.run.profiles=e2e"
# login ADMIN → copiar accessToken a otherIssuerToken → Ctrl+C
Remove-Item Env:PULSEPASS_JWT_ISSUER
```

Las variables de entorno tienen prioridad sobre el `.env`, por eso no hace falta
editarlo. Al terminar, arrancar la app normal y confirmar con un login de ADMIN.

**Token vencido (TC-AUTH-010):** el TTL está fijo en `PT15M`. Al empezar la
carpeta 07, copiar un token de USER_B a `expiredToken` y enviarlo al final,
cuando hayan pasado más de 16 minutos (15 min + 30 s de tolerancia del reloj).
Anotar la hora de emisión y la de envío.