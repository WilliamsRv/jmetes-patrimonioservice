# ==============================================================
# ETAPA 1: Compilacion del JAR con Maven
# ==============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# 1. Solo el pom.xml para cachear dependencias (layer independiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Codigo fuente y compilacion
COPY src ./src
RUN mvn clean package -DskipTests


# ==============================================================
# ETAPA 2: JRE minimo con jlink (solo los modulos que necesita la app)
# ==============================================================
FROM eclipse-temurin:17-jdk-alpine AS jlink

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# 1. Extraemos el JAR y detectamos modulos JDK requeridos con jdeps
RUN jar xf app.jar && \
    MODULES=$(jdeps --ignore-missing-deps -q --recursive --multi-release 17 \
      -cp 'BOOT-INF/lib/*' --print-module-deps BOOT-INF/classes/ 2>/dev/null) && \
    echo "Modulos detectados por jdeps: $MODULES" && \
    echo "$MODULES" > /tmp/modules.txt && \
    echo "Agregando modulos extra necesarios en runtime..." && \
    echo "$MODULES,java.sql,java.xml,java.naming,jdk.crypto.ec,jdk.zipfs" | tr ',' '\n' | sort -u \
      | grep -v '^[[:space:]]*$' | tr '\n' ',' | sed 's/,$//' > /tmp/final-modules.txt && \
    echo "Modulos finales: $(cat /tmp/final-modules.txt)"

# 2. Generamos JRE custom con solo esos modulos (~55MB vs ~180MB del JRE completo)
RUN jlink --add-modules $(cat /tmp/final-modules.txt) \
    --strip-debug --no-header-files --no-man-pages \
    --compress=2 --output /jre


# ==============================================================
# ETAPA 3: Imagen final minima con Alpine + JRE custom + JAR
# ==============================================================
FROM alpine:3.20

# 1. Usuario no-root para seguridad
RUN adduser -D -u 1001 appuser

# 2. Variables de entorno: JRE custom y flags de memoria
ENV JAVA_HOME=/jre
ENV PATH=$JAVA_HOME/bin:$PATH

ENV JAVA_OPTS="\
  ## Container awareness (obedece --memory de Docker) \
  -XX:+UseContainerSupport \
  ## SerialGC: liviano, ideal para 1-2 cores y <512MB RAM \
  -XX:+UseSerialGC \
  ## Heap minimo y maximo \
  -Xms48m -Xmx96m \
  ## Metaspace (clases cargadas) \
  -XX:MaxMetaspaceSize=64m \
  ## Stack de cada thread \
  -Xss256k \
  ## Desactiva ExplicitGC (System.gc() no hace nada) \
  -XX:+DisableExplicitGC \
  ## Pre-toca la memoria al iniciar (evita faltas de pagina) \
  -XX:+AlwaysPreTouch \
  ## Solo compilador C1 (desactiva C2, ahorra CPU/memoria) \
  -XX:TieredStopAtLevel=1 \
  ## Seguridad / rendimiento \
  -Djava.security.egd=file:/dev/./urandom \
  ## Desactiva JMX (no usado) \
  -Dspring.jmx.enabled=false \
  ## Desactiva Netty nativo (usa NIO puro, mas liviano) \
  -Dreactor.netty.native=false"

WORKDIR /app

# 3. Copiamos JRE custom y JAR (appuser puede leerlos, sin chown innecesario)
COPY --from=jlink /jre $JAVA_HOME
COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 5003

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
