-- =====================================================================
-- PulsePass - Fixtures E2E (Fase 5)
-- SOLO para la base pulsepass_e2e. Ejecutar despues de que Flyway haya
-- creado las tablas (arrancar la app con el perfil e2e al menos una vez).
-- Idempotente: se puede ejecutar varias veces sin duplicar datos.
-- =====================================================================

-- ---------- Venues ----------
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
    ('VEN-E2E-01',    'Arena E2E Santa Marta', 'Santa Marta',  'Calle 1 # 1-01', 100, TRUE),
    ('VEN-E2E-SMALL', 'Sala E2E Pequena',      'Santa Marta',  'Calle 2 # 2-02', 1,   TRUE),
    ('VEN-E2E-OFF',   'Teatro E2E Inactivo',   'Barranquilla', 'Calle 3 # 3-03', 50,  FALSE)
ON CONFLICT (code) DO NOTHING;

-- ---------- Eventos ----------
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT d.event_code, d.name, d.description, d.category, d.status, d.event_date, d.minimum_age, v.id
FROM (VALUES
    ('EVT-E2E-PUB',   'Festival E2E Publicado', 'Evento publicado de prueba',          'MUSIC',      'PUBLISHED', TIMESTAMP '2027-11-20 20:00:00', 0,  'VEN-E2E-01'),
    ('EVT-E2E-DRAFT', 'Festival E2E Borrador',  'Borrador: nunca visible al publico',  'MUSIC',      'DRAFT',     TIMESTAMP '2027-11-21 20:00:00', 0,  'VEN-E2E-01'),
    ('EVT-E2E-TOPUB', 'Conferencia E2E',        'Borrador preparado para publicar',    'TECHNOLOGY', 'DRAFT',     TIMESTAMP '2027-11-22 09:00:00', 0,  'VEN-E2E-01'),
    ('EVT-E2E-ADULT', 'Noche E2E Mayores 18',   'Publicado con edad minima',           'MUSIC',      'PUBLISHED', TIMESTAMP '2027-11-23 22:00:00', 18, 'VEN-E2E-01'),
    ('EVT-E2E-FULL',  'Show E2E Aforo 1',       'Publicado en venue de capacidad 1',   'CULTURE',    'PUBLISHED', TIMESTAMP '2027-11-24 19:00:00', 0,  'VEN-E2E-SMALL')
) AS d(event_code, name, description, category, status, event_date, minimum_age, venue_code)
JOIN venues v ON v.code = d.venue_code
ON CONFLICT (event_code) DO NOTHING;

-- ---------- Artistas asociados (artistas sembrados por V2) ----------
INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM (VALUES
    ('EVT-E2E-PUB',   'Solar Beat'),
    ('EVT-E2E-DRAFT', 'Solar Beat'),
    ('EVT-E2E-ADULT', 'Neon Waves')
) AS x(event_code, stage_name)
JOIN events  e ON e.event_code = x.event_code
JOIN artists a ON a.stage_name = x.stage_name
ON CONFLICT DO NOTHING;