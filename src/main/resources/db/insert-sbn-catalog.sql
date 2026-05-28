-- ============================================================
-- CATÁLOGO SBN — Tabla de Familias de Bienes
-- Superintendencia Nacional de Bienes Estatales
-- Basado en el catálogo de familias del Sistema de Bienes Nacionales
--
-- Tabla destino: sbn_catalog
-- Motor: PostgreSQL
-- ============================================================

CREATE TABLE IF NOT EXISTS sbn_catalog (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo                      VARCHAR(20) UNIQUE NOT NULL,
    descripcion                 VARCHAR(500) NOT NULL,
    grupo                       VARCHAR(100) NOT NULL,
    clase                       VARCHAR(100) NOT NULL,
    tasa_depreciacion_anual     DECIMAL(5,2) DEFAULT 0,
    vida_util_meses             INTEGER,
    es_depreciable              BOOLEAN DEFAULT false,
    requiere_serie_marca_modelo BOOLEAN DEFAULT false,
    created_at                  TIMESTAMP DEFAULT NOW()
);

-- ─── MOBILIARIO (Grupo 511) ─── Tasa: 10% | Vida útil: 120 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('51111001', 'Escritorio de madera',           'MOBILIARIO', 'Muebles de Oficina', 10, 120, true, false),
('51111002', 'Silla giratoria',                'MOBILIARIO', 'Muebles de Oficina', 10, 120, true, false),
('51111003', 'Archivador metálico',            'MOBILIARIO', 'Muebles de Oficina', 10, 120, true, false),
('51111004', 'Estante de madera',              'MOBILIARIO', 'Muebles de Oficina', 10, 120, true, false),
('51111005', 'Mesa de reuniones',              'MOBILIARIO', 'Muebles de Oficina', 10, 120, true, false);

-- ─── EQUIPOS DE CÓMPUTO (Grupo 641) ─── Tasa: 25% | Vida útil: 48 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('64121001', 'Computadora de escritorio',      'EQUIPOS DE COMPUTO', 'Hardware',        25, 48, true, true),
('64121002', 'Computadora portátil (Laptop)',  'EQUIPOS DE COMPUTO', 'Hardware',        25, 48, true, true),
('64121003', 'Impresora láser',                'EQUIPOS DE COMPUTO', 'Periféricos',     25, 48, true, true),
('64121004', 'Impresora de inyección de tinta','EQUIPOS DE COMPUTO', 'Periféricos',     25, 48, true, true),
('64121005', 'Escáner',                        'EQUIPOS DE COMPUTO', 'Periféricos',     25, 48, true, true),
('64121006', 'Monitor LCD/LED',                'EQUIPOS DE COMPUTO', 'Periféricos',     25, 48, true, true),
('64121007', 'Servidor',                       'EQUIPOS DE COMPUTO', 'Hardware',        25, 48, true, true),
('64121008', 'Tablet',                         'EQUIPOS DE COMPUTO', 'Hardware',        25, 48, true, true),
('64121009', 'Teléfono celular / smartphone',  'EQUIPOS DE COMPUTO', 'Comunicaciones',  25, 48, true, true);

-- ─── VEHÍCULOS (Grupo 333) ─── Tasa: 20% | Vida útil: 60 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('33311001', 'Automóvil sedán',                'VEHICULOS', 'Vehículos de transporte', 20, 60, true, true),
('33311002', 'Camioneta pick-up',              'VEHICULOS', 'Vehículos de transporte', 20, 60, true, true),
('33311003', 'Motocicleta',                    'VEHICULOS', 'Vehículos de transporte', 20, 60, true, true),
('33311004', 'Ómnibus / Bus',                  'VEHICULOS', 'Vehículos de transporte', 20, 60, true, true),
('33311005', 'Camión de carga',                'VEHICULOS', 'Vehículos de carga',      20, 60, true, true);

-- ─── MAQUINARIA (Grupo 333) ─── Tasa: 10% | Vida útil: 120 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('33321001', 'Tractor agrícola',               'MAQUINARIA', 'Maquinaria agrícola',       10, 120, true, true),
('33321002', 'Excavadora',                     'MAQUINARIA', 'Maquinaria de construcción',10, 120, true, true);

-- ─── MAQUINARIA Y EQUIPO (Grupo 653) ─── Tasa: 10% | Vida útil: 120 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('65321001', 'Fotocopiadora',                  'MAQUINARIA Y EQUIPO', 'Equipos de oficina',   10, 120, true, true),
('65321002', 'Proyector multimedia',           'MAQUINARIA Y EQUIPO', 'Equipos audiovisuales',10, 120, true, true);

-- ─── REFRIGERACIÓN Y AIRE ACONDICIONADO (Grupo 338) ─── Tasa: 10% | Vida útil: 120 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('33811001', 'Aire acondicionado tipo split',   'REFRIGERACION Y AC', 'Climatización',  10, 120, true, true),
('33811002', 'Aire acondicionado central',      'REFRIGERACION Y AC', 'Climatización',  10, 120, true, true),
('33811003', 'Refrigeradora / frigorífico',     'REFRIGERACION Y AC', 'Refrigeración', 10, 120, true, true),
('33811004', 'Congeladora industrial',          'REFRIGERACION Y AC', 'Refrigeración', 10, 120, true, true);

-- ─── INSTRUMENTOS DE MEDICIÓN (Grupo 334) ─── Tasa: 10% | Vida útil: 120 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('33411001', 'Balanza electrónica de precisión','INSTRUMENTOS DE MEDICION', 'Instrumentos de laboratorio',       10, 120, true, true),
('33411002', 'Osciloscopio',                    'INSTRUMENTOS DE MEDICION', 'Instrumentos de medición eléctrica',10, 120, true, true),
('33411003', 'Termómetro industrial',           'INSTRUMENTOS DE MEDICION', 'Instrumentos de temperatura',       10, 120, true, true);

-- ─── EQUIPOS MÉDICOS (Grupo 336) ─── Tasa: 10% | Vida útil: 120 meses
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('33611001', 'Electrocardiógrafo',              'EQUIPOS MEDICOS', 'Diagnóstico',               10, 120, true, true),
('33611002', 'Ecógrafo',                        'EQUIPOS MEDICOS', 'Diagnóstico por imágenes',  10, 120, true, true),
('33611003', 'Equipo de rayos X',               'EQUIPOS MEDICOS', 'Diagnóstico por imágenes',  10, 120, true, true),
('33611004', 'Silla de ruedas',                 'EQUIPOS MEDICOS', 'Equipos de rehabilitación', 10, 120, true, false);

-- ─── BIENES CULTURALES (Grupo 461) ─── No depreciables
INSERT INTO sbn_catalog (codigo, descripcion, grupo, clase, tasa_depreciacion_anual, vida_util_meses, es_depreciable, requiere_serie_marca_modelo) VALUES
('46111001', 'Obra de arte / pintura',          'BIENES CULTURALES', 'Artes plásticas', 0, NULL, false, false),
('46111002', 'Escultura / estatua',             'BIENES CULTURALES', 'Artes plásticas', 0, NULL, false, false);
