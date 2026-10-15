# Fixtures E2E — PulsePass

Datos de prueba **exclusivos** de la base `pulsepass_e2e`. Nunca ejecutar estos
scripts contra otra base.

## 1. Base de datos limpia

```bash
docker rm -f pulsepass-e2e-db
docker run -d --name pulsepass-e2e-db -e POSTGRES_DB=pulsepass_e2e -e POSTGRES_USER=pulsepass_e2e -e POSTGRES_PASSWORD=<tu clave del .env> -p 5433:5432 postgres:17-alpine
```

Borrar el contenedor elimina todos los datos. Es la forma de repetir las pruebas desde cero.

## 2. Crear las tablas

Arrancar la app una vez con el perfil `e2e` (Flyway aplica V1–V4):

```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=e2e"
```

## 3. Sembrar venues y eventos

Con la app o al menos el contenedor corriendo, desde la raíz del repo:

```bash
docker cp tests/e2e/fixtures/seed-e2e.sql pulsepass-e2e-db:/tmp/seed-e2e.sql
docker exec pulsepass-e2e-db psql -U pulsepass_e2e -d pulsepass_e2e -v ON_ERROR_STOP=1 -f /tmp/seed-e2e.sql
```

## 4. Identidades privilegiadas

1. En Postman (carpeta `00 Preparación`), registrar con `POST /api/users`:
   `e2e_admin`, `e2e_staff` y `e2e_inactive`. Las contraseñas se eligen en el
   momento y se guardan solo como valor local del ambiente de Postman.
2. Promover roles:

```bash
docker cp tests/e2e/fixtures/promote-roles.sql pulsepass-e2e-db:/tmp/promote-roles.sql
docker exec pulsepass-e2e-db psql -U pulsepass_e2e -d pulsepass_e2e -v ON_ERROR_STOP=1 -f /tmp/promote-roles.sql
```

La consulta final debe mostrar `e2e_admin | ADMIN` y `e2e_staff | STAFF`.

## 5. Scripts que se ejecutan durante las pruebas

| Script | Cuándo | Caso |
|---|---|---|
| `deactivate-user.sql` | Después del login de `e2e_inactive` | TC-AUTH-013 |
| `revoke-staff.sql` | Con un token de STAFF ya guardado | TC-SEC-017 |
| `promote-roles.sql` | Para devolverle el rol a STAFF | Restauración |

Se ejecutan con los mismos dos comandos (`docker cp` + `docker exec`), cambiando el nombre del archivo.

## Fixtures disponibles

| Código | Tipo | Estado / dato clave |
|---|---|---|
| VEN-E2E-01 | Venue | Activo, capacidad 100 |
| VEN-E2E-SMALL | Venue | Activo, capacidad 1 |
| VEN-E2E-OFF | Venue | Inactivo |
| EVT-E2E-PUB | Evento | PUBLISHED, artista Solar Beat |
| EVT-E2E-DRAFT | Evento | DRAFT, artista Solar Beat |
| EVT-E2E-TOPUB | Evento | DRAFT, listo para publicar |
| EVT-E2E-ADULT | Evento | PUBLISHED, edad mínima 18 |
| EVT-E2E-FULL | Evento | PUBLISHED, en VEN-E2E-SMALL |