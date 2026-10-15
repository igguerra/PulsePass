-- Ejecutar DESPUES de registrar e2e_admin y e2e_staff con POST /api/users.
-- Convierte esas cuentas en ADMIN y STAFF. SOLO para pulsepass_e2e.

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'e2e_admin'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'e2e_staff'
ON CONFLICT DO NOTHING;

-- Quitar el rol USER que les dio el registro.
DELETE FROM user_roles
WHERE role = 'USER'
  AND user_id IN (SELECT id FROM users WHERE username IN ('e2e_admin', 'e2e_staff'));

-- Verificacion: debe mostrar e2e_admin | ADMIN y e2e_staff | STAFF
SELECT u.username, r.role
FROM users u JOIN user_roles r ON r.user_id = u.id
WHERE u.username IN ('e2e_admin', 'e2e_staff')
ORDER BY u.username;