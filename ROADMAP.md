# 🗺️ Roadmap de Evolución Profesional
## Document Management System - Enterprise Grade

> **Objetivo**: Convertir este proyecto en un sistema profesional que demuestre arquitectura, integración y buenas prácticas nivel empresa.

---

## 📊 Estado Actual vs Objetivo

| Aspecto | Estado Actual | Objetivo Final |
|---------|---------------|----------------|
| **Testing** | 1 test básico | >80% cobertura + tests de integración |
| **CI/CD** | ❌ No existe | GitHub Actions + quality gates |
| **Base de Datos** | ❌ Solo memoria | PostgreSQL + JPA |
| **Seguridad** | ❌ Sin auth | JWT + Spring Security + roles |
| **Documentación** | README extenso | README + Swagger + Diagramas |
| **Observabilidad** | Logs básicos | Actuator + Métricas + Health checks |
| **Deployment** | Local only | Docker multi-stage + cloud ready |

---

## 🎯 SPRINT 1: Testing & CI/CD (PRIORIDAD MÁXIMA)
**Duración**: 1-2 días  
**Por qué primero**: Preguntan en 80% de entrevistas backend

### Tareas

#### 1.1 Tests Unitarios con Mocks
- [ ] `DocumentServiceTest` - Tests con Mockito
  - ✅ Test upload documento válido
  - ✅ Test validaciones (archivo vacío, muy grande, tipo inválido)
  - ✅ Test crear bucket si no existe
  - ✅ Test manejo de excepciones S3
  
- [ ] `DocumentControllerTest` - Tests con MockMvc
  - ✅ Test POST /api/documents (201 Created)
  - ✅ Test GET /api/documents (200 OK)
  - ✅ Test GET /api/documents/{id}/download (200 OK)
  - ✅ Test DELETE /api/documents/{id} (204 No Content)
  - ✅ Test validaciones devuelven 400
  - ✅ Test documento no encontrado devuelve 404

#### 1.2 Tests de Integración
- [ ] `DocumentIntegrationTest` - Tests end-to-end con LocalStack
  - ✅ Test flujo completo: upload → list → download → delete
  - ✅ Test múltiples archivos
  - ✅ Test concurrencia

#### 1.3 Cobertura de Código
- [ ] Configurar JaCoCo en `pom.xml`
- [ ] Generar reporte de cobertura
- [ ] Objetivo: >80% cobertura en service/controller

#### 1.4 GitHub Actions Pipeline
- [ ] Crear `.github/workflows/ci.yml`
  - ✅ Build con Maven
  - ✅ Run tests
  - ✅ Generate coverage report
  - ✅ Fail if coverage < 80%
  - ✅ Integration tests con LocalStack
  - ✅ Build Docker image

**Resultado Sprint 1**: Badge verde en GitHub + tests profesionales

---

## 🗄️ SPRINT 2: Persistencia & Búsqueda
**Duración**: 2-3 días  
**Por qué**: "¿Dónde guardas los metadatos?" es pregunta común

### Tareas

#### 2.1 Base de Datos
- [ ] Añadir PostgreSQL al `docker-compose.yml`
- [ ] Configurar Spring Data JPA
- [ ] Crear entidad `DocumentEntity` con JPA annotations
  ```java
  @Entity
  @Table(name = "documents")
  class DocumentEntity {
      @Id UUID id;
      String name;
      String s3Key;
      Long size;
      String contentType;
      LocalDateTime uploadDate;
      String uploadedBy; // Para sprint 3
      List<String> tags; // Para búsqueda
  }
  ```
- [ ] Crear `DocumentRepository extends JpaRepository`

#### 2.2 Migraciones con Flyway
- [ ] Configurar Flyway en `pom.xml`
- [ ] Crear `V1__create_documents_table.sql`
- [ ] Crear `V2__add_tags_column.sql`

#### 2.3 Paginación y Ordenamiento
- [ ] Endpoint GET `/api/documents?page=0&size=10&sort=uploadDate,desc`
- [ ] Usar `Pageable` de Spring Data
- [ ] Devolver `Page<Document>` con metadatos de paginación

#### 2.4 Búsqueda y Filtrado
- [ ] Endpoint GET `/api/documents/search?name=factura&type=pdf&from=2026-01-01&to=2026-03-01`
- [ ] Implementar `@Query` con JPQL o Specifications
- [ ] Filtros: nombre, tipo, rango de fechas, tags

**Resultado Sprint 2**: Persistencia real + búsqueda avanzada

---

## 🔒 SPRINT 3: Seguridad & Autenticación
**Duración**: 2-3 días  
**Por qué**: Sistema sin auth no se puede usar en producción

### Tareas

#### 3.1 Spring Security + JWT
- [ ] Añadir Spring Security y JWT dependencies
- [ ] Crear `User` entity (id, username, email, password, role)
- [ ] Crear `UserRepository`
- [ ] Crear `AuthController` con endpoints:
  - POST `/api/auth/register` - Registrar usuario
  - POST `/api/auth/login` - Login y devolver JWT
  
#### 3.2 JWT Configuration
- [ ] Crear `JwtTokenProvider` para generar/validar tokens
- [ ] Crear `JwtAuthenticationFilter` que valida token en cada request
- [ ] Configurar `SecurityConfig` con rutas públicas/privadas

#### 3.3 Autorización por Roles
- [ ] Roles: `ADMIN`, `USER`
- [ ] USER puede:
  - ✅ Subir documentos
  - ✅ Ver solo SUS documentos
  - ✅ Descargar solo SUS documentos
  - ✅ Eliminar solo SUS documentos
- [ ] ADMIN puede:
  - ✅ Ver TODOS los documentos
  - ✅ Eliminar cualquier documento
  - ✅ Ver estadísticas del sistema

#### 3.4 Auditoría
- [ ] Guardar `uploadedBy` (username) en DocumentEntity
- [ ] Guardar `createdAt`, `updatedAt`, `deletedAt` (soft delete)
- [ ] Usar `@CreatedBy`, `@CreatedDate` de Spring Data JPA

**Resultado Sprint 3**: Sistema seguro con usuarios y roles

---

## ⚡ SPRINT 4: Features Avanzadas
**Duración**: 3-4 días  
**Por qué**: Diferenciación vs otros portfolios

### Tareas

#### 4.1 Versionado de Documentos
- [ ] Tabla `document_versions` (id, documentId, versionNumber, s3Key, uploadDate)
- [ ] Endpoint POST `/api/documents/{id}/versions` - Subir nueva versión
- [ ] Endpoint GET `/api/documents/{id}/versions` - Listar versiones
- [ ] Endpoint GET `/api/documents/{id}/versions/{version}/download` - Descargar versión específica
- [ ] Marcar versión actual

#### 4.2 Tags y Categorización
- [ ] Tabla `tags` (id, name)
- [ ] Relación many-to-many `document_tags`
- [ ] Endpoint POST `/api/documents/{id}/tags` - Añadir tag
- [ ] Endpoint DELETE `/api/documents/{id}/tags/{tagId}` - Quitar tag
- [ ] Búsqueda por tags: `/api/documents?tags=urgent,invoice`

#### 4.3 Compartir Documentos
- [ ] Tabla `document_shares` (id, documentId, sharedWithUserId, permission, expiresAt)
- [ ] Endpoint POST `/api/documents/{id}/share` - Compartir con usuario
  ```json
  {
    "userId": "user-123",
    "permission": "READ", // READ, WRITE, ADMIN
    "expiresAt": "2026-12-31T23:59:59"
  }
  ```
- [ ] Endpoint GET `/api/documents/shared-with-me` - Ver docs compartidos conmigo

#### 4.4 URLs Firmadas (Pre-signed URLs)
- [ ] Endpoint POST `/api/documents/{id}/share-link` - Generar URL pública temporal
  ```json
  {
    "url": "https://s3.../document.pdf?signature=...",
    "expiresAt": "2026-03-05T12:00:00"
  }
  ```
- [ ] Usar `generatePresignedUrl()` de AWS SDK
- [ ] Expira en 1 hora por defecto

#### 4.5 Thumbnails para Imágenes
- [ ] Al subir imagen, generar thumbnail (200x200)
- [ ] Guardar en S3 como `thumbnails/{id}-thumb.jpg`
- [ ] Endpoint GET `/api/documents/{id}/thumbnail` - Ver thumbnail
- [ ] Usar librería como `thumbnailator`

**Resultado Sprint 4**: Features que impresionan en demo

---

## 📊 SPRINT 5: Observabilidad & Ops
**Duración**: 2-3 días  
**Por qué**: Demuestra conocimiento de producción

### Tareas

#### 5.1 Spring Boot Actuator
- [ ] Añadir `spring-boot-starter-actuator`
- [ ] Exponer endpoints:
  - `/actuator/health` - Estado de la aplicación
  - `/actuator/metrics` - Métricas
  - `/actuator/info` - Info del sistema
  - `/actuator/prometheus` - Métricas para Prometheus

#### 5.2 Health Checks Personalizados
- [ ] `S3HealthIndicator` - Verifica conectividad con S3
- [ ] `DatabaseHealthIndicator` - Verifica conectividad con DB
- [ ] Response:
  ```json
  {
    "status": "UP",
    "components": {
      "s3": { "status": "UP", "details": { "bucket": "documents" } },
      "db": { "status": "UP" },
      "diskSpace": { "status": "UP" }
    }
  }
  ```

#### 5.3 Métricas Personalizadas
- [ ] Counter: `documents.uploaded.total`
- [ ] Counter: `documents.downloaded.total`
- [ ] Gauge: `documents.count` (total documentos)
- [ ] Timer: `s3.upload.duration`
- [ ] Usar Micrometer `MeterRegistry`

#### 5.4 Logging Estructurado
- [ ] Configurar Logback para JSON output
- [ ] Añadir `logstash-logback-encoder`
- [ ] Logs incluyen: timestamp, level, traceId, userId, operation
- [ ] Ejemplo:
  ```json
  {
    "timestamp": "2026-03-04T10:30:00Z",
    "level": "INFO",
    "traceId": "abc-123",
    "userId": "user-456",
    "operation": "document.upload",
    "message": "Document uploaded successfully",
    "documentId": "doc-789",
    "size": 1024000
  }
  ```

#### 5.5 Docker Multi-Stage Build
- [ ] Crear `Dockerfile` optimizado
  ```dockerfile
  # Stage 1: Build
  FROM maven:3.9-eclipse-temurin-21 AS build
  WORKDIR /app
  COPY pom.xml .
  RUN mvn dependency:go-offline
  COPY src ./src
  RUN mvn clean package -DskipTests
  
  # Stage 2: Runtime
  FROM eclipse-temurin:21-jre-alpine
  WORKDIR /app
  COPY --from=build /app/target/*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```
- [ ] Imagen final <100MB

#### 5.6 Docker Compose Completo
- [ ] `docker-compose.yml` con 3 servicios:
  - `app` - Spring Boot application
  - `postgres` - Base de datos
  - `localstack` - S3 local
- [ ] Health checks para cada servicio
- [ ] Restart policies
- [ ] Volumes para persistencia

**Resultado Sprint 5**: Ready para deployment real

---

## 📚 SPRINT 6: Documentación & Presentación
**Duración**: 1-2 días  
**Por qué**: Proyecto sin documentación visual no impacta

### Tareas

#### 6.1 Swagger/OpenAPI
- [ ] Añadir `springdoc-openapi-starter-webmvc-ui`
- [ ] Configurar en `application.properties`
- [ ] Documentar cada endpoint con `@Operation`, `@ApiResponse`
- [ ] Acceder en `http://localhost:8080/swagger-ui.html`
- [ ] Incluir ejemplos de request/response

#### 6.2 Diagrama de Arquitectura
- [ ] Crear diagrama C4 (Context + Container + Component)
- [ ] Herramientas: PlantUML, Mermaid, o Excalidraw
- [ ] Incluir:
  - Usuario → Frontend → Backend → DB/S3
  - Flujos principales (upload, download, search)
  - Componentes security (JWT)
- [ ] Guardar en `docs/architecture/`

#### 6.3 Caso de Uso de Negocio Específico
- [ ] **CAMBIAR ENFOQUE**: Ya no es "demo genérico"
- [ ] **Elegir vertical**: (ejemplos abajo)

**Opción A: Sistema de Gestión Documental para PYMES**
```
Problema: PYME con 20 empleados que necesita:
- Contratos de clientes organizados
- Facturas accesibles para contabilidad
- Documentos internos (RRHH, legal)
- Compartir docs con externos

Solución: Este sistema permite:
✅ Organizar por tags (contratos, facturas, rrhh)
✅ Buscar por fecha/cliente
✅ Compartir con externos (URLs temporales)
✅ Roles (admin puede ver todo, empleados solo sus docs)
```

**Opción B: Portal de Documentos para Educación**
```
Problema: Universidad necesita:
- Estudiantes suben trabajos
- Profesores descargan y evalúan
- Material de estudio accesible 24/7
- Histórico de entregas

Solución: Este sistema permite:
✅ Estudiantes suben trabajos (límite 10MB)
✅ Profesores ven todos los trabajos de su materia
✅ Búsqueda por estudiante, fecha, materia
✅ Versionado (resubir trabajo corregido)
```

**Opción C: Sistema de Archivo Digital para Clínica**
```
Problema: Clínica médica necesita:
- Historiales médicos digitalizados
- Imágenes de diagnósticos
- Acceso rápido en consulta
- Cumplir normativa de privacidad

Solución: Este sistema permite:
✅ Médicos suben historiales/imágenes
✅ Búsqueda rápida por paciente
✅ Roles estrictos (solo médico asignado ve historial)
✅ Auditoría de accesos
```

- [ ] Actualizar README con el caso elegido
- [ ] Crear `docs/BUSINESS_CASE.md` detallado

#### 6.4 Postman Collection
- [ ] Exportar collection con todos los endpoints
- [ ] Incluir ejemplos de:
  - Registro y login
  - Upload documento
  - Búsqueda
  - Share documento
- [ ] Variables de entorno (baseUrl, token)
- [ ] Guardar en `docs/postman/`

#### 6.5 Video Demo (Opcional pero Poderoso)
- [ ] Grabar demo de 3-5 minutos
- [ ] Mostrar:
  - Login
  - Upload documento
  - Búsqueda y filtrado
  - Download
  - Dashboard de admin
- [ ] Subir a YouTube (unlisted)
- [ ] Link en README

**Resultado Sprint 6**: Presentación profesional completa

---

## 🎯 SPRINTS FUTUROS (Opcional - Diferenciación Máxima)

### SPRINT 7: Frontend (React/Vue)
- [ ] SPA con React + Vite
- [ ] Login page
- [ ] Dashboard de documentos
- [ ] Drag & drop upload
- [ ] Búsqueda en tiempo real
- [ ] Preview de PDFs/imágenes

### SPRINT 8: Microservicios
- [ ] Separar en 3 servicios:
  - `auth-service` - Autenticación
  - `document-service` - Gestión de docs
  - `notification-service` - Emails/notificaciones
- [ ] API Gateway con Spring Cloud Gateway
- [ ] Service discovery con Eureka

### SPRINT 9: Event-Driven Architecture
- [ ] Añadir RabbitMQ/Kafka
- [ ] Eventos:
  - `DocumentUploadedEvent`
  - `DocumentSharedEvent`
  - `DocumentDeletedEvent`
- [ ] Consumers:
  - Generar thumbnails async
  - Enviar notificaciones
  - Actualizar estadísticas

### SPRINT 10: Cloud Deployment
- [ ] Deploy a AWS:
  - ECS/EKS para containers
  - RDS para PostgreSQL
  - S3 real (no LocalStack)
  - CloudWatch para logs
- [ ] Terraform para IaC
- [ ] CI/CD completo con deploy a staging/prod

---

## 📈 Métricas de Éxito

### Por Sprint

| Sprint | Métrica de Éxito |
|--------|------------------|
| 1 | ✅ Tests passing en CI, cobertura >80% |
| 2 | ✅ Metadatos en DB, búsqueda funcionando |
| 3 | ✅ Login/Register funcionando, JWT válido |
| 4 | ✅ 3+ features avanzadas implementadas |
| 5 | ✅ Container levanta con docker-compose |
| 6 | ✅ Swagger accesible, diagrama en README |

### Global

- [ ] **70+ commits** (muestra evolución constante)
- [ ] **10+ PRs** con descripción (si trabajas con branches)
- [ ] **README con badges**: build passing, coverage, license
- [ ] **3+ releases** con changelog
- [ ] **Issues organizados** con labels (bug, feature, enhancement)

---

## 🎤 Preparación para Entrevistas

### Historias que Podrás Contar

**"Háblame de un proyecto complejo que hayas hecho"**
> Desarrollé un sistema de gestión documental enterprise-grade con Spring Boot, AWS S3 y PostgreSQL. Implementé autenticación JWT, búsqueda avanzada, versionado de documentos y CI/CD completo. El sistema maneja validaciones robustas, tiene +80% de cobertura de tests y usa arquitectura en capas para escalabilidad.

**"¿Cómo garantizas la calidad del código?"**
> Uso una estrategia de testing multinivel: tests unitarios con Mockito, tests de integración con Testcontainers, y CI/CD que falla si la cobertura baja de 80%. También implemento manejo de excepciones centralizado y validaciones en múltiples capas.

**"¿Experiencia con cloud?"**
> Integré AWS S3 usando el SDK v2 oficial, implementé pre-signed URLs para compartir archivos de forma segura, y usé LocalStack para desarrollo local. El código está listo para deploy en ECS/EKS sin cambios.

**"¿Cómo manejas la seguridad?"**
> Implementé Spring Security con JWT, roles basados en permisos (RBAC), y auditoría completa de acciones. Los archivos sensibles requieren autenticación, y las URLs de descarga expiran automáticamente.

### Demo en Entrevista (5 minutos)

1. **Mostrar Swagger** (30s)
2. **Login y obtener JWT** (30s)
3. **Upload documento vía Postman** (1min)
4. **Búsqueda con filtros** (1min)
5. **Ver métricas en /actuator** (30s)
6. **Mostrar código de test** (1min)
7. **Mostrar pipeline en GitHub Actions** (30s)

---

## 📝 Próximos Pasos INMEDIATOS

### Esta Semana
1. ✅ Lee este roadmap completo
2. ✅ Decide sprint por el que empezar (recomiendo: SPRINT 1)
3. ✅ Crea branch `feature/sprint-1-testing`
4. ✅ Implementa primer test unitario
5. ✅ Commit y push

### Este Mes
- Completa SPRINT 1 y 2
- Documenta en commits claros
- Crea issues en GitHub para tracking

### Próximos 3 Meses
- Completa todos los sprints 1-6
- Añade al menos 1 sprint futuro (7-10)
- Prepara demo para entrevistas

---

## 🆘 ¿Por Dónde Empezar?

**Si tienes 1 hora**: SPRINT 1.4 - GitHub Actions (mayor impacto visual)  
**Si tienes 1 día**: SPRINT 1 completo (testing + CI)  
**Si tienes 1 semana**: SPRINT 1 + 2 (testing + persistencia)  
**Si tienes 1 mes**: SPRINT 1-4 (sistema casi completo)

---

## 📌 Reglas de Oro

1. **Commits frecuentes** - 1 commit por feature pequeña
2. **Mensajes claros** - "Add JWT authentication filter" no "fix stuff"
3. **Tests primero** - No merge sin tests
4. **Documentar todo** - README actualizado cada sprint
5. **Issues organizados** - Track progress visiblemente
6. **No feature creep** - Termina sprint antes de empezar el siguiente

---

**🎯 Meta Final**: En 3 meses, este proyecto será indistinguible de un sistema comercial real.

Cuando un recruiter o tech lead vea tu GitHub, pensará:
> "Este desarrollador sabe construir sistemas complejos, completos y mantenibles. Necesitamos contratarlo."

¡Éxito! 🚀
