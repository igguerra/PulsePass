-- Ejecutar DESPUES de que e2e_inactive haya hecho login y su token
-- este guardado en Postman. SOLO para pulsepass_e2e.
UPDATE users SET active = FALSE WHERE username = 'e2e_inactive';

SELECT username, active FROM users WHERE username = 'e2e_inactive';