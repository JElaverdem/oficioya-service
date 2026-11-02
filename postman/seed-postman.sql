-- Datos de prueba para la colección de Postman (OficioYa - Service).
-- Se puede ejecutar todas las veces que se quiera: borra y vuelve a crear solo estas solicitudes.
-- Base de datos: oficioyaService (tabla requests).

DELETE FROM requests WHERE id::text LIKE 'aaaaaaaa-0000-0000-0000-0000000000%';

-- Identidades (deben coincidir con las variables de la colección):
--   trabajador       11111111-1111-1111-1111-111111111111
--   otro trabajador  22222222-2222-2222-2222-222222222222
--   contratante      33333333-3333-3333-3333-333333333333
--   otro contratante 44444444-4444-4444-4444-444444444444
--
-- Las fechas usan la hora de Bogotá, porque el servicio compara con LocalDateTime.now().

INSERT INTO requests
  (id, root_id, contractor_id, worker_id, description, neighborhood, exact_address,
   date_scheduled, state, date_created, cancellation_date_time, cancellation_state)
VALUES
-- 1. Aceptar (200): PENDING recién creada
('aaaaaaaa-0000-0000-0000-000000000001', 'cccccccc-0000-0000-0000-000000000001',
 '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
 'Reparar una gotera', 'Niza Antigua', 'Cra. 70a # 118-33',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'PENDING',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL),

-- 2. Rechazar (200): PENDING recién creada
('aaaaaaaa-0000-0000-0000-000000000002', 'cccccccc-0000-0000-0000-000000000002',
 '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
 'Cambiar una llave', 'Usaquén', 'Calle 1 # 2-3',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'PENDING',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL),

-- 3. Ajena (404): asignada a OTRO trabajador
('aaaaaaaa-0000-0000-0000-000000000003', 'cccccccc-0000-0000-0000-000000000003',
 '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222',
 'Pintar una pared', 'Chapinero', 'Cra. 7 # 50-10',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'PENDING',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL),

-- 4. Tarde (409): creada hace 45 minutos
('aaaaaaaa-0000-0000-0000-000000000004', 'cccccccc-0000-0000-0000-000000000004',
 '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
 'Instalar un lavamanos', 'Suba', 'Av. Suba # 100-20',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'PENDING',
 (now() AT TIME ZONE 'America/Bogota') - interval '45 minutes', NULL, NULL),

-- 5. No pendiente (409): ya fue rechazada
('aaaaaaaa-0000-0000-0000-000000000005', 'cccccccc-0000-0000-0000-000000000005',
 '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
 'Revisar el gas', 'Kennedy', 'Cra. 80 # 38-15',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'REJECTED',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL),

-- 6 y 7. Conflicto: dos solicitudes del mismo rootId, ambas aceptadas por sus trabajadores
('aaaaaaaa-0000-0000-0000-000000000006', 'bbbbbbbb-0000-0000-0000-000000000001',
 '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
 'Arreglar una puerta', 'Teusaquillo', 'Cll. 45 # 20-10',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'CONFLICT',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL),
('aaaaaaaa-0000-0000-0000-000000000007', 'bbbbbbbb-0000-0000-0000-000000000001',
 '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222',
 'Arreglar una puerta', 'Teusaquillo', 'Cll. 45 # 20-10',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'CONFLICT',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL),

-- 8. Conflicto ajeno (404 para otro contratante)
('aaaaaaaa-0000-0000-0000-000000000008', 'bbbbbbbb-0000-0000-0000-000000000002',
 '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
 'Cambiar una cerradura', 'Engativá', 'Av. Boyacá # 70-30',
 (now() AT TIME ZONE 'America/Bogota') + interval '3 days', 'CONFLICT',
 (now() AT TIME ZONE 'America/Bogota'), NULL, NULL);

SELECT id, worker_id, state FROM requests WHERE id::text LIKE 'aaaaaaaa-0000-0000-0000-0000000000%' ORDER BY id;
