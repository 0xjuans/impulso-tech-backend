# syntax=docker/dockerfile:1.7
# =============================================================================
# Dockerfile del backend de Impulso Tech.
#
# Utiliza una construcción multi-etapa para producir una imagen ligera de
# ejecución. La etapa `build` compila el proyecto con Maven usando Eclipse
# Temurin 21, aprovechando el cache de dependencias entre invocaciones; la
# etapa `runtime` sólo copia el JAR y lo ejecuta con un JRE reducido.
#
# La aplicación escucha en el puerto expuesto por la variable de entorno
# SERVER_PORT (Railway lo asigna en $PORT y el compose la mapea a 8080).
# =============================================================================

# --- Etapa de construcción ---------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache de dependencias: copiamos el pom.xml primero para que la resolución
# se reutilice cuando el código fuente cambia sin tocar dependencias.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline

# Compilación del proyecto. Los tests se ejecutan en el pipeline de CI antes
# del despliegue para no bloquear la construcción de la imagen.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests package \
    && cp target/*.jar /workspace/app.jar

# --- Etapa de ejecución ------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuario sin privilegios para reducir la superficie de ataque.
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080
EXPOSE 8080

# Railway inyecta $PORT; lo redirigimos a SERVER_PORT que ya está cableado en
# application.yml. Cuando no lo define (p. ej. docker run local) usa 8080.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-$SERVER_PORT} -jar /app/app.jar"]
