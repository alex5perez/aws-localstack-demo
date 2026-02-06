# Document Management System 📁

Sistema de gestión documental con **Spring Boot**, **AWS S3** y **LocalStack**. API REST completa para subir, listar, descargar y eliminar documentos en S3.

## 📋 Características

- ✅ **API REST** completa con Spring Boot
- ✅ Subida de documentos a S3
- ✅ Listado de documentos con metadatos
- ✅ Descarga de archivos
- ✅ Eliminación de documentos
- ✅ Arquitectura en capas (Controller → Service → S3)
- ✅ LocalStack para desarrollo sin costos AWS
- ✅ Tests de integración con JUnit 5
- ✅ Logging con SLF4J

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.2.2** (Framework backend)
- **Maven** (Gestión de dependencias)
- **AWS SDK v2** (Cliente S3)
- **LocalStack** (Simula AWS S3 localmente)
- **Lombok** (Reduce boilerplate code)
- **JUnit 5** (Testing)

## 📦 Requisitos Previos

### 1. Java 17 o superior
```bash
java -version
```

### 2. Maven
```bash
mvn -version
```

### 3. Docker (para LocalStack)
```bash
docker --version
```

### 4. LocalStack
Instalar y correr LocalStack con Docker:

```bash
# Opción 1: Con Docker directamente
docker run -d --name localstack -p 4566:4566 -e SERVICES=s3 localstack/localstack

# Opción 2: Con Docker Compose (crear docker-compose.yml)
docker-compose up -d
```

**Archivo docker-compose.yml** (opcional):
```yaml
version: '3.8'
services:
  localstack:
    image: localstack/localstack
    ports:
      - "4566:4566"
    environment:
      - SERVICES=s3
      - DEBUG=1
    volumes:
      - "./localstack-data:/tmp/localstack"
```
https://github.com/alex5perez/aws-localstack-demo.git
cd aws-localstack-demo
```

### 2. Levantar LocalStack
```bash
docker-compose up -d
```

### 3. Compilar el proyecto
```bash
mvn clean install
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

La API estará disponible en: **http://localhost:8080**

---

## 📡 Endpoints de la API

### 🔹 Health Check
```bash
GET /api/documents/health
```
```bash
curl http://localhost:8080/api/documents/health
```

### 🔹 Subir Documento
```bash
POST /api/documents
```
```bash
curl -X POST http://localhost:8080/api/documents \
  -F "file=@documento.pdf" \
  -F "name=Factura Enero 2024"
```

**Respuesta:**
```json
{
  "id": "abc-123-def",
  "name": "Factura Enero 2024",
  "fileName": "documento.pdf",
  "contentType": "application/pdf",
  "size": 245678,
  "s3Key├── java/com/alexp/aws/
│   │   │   ├── DocumentManagementApplication.java  # Clase principal
│   │   │   ├── controller/
│   │   │   │   └── DocumentController.java         # Endpoints REST
│   │   │   ├── service/
│   │   │   │   └── DocumentService.java            # Lógica de negocio
│   │   │   ├── config/
│   │   │   │   └── S3Config.java                   # Configuración S3
│   │   │   └── model/
│   │   │       └── Document.java                   # Modelo de datos
│   │   └── resources/
│   │       └── application.properties              # Configuración
│   └── test/
│       └── java/com/alexp/aws/
Mejoras planeadas para próximas versiones:

- [ ] **Autenticación JWT**: Añadir seguridad con Spring Security
- [ ] **Base de datos**: Guardar metadatos en PostgreSQL/DynamoDB
- [ ] **Búsqueda**: Implementar búsqueda por nombre, fecha, tipo
- [ ] **Versionado**: Control de versiones de documentos
- [ ] **Swagger/OpenAPI**: Documentación automática de la API
- [ ] **Docker**: Dockerfile y docker-compose completo
- [ ] **Tests avanzados**: Tests de integración completos
- [ ] **Paginación**: Listar documentos con paginación
```
HTTP Request
     ↓
┌─────────────────────┐
│  DocumentController │  ← Capa de presentación (REST API)
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│  DocumentService    │  ← Capa de lógica de negocio
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│  S3Client (AWS SDK) │  ← Capa de acceso a datos
└──────────┬──────────┘
           ↓
      LocalStack S3
curl http://localhost:8080/api/documents
```

**Respuesta:**
```json
[
  {
    "id": "abc-123-def",
    "fileName": "abc-123-def-documento.pdf",
    "s3Key": "documents/abc-123-def-documento.pdf",
    "size": 245678,
    "uploadDate": "2026-02-06T15:30:00"
  }
]
```

### 🔹 Descargar Documento
```bash
GET /api/documents/{id}/download
```
```bash
curl http://localhost:8080/api/documents/abc-123-def/download -o archivo.pdf
```

### 🔹 Eliminar Documento
```bash
DELETE /api/documents/{id}
```
```bash
curl -X DELETE http://localhost:8080/api/documents/abc-123-def
```

---

## 🧪 Ejecutar Tests

```bash
# Todos los tests
mvn test

# Con cobertura
mvn test jacoco:repor
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=S3LocalStackDemoTest
```

## 📁 Estructura del Proyecto

```
aws-localstack-demo/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/alexp/aws/
│   │           └── S3LocalStackDemo.java
│   └── test/
│       └── java/
│           └── com/alexp/aws/
│               └── S3LocalStackDemoTest.java
├── pom.xml
├── README.md
└── docker-compose.yml (opcional)
```

## 🎯 Próximos Pasos
� Conceptos Aprendidos

Este proyecto demuestra:
- ✅ Arquitectura en capas (Controller-Service-Client)
- ✅ Inyección de dependencias con Spring
- ✅ Configuración externalizada con `application.properties`
- ✅ API REST con Spring Boot
- ✅ Integración con AWS S3
- ✅ Testing en entornos locales con LocalStack
- ✅ Uso de Lombok para reducir código boilerplate
- ✅ Manejo de archivos con `MultipartFile`
- ✅ Logging con SLF4J

---

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 👤 Autor

**Alex Pérez**
- GitHub: [@alex5perez](https://github.com/alex5perez)
- Portfolio profesional mostrando dominio de Java, Spring Boot, AWS y arquitecturas modernas

---

⭐ **Si este proyecto te fue útil, considera darle una estrella en GitHub!**
**Solución**: Verifica que LocalStack esté corriendo:
```bash
docker ps | grep localstack
```

### Error: "Unable to execute HTTP request"
**Solución**: Asegúrate de que el puerto 4566 esté disponible y LocalStack esté activo.

### Los tests fallan
**Solución**: LocalStack debe estar corriendo antes de ejecutar los tests.

## 📚 Recursos

- [AWS SDK for Java v2 - Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [LocalStack Documentation](https://docs.localstack.cloud/overview/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 👤 Autor

**Alex P**
- Portfolio profesional mostrando dominio de Java, Maven, AWS y DevOps
- GitHub: [Tu perfil]

---

⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub!
