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
| 1 | GET | `/api/venues/{code}` | Isabella | | | NOT RUN |
| 2 | GET | `/api/venues/active` | Isabella | | | NOT RUN |
| 3 | POST | `/api/events` | Isabella | | | NOT RUN |
| 4 | GET | `/api/events/{eventCode}` | Isabella | | | NOT RUN |
| 5 | GET | `/api/events/published` | Isabella | | | NOT RUN |
| 6 | PATCH | `/api/events/{eventCode}/publish` | Isabella | | | NOT RUN |
| 7 | POST | `/api/events/{eventCode}/artists/{artistId}` | Isabella | | | NOT RUN |
| 8 | GET | `/api/events/by-artist` | Isabella | | | NOT RUN |
| 9 | GET | `/api/artists/{id}` | Isabella | | | NOT RUN |
| 10 | GET | `/api/artists/by-stage-name` | Isabella | | | NOT RUN |
| 11 | GET | `/api/artists/active` | Isabella | | | NOT RUN |
| 12 | POST | `/api/auth/login` | Isabella | | | NOT RUN |
| 13 | GET | `/api/auth/me` | Isabella | | | NOT RUN |
| 14 | POST | `/api/users` | Valeria | TC-AUTH-001, TC-BIZ-006 (USER_B) | E2E-USR-01 a 03 (400), TC-BIZ-006 duplicados (409), TC-SEC-016 | NOT RUN |
| 15 | GET | `/api/users/by-email` | Valeria | E2E-USR-06, E2E-USR-08 (ADMIN) | TC-SEC-006 (403), E2E-USR-10 (401), E2E-USR-12 (404) | NOT RUN |
| 16 | GET | `/api/users/by-username` | Valeria | E2E-USR-07, E2E-USR-09 (ADMIN) | TC-SEC-007 (403), E2E-USR-11 (401) | NOT RUN |
| 17 | POST | `/api/tickets` | Valeria | TC-BIZ-007 (T1 y T2) | TC-SEC-008, TC-BIZ-014 (draft, aforo, edad), TC-BIZ-015, E2E-TKT-03 (401), E2E-TKT-04 (403), E2E-VAL-01 a 03 (400) | NOT RUN |
| 18 | GET | `/api/tickets/{ticketCode}` | Valeria | TC-BIZ-008 | TC-SEC-009 (403), E2E-VAL-04 (404) | NOT RUN |
| 19 | GET | `/api/tickets/by-user` | Valeria | E2E-TKT-01, E2E-TKT-02 (ADMIN) | TC-SEC-010 (403) | NOT RUN |
| 20 | GET | `/api/events/{eventCode}/tickets/paid` | Valeria | TC-BIZ-009 (STAFF) | TC-SEC-013 (403) | NOT RUN |
| 21 | PATCH | `/api/tickets/{ticketCode}/cancel` | Valeria | TC-BIZ-010 | TC-SEC-011 (403 sin mutación), TC-BIZ-011 (409), E2E-VAL-05 (404) | NOT RUN |
| 22 | PATCH | `/api/tickets/{ticketCode}/use` | Valeria | TC-SEC-014 / TC-BIZ-012 (STAFF) | TC-SEC-012 (403 sin mutación), TC-BIZ-013 (409) | NOT RUN |