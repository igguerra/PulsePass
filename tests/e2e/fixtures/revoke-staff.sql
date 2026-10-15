-- Retira el rol STAFF a e2e_staff (su token sigue siendo valido).
-- Para restaurarlo, volver a ejecutar promote-roles.sql. SOLO para pulsepass_e2e.
DELETE FROM user_roles
WHERE role = 'STAFF'
  AND user_id = (SELECT id FROM users WHERE username = 'e2e_staff');

SELECT u.username, r.role
FROM users u LEFT JOIN user_roles r ON r.user_id = u.id
WHERE u.username = 'e2e_staff';