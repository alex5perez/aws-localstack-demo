# Document Management System 📁

Sistema de gestión documental profesional con **Spring Boot**, **AWS S3** y **LocalStack**. API REST completa para almacenar, gestionar y distribuir documentos en la nube.

> **🚀 ¿NUEVO AQUÍ?** Lee primero: **[EMPEZAR_AQUI.md](EMPEZAR_AQUI.md)** (plan simple de 5 pasos)

---

## 🎯 ¿Qué Problema Resuelve?

### **El Desafío**
Las empresas modernas generan miles de documentos diariamente: contratos, facturas, reportes, imágenes, presentaciones. Almacenarlos en servidores locales presenta múltiples problemas:

- 📦 **Espacio limitado**: Los discos locales se llenan rápidamente
- 🔒 **Seguridad**: Archivos vulnerables a pérdida por fallos de hardware
- 🌐 **Accesibilidad**: Difícil acceder desde múltiples ubicaciones
- 💸 **Costos**: Infraestructura física costosa de mantener y escalar
- ⚡ **Escalabilidad**: Difícil manejar picos de demanda

### **La Solución**
Este sistema ofrece una **API REST moderna** que permite:

✅ **Almacenamiento en la nube** usando AWS S3 (escalable e infinito)  
✅ **Acceso vía HTTP** desde cualquier aplicación (web, móvil, desktop)  
✅ **Validaciones robustas** (tipos de archivo, tamaños, seguridad)  
✅ **Desarrollo sin costos** usando LocalStack (simula AWS localmente)  
✅ **Arquitectura profesional** lista para producción

### **Casos de Uso Reales**

🏢 **Empresa de Recursos Humanos**
- Almacenar CVs de candidatos
- Gestionar contratos de empleados
- Archivar documentos de nómina

📊 **Sistema de Facturación**
- Almacenar facturas en PDF
- Generar y guardar reportes mensuales
- Distribuir documentos a clientes

🏥 **Clínica Médica**
- Almacenar historiales médicos
- Gestionar imágenes de diagnósticos
- Archivar resultados de laboratorio

🎓 **Plataforma Educativa**
- Almacenar material de estudio (PDFs, presentaciones)
- Gestionar trabajos de estudiantes
- Distribuir certificados

---

## 📋 Características

- ✅ **API REST** completa con Spring Boot
- ✅ **Validaciones robustas**: Tipos de archivo, tamaño máximo (10MB)
- ✅ **Manejo de errores centralizado**: Respuestas JSON estructuradas
- ✅ Subida de documentos a S3
- ✅ Listado de documentos con metadatos
- ✅ Descarga de archivos
- ✅ Eliminación de documentos
- ✅ **Arquitectura en capas** (Controller → Service → S3)
- ✅ **LocalStack** para desarrollo sin costos AWS
- ✅ Tests de integración con JUnit 5
- ✅ Logging detallado con SLF4J

## 🛠️ Stack Tecnológico

### **Backend Framework**
- **Java 17** → LTS (Long Term Support), rendimiento mejorado
- **Spring Boot 3.2.2** → Framework #1 en empresas Java, autoconfiguración, microservicios ready
- **Maven** → Estándar de la industria para gestión de dependencias

### **Cloud & Storage**
- **AWS SDK v2** → Cliente oficial de AWS, mejor rendimiento que v1
- **AWS S3** → Almacenamiento escalable, 99.999999999% durabilidad
- **LocalStack** → Simula AWS localmente, desarrollo sin costos, CI/CD friendly

### **Utilities & Testing**
- **Lombok** → Reduce código repetitivo (getters, setters, builders)
- **JUnit 5** → Framework de testing moderno
- **SLF4J** → Abstracción de logging estándar

### **¿Por Qué Esta Arquitectura?**

**Spring Boot** → Es el framework backend más demandado en el mercado laboral Java  
**S3** → Estándar de facto para almacenamiento en la nube (Netflix, Airbnb, Spotify lo usan)  
**LocalStack** → Permite desarrollar sin cuenta AWS, sin costos, ideal para CI/CD  
**Capas separadas** → Mantenibilidad, testing, escalabilidad  

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

## 🛡️ Validaciones y Seguridad

El sistema implementa **validaciones robustas** para garantizar seguridad y calidad:

### **Validaciones Implementadas**

✅ **Tamaño de archivo**: Máximo 10MB  
✅ **Tipos permitidos**: PDF, Word, Excel, PowerPoint, Imágenes (JPEG, PNG, GIF)  
✅ **Archivo no vacío**: Rechaza archivos de 0 bytes  
✅ **Nombre válido**: No permite nombres vacíos o nulos  

### **Tipos de Archivo Aceptados**

| Categoría | Formatos | MIME Types |
|-----------|----------|------------|
| **Documentos** | PDF, Word (.doc/.docx), Excel (.xls/.xlsx), PowerPoint (.ppt/.pptx) | application/pdf, application/msword, etc. |
| **Imágenes** | JPEG, PNG, GIF | image/jpeg, image/png, image/gif |
| **Texto** | TXT | text/plain |

### **Ejemplos de Errores**

#### ❌ Archivo muy grande
```bash
curl -X POST http://localhost:8080/api/documents \
  -F "file=@archivo_15mb.pdf" \
  -F "name=Archivo Grande"
```

**Respuesta HTTP 413**:
```json
{
  "timestamp": "2026-03-03T10:30:00",
  "status": 413,
  "error": "Payload Too Large",
  "message": "File size exceeds the maximum allowed (10MB)",
  "path": "/api/documents"
}
```

#### ❌ Tipo de archivo no permitido
```bash
curl -X POST http://localhost:8080/api/documents \
  -F "file=@virus.exe" \
  -F "name=Ejecutable"
```

**Respuesta HTTP 400**:
```json
{
  "timestamp": "2026-03-03T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "File type 'application/x-msdownload' is not allowed. Allowed types: PDF, Word, Excel, PowerPoint, Images (JPEG, PNG, GIF)",
  "path": "/api/documents"
}
```

#### ❌ Documento no encontrado
```bash
curl http://localhost:8080/api/documents/id-inexistente/download
```

**Respuesta HTTP 404**:
```json
{
  "timestamp": "2026-03-03T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Document not found with ID: id-inexistente",
  "path": "/api/documents/id-inexistente/download"
}
```

### **Códigos de Estado HTTP**

| Código | Significado | Cuándo ocurre |
|--------|-------------|---------------|
| **200 OK** | Éxito | Operación exitosa |
| **201 Created** | Creado | Documento subido exitosamente |
| **204 No Content** | Sin contenido | Documento eliminado exitosamente |
| **400 Bad Request** | Solicitud inválida | Archivo inválido, parámetros incorrectos |
| **404 Not Found** | No encontrado | Documento no existe |
| **413 Payload Too Large** | Muy grande | Archivo excede 10MB |
| **500 Internal Server Error** | Error del servidor | Error inesperado (S3, etc.) |

---

## 🧪 Ejecutar Tests

```bash
# Todos los tests
mvn test

# Con cobertura
mvn test jacoco:report
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
│   │   ├── java/com/alexp/aws/
│   │   │   ├── DocumentManagementApplication.java  # 🚀 Clase principal Spring Boot
│   │   │   ├── controller/
│   │   │   │   └── DocumentController.java         # 🌐 Endpoints REST (API)
│   │   │   ├── service/
│   │   │   │   └── DocumentService.java            # 💼 Lógica de negocio + Validaciones
│   │   │   ├── config/
│   │   │   │   └── S3Config.java                   # ⚙️  Configuración de S3 Client
│   │   │   ├── model/
│   │   │   │   └── Document.java                   # 📦 Modelo de datos (DTO)
│   │   │   ├── dto/
│   │   │   │   └── ErrorResponse.java              # ⚠️  Respuestas de error estructuradas
│   │   │   └── exception/
│   │   │       ├── DocumentNotFoundException.java  # ❌ Excepción: documento no encontrado
│   │   │       ├── InvalidFileException.java       # ❌ Excepción: archivo inválido
│   │   │       ├── S3OperationException.java       # ❌ Excepción: error de S3
│   │   │       └── GlobalExceptionHandler.java     # 🛡️ Manejador global de errores
│   │   └── resources/
│   │       └── application.properties              # 📝 Configuración de la aplicación
│   └── test/
│       └── java/com/alexp/aws/
│           └── S3LocalStackDemoTest.java           # 🧪 Tests de integración
├── pom.xml                                         # 📦 Dependencias Maven
├── docker-compose.yml                              # 🐳 Configuración LocalStack
├── README.md                                       # 📖 Documentación
├── TESTING.md                                      # 🧪 Guía de testing
└── test-daily.ps1                                  # 🤖 Script de tests automatizado
```

### **Responsabilidad de Cada Capa**

| Capa | Clase | Responsabilidad |
|------|-------|-----------------|
| **Presentación** | `DocumentController` | Recibe peticiones HTTP, valida entrada básica, delega a Service |
| **Negocio** | `DocumentService` | Validaciones avanzadas, lógica de negocio, coordina con S3 |
| **Integración** | `S3Client` (AWS SDK) | Comunicación directa con S3/LocalStack |
| **Configuración** | `S3Config` | Configuración de beans y clientes externos |
| **Modelo** | `Document`, `ErrorResponse` | Estructuras de datos (DTOs) |
| **Excepciones** | `*Exception`, `GlobalExceptionHandler` | Manejo centralizado de errores |

## 🎯 Próximos Pasos
� Conceptos Aprendidos

Este proyecto demuestra patrones y prácticas profesionales de la industria:

### **Arquitectura y Diseño**
- ✅ **Arquitectura en capas** (Controller → Service → Client)
- ✅ **Separación de responsabilidades** (Single Responsibility Principle)
- ✅ **Inyección de dependencias** con Spring (`@Autowired`, constructores)
- ✅ **Configuración externalizada** (`application.properties`)

### **Spring Boot**
- ✅ **API REST** con `@RestController`, `@GetMapping`, `@PostMapping`, `@DeleteMapping`
- ✅ **Beans y configuración** con `@Configuration`, `@Bean`
- ✅ **Manejo global de excepciones** con `@ControllerAdvice`, `@ExceptionHandler`
- ✅ **Validación de datos** con validaciones personalizadas

### **Cloud & AWS**
- ✅ **Integración con AWS S3** usando SDK v2
- ✅ **LocalStack** para desarrollo local sin costos
- ✅ **Operaciones CRUD** en almacenamiento en la nube
- ✅ **Manejo de archivos binarios** con streams

### **Buenas Prácticas**
- ✅ **Excepciones personalizadas** para casos de negocio específicos
- ✅ **DTOs** (Data Transfer Objects) para respuestas estructuradas
- ✅ **Códigos HTTP semánticos** (200, 201, 400, 404, 413, 500)
- ✅ **Logging estratégico** con SLF4J
- ✅ **Validaciones robustas** (tamaño, tipo de archivo, datos requeridos)
- ✅ **Manejo de errores consistente** con mensajes claros

### **Java Moderno**
- ✅ **Lombok** para reducir boilerplate code (`@Data`, `@Builder`, `@Slf4j`)
- ✅ **Java 17 features** (Records, Pattern Matching potencial)
- ✅ **Streams API** para procesamiento funcional
- ✅ **Optional** para evitar NullPointerException

### **DevOps & Testing**
- ✅ **Docker** para servicios de infraestructura
- ✅ **Scripts automatizados** para testing diario
- ✅ **Maven** para build automation
- ✅ **Testing local** antes de deployment

---

## 🎓 Skills Demostradas

Este proyecto muestra competencias valoradas por empresas:

| Skill | Nivel | Evidencia en el Proyecto |
|-------|-------|--------------------------|
| **Java** | ⭐⭐⭐⭐ | Código limpio, uso de features modernas |
| **Spring Boot** | ⭐⭐⭐⭐ | API REST completa, arquitectura profesional |
| **AWS** | ⭐⭐⭐ | Integración con S3, SDK v2 |
| **REST APIs** | ⭐⭐⭐⭐ | Endpoints bien diseñados, códigos HTTP correctos |
| **Error Handling** | ⭐⭐⭐⭐ | Manejo centralizado, mensajes claros |
| **Clean Code** | ⭐⭐⭐⭐ | Código documentado, nombres descriptivos |
| **DevOps** | ⭐⭐⭐ | Docker, LocalStack, scripts automatizados |

---

## � ¿Por Qué Este Proyecto es Valioso para Empresas?

### **1. Resuelve un Problema Real**
No es un "CRUD básico" o tutorial copiado. Es una solución a una necesidad empresarial real: gestión de documentos en la nube.

### **2. Arquitectura Escalable**
La arquitectura en capas permite:
- ✅ Añadir autenticación sin romper nada
- ✅ Cambiar el almacenamiento (S3 → MinIO → Google Cloud)
- ✅ Escalar horizontalmente con microservicios
- ✅ Añadir caché fácilmente

### **3. Listo para Producción** (con mejoras)
Con pocas modificaciones, este código puede:
- ✅ Desplegarse en AWS real (ya usa el SDK oficial)
- ✅ Integrarse con frontend (React, Angular, Vue)
- ✅ Añadir autenticación JWT en 1 día
- ✅ Conectarse a base de datos para metadatos

### **4. Código Mantenible**
- ✅ Comentarios explicativos en español/inglés
- ✅ Nombres de variables descriptivos
- ✅ Responsabilidades claras por clase
- ✅ Fácil para nuevos desarrolladores entender

### **5. Buenas Prácticas de la Industria**
- ✅ Exception handling profesional
- ✅ Validaciones robustas
- ✅ Logs informativos
- ✅ Configuración externalizada
- ✅ Testing automatizable

### **6. Tecnologías Demandadas**
Según encuestas de desarrolladores 2025-2026:
- **Spring Boot**: #1 framework backend Java (usado por 60%+ empresas)
- **AWS**: #1 proveedor cloud (33% market share)
- **REST APIs**: Estándar en 95% de empresas modernas
- **Docker**: Usado en 80% de empresas tech

---

## �📄 Licencia

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
