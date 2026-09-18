# Impulso Tech — Backend

> **Proyecto educativo.** Impulso Tech es una plataforma desarrollada con
> fines **académicos y de aprendizaje**, como práctica de
> arquitectura de software, buenas prácticas de ingeniería y desarrollo
> full-stack moderno.

API REST del ecosistema Impulso Tech. Provee autenticación, gestión de
cursos, rutas de aprendizaje, actividades (retos, proyectos, laboratorios
y evaluaciones), comunidad, gamificación, notificaciones y emisión de
certificados.

## Contexto y propósito

Este repositorio existe como **material de estudio**. Cada módulo,
migración y decisión de arquitectura está pensada para ilustrar
principios que se enseñan en cursos de ingeniería de software:

- Arquitectura por capas y separación de responsabilidades.
- Seguridad de aplicaciones (autenticación JWT, autorización por
  recurso, aislamiento de código no confiable).
- Persistencia con JPA/Hibernate y migraciones versionadas con Flyway.
- Diseño de APIs REST documentadas con OpenAPI.
- Pruebas unitarias y de integración incrementales.
- Despliegue en un PaaS moderno (Fly.io) contra Postgres serverless
  (Neon) y almacenamiento S3-compatible (Cloudflare R2).

Cualquier persona puede clonar el proyecto, estudiar su código y
reutilizarlo para aprender.

## Stack

- Java 21 · Spring Boot 3.3 · Spring Web · Spring Security · Spring Data
  JPA · Hibernate 6.5 · Bean Validation.
- PostgreSQL (Neon) + Flyway.
- Redis (Upstash) para caché y rate limiting.
- Cloudflare R2 para almacenamiento de archivos.
- Brevo para correo transaccional.
- Google OAuth 2.0 (Google Identity Services) como proveedor federado.
- OpenPDF para generación de certificados.
- Docker CLI como *sandbox* de ejecución de código de estudiantes.
- Springdoc OpenAPI + Swagger UI para documentación interactiva.

## Estructura por módulos

El backend es un **monolito modular** con separación por dominios de
negocio. Cada módulo mantiene su propio paquete con las capas
`controller`, `service`, `repository`, `entity`, `dto` y `mapper`.

```
tech.impulso
├── auth              Registro, login, Google, verificación, recuperación
├── users             Perfil, roles (ESTUDIANTE, INSTRUCTOR, ADMINISTRADOR)
├── courses           Cursos, módulos y publicación
├── learning-routes   Rutas de aprendizaje
├── lessons           Lecciones y su ordenamiento
├── enrollments       Inscripciones y progreso
├── activities        Marco común de actividades
├── challenges        Retos y entregas
├── labs              Laboratorios + sandbox de ejecución de código
├── projects          Proyectos con entrega por URL
├── evaluations       Evaluaciones con preguntas e intentos
├── certificates      Emisión y verificación pública (RF-047)
├── gamification      XP, niveles, rachas, insignias
├── community         Publicaciones y respuestas
├── notifications     Centro de notificaciones
├── files             Subida de archivos a R2
├── mascot            Servicio de IA y mascota
├── messaging         Mensajería entre usuarios
├── admin             Panel administrativo
└── config            Configuración transversal
```

## Requisitos

- JDK 21.
- Maven 3.9+.
- Docker (opcional) — sólo si se activa el modo `docker-cli` del sandbox.
- PostgreSQL local o cuenta de Neon.
- Cuentas opcionales: Cloudflare R2, Brevo, Google Cloud (OAuth).

## Configuración local

Variables de entorno mínimas (equivalente en PowerShell con
`$env:NOMBRE = "valor"`):

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/impulso
DATABASE_USERNAME=impulso
DATABASE_PASSWORD=impulso
JWT_SECRET=cambiar-por-un-valor-largo-y-aleatorio-de-al-menos-32-bytes
APP_CORS_ORIGINS=http://localhost:4200
GOOGLE_CLIENT_ID=<opcional, requerido para login con Google>
```

Para sembrar el primer administrador al arrancar:

```bash
APP_ADMIN_EMAIL=admin@impulso.local
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=<clave segura>
APP_ADMIN_FIRST_NAME=Admin
APP_ADMIN_LAST_NAME=Inicial
```

Correo transaccional (Brevo) — opcional en dev:

```bash
APP_MAIL_ENABLED=false
```

Sandbox de ejecución de código — por defecto `stub` (rechaza toda
ejecución); para habilitarlo con Docker local:

```bash
CODE_EXEC_MODE=docker-cli
```

## Arranque

```bash
mvn spring-boot:run
```

Por defecto queda en `http://localhost:8080`.

- Health: `GET /actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Tests

Batería completa:

```bash
mvn test
```

Un test puntual durante desarrollo:

```bash
mvn -Dtest=CertificateServiceTest test
```

La política de pruebas (§47 de `CLAUDE.md`) exige acompañar cada cambio
funcional con sus pruebas correspondientes en el mismo commit.

## Migraciones (Flyway)

Todas las migraciones viven en `src/main/resources/db/migration/` y se
aplican automáticamente al arrancar. Nunca modifiques la estructura
manualmente: agrega una nueva migración `V<n>__descripcion.sql`.

## Sandbox de ejecución de código

Los laboratorios permiten a los estudiantes ejecutar código. El proceso
de Spring **jamás** lo ejecuta directamente. En su lugar:

```
Spring Boot → CodeExecutionService → Docker container efímero
```

Cada ejecución corre en un contenedor con `--network=none`,
`--read-only`, límites de CPU/RAM/PIDs, usuario sin privilegios y
timeout aplicado. Ver [`DockerCliCodeExecutionService`](src/main/java/tech/impulso/labs/service/DockerCliCodeExecutionService.java).

## Certificados

Al completar un curso cuyo campo `generatesCertificate` es `true`, el
backend emite un certificado idempotente asociado al estudiante. Se
expone verificación pública por código UUID y descarga en PDF generada
on-the-fly. Ver módulo [`certificates/`](src/main/java/tech/impulso/certificates/).

## Despliegue

El backend se despliega en Fly.io. La configuración está en
[`fly.toml`](fly.toml). Neon opera en modo *serverless suspend*, por lo
que el pool de Hikari está afinado para tolerar el arranque en frío
(ver comentarios en `application.yml`).

## Licencia y uso

Proyecto **educativo, sin fines de lucro**. Puedes clonarlo,
estudiarlo, ejecutarlo localmente y usarlo como referencia para tu
propio aprendizaje.
