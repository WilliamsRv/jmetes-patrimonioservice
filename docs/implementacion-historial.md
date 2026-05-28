# Historial de Implementaciones - vg-ms-patrimonioservice

## 2026-05-18 - Docker Compose para Toolchain CI/CD

### Archivos creados

- `docker-compose.yml` — Orquestacion de servicios CI/CD para VPS
- `docker/jenkins/Dockerfile` — Jenkins custom con Java 17, Maven 3.9.6, JMeter 5.6.3, Docker CLI
- `docker/jenkins/plugins.txt` — Plugins preinstalados (Slack, Sonar, JUnit, BlueOcean, etc.)
- `docker/.env.ci` — Variables de entorno configurables para el entorno CI/CD

### Servicios incluidos

| Servicio | Puerto | Proposito |
|----------|--------|-----------|
| Jenkins | 8080 | Servidor CI/CD con pipeline completo |
| SonarQube | 9000 | Analisis estatico de codigo y calidad |
| PostgreSQL | 5432 | Base de datos interna para SonarQube |

### Prerequisitos para el VPS

```bash
# Clonar el repositorio
git clone <repo-url> vg-ms-patrimonioservice
cd vg-ms-patrimonioservice

# Copiar variables de entorno
cp docker/.env.ci .env
# Editar .env con valores deseados (puertos, passwords)

# Construir y levantar
docker compose build
docker compose up -d
```

---

## 2026-05-18 - 3 Pruebas Unitarias (JUnit 5 + Mockito + StepVerifier)

### Prueba 1: AssetService — Valores por defecto en creacion
**Archivo:** `src/test/java/.../application/service/AssetServiceTest.java`

**Que prueba:** Al crear un activo sin `currentValue` ni `createdBy`, el servicio asigna:
- `currentValue = acquisitionValue` (mantiene coherencia contable)
- `createdBy = 00000000-0000-0000-0000-000000000000` (UUID por defecto)

**Logica de negocio:** Lineas 43-45 del `AssetService.create()`:
```java
if (asset.getCurrentValue() == null)
    asset.setCurrentValue(asset.getAcquisitionValue());
```

### Prueba 2: DepreciationService — Calculo con valor residual
**Archivo:** `src/test/java/.../application/service/DepreciationServiceTest.java`

**Que prueba:** La formula de depreciacion lineal con valor residual no nulo:
- `initialValue = 5000`, `residualValue = 500`, `usefulLife = 5 años`
- `annualDepreciation = (5000 - 500) / 5 = 900.00`
- `monthlyDepreciation = 900 / 12 = 75.00`

**Logica de negocio:** Lineas 80-91 del `DepreciationService.create()`:
```java
BigDecimal depreciableValue = initialValue.subtract(residualValue);
depreciation.setAnnualDepreciation(
    depreciableValue.divide(BigDecimal.valueOf(usefulLife), 2, RoundingMode.HALF_UP));
```

### Prueba 3: AssetDisposalService — Restauracion con actualizacion de estado
**Archivo:** `src/test/java/.../application/service/AssetDisposalServiceTest.java`

**Que prueba:** Al restaurar una baja ejecutada que tiene detalles de activos asociados, el servicio llama a `assetStatusPort.updateAssetStatusToAvailable()` para cada activo involucrado.

**Flujo:** `restore()` → `updateAssetsStatusToAvailable()` → `assetDisposalDetailPort.findByDisposalId()` → `assetStatusPort.updateAssetStatusToAvailable()`

---

## 2026-05-18 - Mejora del Jenkinsfile

### Cambios realizados

| Aspecto | Antes | Despues |
|---------|-------|---------|
| Reportes JUnit | No se publicaban | `junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'` |
| Cobertura JaCoCo | No se publicaba | `jacoco(execPattern, classPattern, sourcePattern)` |
| SonarQube | `mvn compile sonar:sonar` sin cobertura | `mvn verify sonar:sonar` con `sonar.coverage.jacoco.xmlReportPaths` |
| Slack success | Mensaje basico + JMeter summary | Tests pasados + % cobertura + JMeter summary |
| Slack failure | Mensaje basico | Etapa fallida + URL del build |
| Slack unstable | No existia | Nuevo bloque `unstable` con color naranja |
| Limpieza | `kill` manual | `cleanWs` + `kill` en `always` |

---

## 2026-05-18 - Mejora de load-test.sh

### Cambio

- **Antes:** JMeter ejecutaba solo el test plan y guardaba resultados en JTL
- **Despues:** JMeter genera ademas un **reporte HTML** con graficos usando las flags `-e -o jmeter/results/report`

```bash
# Antes
jmeter -n -t jmeter/patrimonio-load-test.jmx -l jmeter/results/resultados.jtl

# Despues
jmeter -n -t jmeter/patrimonio-load-test.jmx -l jmeter/results/resultados.jtl -e -o jmeter/results/report
```

### Reporte HTML genera

- Tabla resumen con p50, p90, p95, p99
- Grafico de throughput a traves del tiempo
- Grafico de tiempos de respuesta
- Distribucion de errores
- Grafico de latencia
