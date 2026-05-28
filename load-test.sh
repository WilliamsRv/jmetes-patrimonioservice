#!/bin/bash
set -e

JAR="target/patrimonio-service-0.0.1-SNAPSHOT.jar"
RESULTS="jmeter/results"
JMETER="/opt/apache-jmeter-5.6.3/bin/jmeter"

mkdir -p "$RESULTS"

echo "=== INICIANDO MICROSERVICIO CON H2 ==="
java -jar "$JAR" --spring.profiles.active=loadtest > "$RESULTS/service.log" 2>&1 &
SERVICE_PID=$!
echo "$SERVICE_PID" > service.pid
echo "PID: $SERVICE_PID"

echo "Esperando servicio en puerto 5003..."
for i in $(seq 1 30); do
    if curl -s -o /dev/null http://localhost:5003/api/v1/assets 2>/dev/null; then
        echo "SERVICIO LISTO en intento $i"
        break
    fi
    sleep 2
done

# Verificar que el servicio este vivo
if ! kill -0 $SERVICE_PID 2>/dev/null; then
    echo "ERROR: El servicio YA NO esta corriendo!"
    echo "=== ULTIMAS LINEAS DEL LOG ==="
    tail -30 "$RESULTS/service.log"
    exit 1
fi

echo "=== VERIFICANDO CON CURL ==="
curl -s -I http://localhost:5003/api/v1/assets 2>&1 | head -10

echo "=== EJECUTANDO JMETER ==="
"$JMETER" -n -t jmeter/patrimonio-load-test.jmx -l "$RESULTS/resultados.jtl" -e -o "$RESULTS/report" 2>"$RESULTS/jmeter_output.log" || true

echo "=== DETENIENDO SERVICIO ==="
kill "$SERVICE_PID" 2>/dev/null || true
wait "$SERVICE_PID" 2>/dev/null || true
echo "=== FIN ==="
