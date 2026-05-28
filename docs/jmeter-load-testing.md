# Pruebas de Carga con JMeter - vg-ms-patrimonioservice

## 1. Descripción

Pruebas de carga al microservicio `vg-ms-patrimonioservice` con JMeter para validar su rendimiento bajo 3 escenarios progresivos en los módulos de **Assets** y **Asset Disposals**.

## 2. Escenarios

### VERDE - Carga Normal (10 usuarios)

| Parámetro | Valor |
|-----------|-------|
| Usuarios | 10 |
| Ramp-up | 30s |
| Iteraciones | 5 |
| Objetivo | Operacion diaria tipica |

**Endpoints:**
- `GET /api/v1/assets` - Listar activos
- `GET /api/v1/asset-disposals` - Listar bajas
- `GET /api/v1/asset-disposal-details/active-asset-ids` - IDs activos

---

### AMARILLO - Carga Moderada (50 usuarios)

| Parámetro | Valor |
|-----------|-------|
| Usuarios | 50 |
| Ramp-up | 60s |
| Iteraciones | 10 |
| Objetivo | Procesos administrativos simultaneos |

**Endpoints:**
- `GET /api/v1/assets?page=0&size=20` - Activos paginados
- `POST /api/v1/assets` - Crear activo
- `GET /api/v1/asset-disposals/status/INITIATED` - Bajas iniciadas
- `POST /api/v1/asset-disposals` - Crear solicitud de baja

---

### ROJO - Carga de Estres (200 usuarios)

| Parámetro | Valor |
|-----------|-------|
| Usuarios | 200 |
| Ramp-up | 30s |
| Iteraciones | 20 |
| Objetivo | Pico maximo / punto de quiebre |

**Endpoints:**
- `GET /api/v1/assets` - Listar todos
- `POST /api/v1/assets` - Crear activo
- `GET /api/v1/asset-disposals` - Listar todas
- `POST /api/v1/asset-disposals` - Crear baja

---

## 3. Ejecucion

```bash
jmeter -n -t jmeter/patrimonio-load-test.jmx -l jmeter/results/resultados.jtl
```

Para ver el reporte:
```bash
jmeter -g jmeter/results/resultados.jtl -o jmeter/results/reporte/
```

## 4. Metricas

| Métrica | Verde | Amarillo | Rojo |
|---------|-------|----------|------|
| Tiempo Respuesta (p50) | < 500ms | < 1000ms | < 3000ms |
| Tiempo Respuesta (p95) | < 1000ms | < 2000ms | < 5000ms |
| Throughput | > 30 req/s | > 80 req/s | > 150 req/s |
| Tasa Error | < 1% | < 5% | < 10% |

## 5. Interpretacion de Resultados Esperados

### Verde (10 users)
- **p50:** ~150-300ms. Consultas GET rapidas.
- **Error:** 0%. Sin contention.
- **Conclusion:** Sistema optimo para uso diario. Arquitectura reactiva WebFlux maneja bien consultas livianas.

### Amarillo (50 users)
- **p50:** ~400-800ms. POSTs incrementan latencia por escritura en BD.
- **Error:** < 1%. Timeouts ocasionales en escritura.
- **Conclusion:** Rendimiento aceptable. El pool R2DBC (max 20) empieza a mostrar contencion.

### Rojo (200 users)
- **p50:** ~1000-2500ms. Latencia alta por contencion de pool BD.
- **Error:** ~3-8%. Timeouts en POST por agotamiento de conexiones.
- **Conclusion:** Sistema se degrada bajo estres. Cuello de botella: pool BD y recursos del contenedor.

## 6. Recomendaciones

1. **Aumentar** `spring.r2dbc.pool.max-size` de 20 a 50
2. **Agregar indices** en `assets(asset_status)` y `asset_disposals(file_status)`
3. **Cache** en GET /api/v1/assets (consultas repetitivas)
4. **Rate limiting** por tenant para evitar abusos
