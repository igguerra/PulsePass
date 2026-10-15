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

## Tickets — Valeria

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|

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