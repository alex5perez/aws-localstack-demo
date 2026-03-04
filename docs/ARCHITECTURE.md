# 🏗️ Arquitectura del Sistema

## Diagrama de Contexto (C4 - Nivel 1)

```mermaid
graph TB
    User[👤 Usuario<br/>PYME Employee/Admin]
    Client[📱 Cliente Externo<br/>Final Customer]
    
    System[📦 Document Management System<br/>Spring Boot Application]
    
    S3[(☁️ AWS S3<br/>Object Storage)]
    DB[(💾 PostgreSQL<br/>Metadata DB)]
    
    User -->|Upload/Download<br/>Search/Manage| System
    Client -->|Download via<br/>Temp Link| System
    System -->|Store Files| S3
    System -->|Store Metadata| DB
    
    style System fill:#4CAF50,color:#fff
    style S3 fill:#FF9800,color:#fff
    style DB fill:#2196F3,color:#fff
```

---

## Diagrama de Contenedores (C4 - Nivel 2)

```mermaid
graph TB
    subgraph "User Applications"
        Browser[🌐 Web Browser]
        Mobile[📱 Mobile App]
        Postman[🔧 API Client<br/>Postman/cURL]
    end
    
    subgraph "Document Management System"
        API[🚀 REST API<br/>Spring Boot<br/>Port 8080]
        Auth[🔐 Auth Service<br/>JWT Provider]
    end
    
    subgraph "Data Layer"
        S3[(☁️ S3 Bucket<br/>documents)]
        DB[(💾 PostgreSQL<br/>Metadata)]
        Cache[(⚡ Redis Cache<br/>Future)]
    end
    
    Browser -->|HTTPS| API
    Mobile -->|HTTPS| API
    Postman -->|HTTP/HTTPS| API
    
    API -->|Authenticate| Auth
    API -->|Store/Retrieve Files| S3
    API -->|CRUD Metadata| DB
    API -.->|Cache Queries| Cache
    
    style API fill:#4CAF50,color:#fff
    style Auth fill:#9C27B0,color:#fff
    style S3 fill:#FF9800,color:#fff
    style DB fill:#2196F3,color:#fff
    style Cache fill:#607D8B,color:#fff
```

---

## Diagrama de Componentes (C4 - Nivel 3)

```mermaid
graph TB
    subgraph "Presentation Layer"
        Controller[📡 DocumentController<br/>REST Endpoints]
        AuthController[🔐 AuthController<br/>Login/Register]
        ExceptionHandler[⚠️ GlobalExceptionHandler<br/>Error Handling]
    end
    
    subgraph "Business Logic Layer"
        Service[💼 DocumentService<br/>Business Logic]
        AuthService[🔑 AuthService<br/>User Management]
        Validator[✅ FileValidator<br/>Validation Rules]
    end
    
    subgraph "Data Access Layer"
        Repository[🗄️ DocumentRepository<br/>JPA Repository]
        UserRepo[👤 UserRepository<br/>JPA Repository]
        S3Service[☁️ S3Client<br/>AWS SDK]
    end
    
    subgraph "Infrastructure"
        Config[⚙️ S3Config<br/>Bean Configuration]
        Security[🛡️ SecurityConfig<br/>JWT Filter]
    end
    
    Controller -->|Calls| Service
    AuthController -->|Calls| AuthService
    Controller -->|Catches Exceptions| ExceptionHandler
    
    Service -->|Validates| Validator
    Service -->|Saves Metadata| Repository
    Service -->|Stores File| S3Service
    
    AuthService -->|Manages Users| UserRepo
    AuthService -->|Generates Token| Security
    
    S3Service -->|Uses| Config
    
    Repository -->|JPA| DB[(Database)]
    UserRepo -->|JPA| DB
    S3Service -->|AWS SDK| S3[(S3)]
    
    style Controller fill:#4CAF50,color:#fff
    style Service fill:#2196F3,color:#fff
    style Repository fill:#FF9800,color:#fff
    style S3Service fill:#FF5722,color:#fff
```

---

## Flujo de Datos: Upload de Documento

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant Controller as 📡 Controller
    participant Service as 💼 Service
    participant Validator as ✅ Validator
    participant Repository as 🗄️ Repository
    participant S3 as ☁️ S3 Client
    participant DB as 💾 Database
    
    User->>Controller: POST /api/documents<br/>(file + metadata)
    
    Controller->>Service: uploadDocument(file, name)
    
    Service->>Validator: validateFile(file)
    alt Invalid File
        Validator-->>Service: throw InvalidFileException
        Service-->>Controller: Exception
        Controller-->>User: 400 Bad Request
    else Valid File
        Validator-->>Service: ✅ Valid
        
        Service->>Service: Generate UUID + S3 Key
        
        Service->>S3: putObject(s3Key, fileData)
        S3-->>Service: Success
        
        Service->>Service: Create Document entity
        
        Service->>Repository: save(documentEntity)
        Repository->>DB: INSERT document
        DB-->>Repository: Saved
        Repository-->>Service: DocumentEntity
        
        Service-->>Controller: Document DTO
        Controller-->>User: 201 Created (Document)
    end
```

---

## Flujo de Datos: Download de Documento

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant Controller as 📡 Controller
    participant Service as 💼 Service
    participant Repository as 🗄️ Repository
    participant S3 as ☁️ S3 Client
    participant DB as 💾 Database
    
    User->>Controller: GET /api/documents/{id}/download
    
    Controller->>Service: downloadDocument(id)
    
    Service->>Repository: findById(id)
    Repository->>DB: SELECT * FROM documents WHERE id = ?
    DB-->>Repository: DocumentEntity
    
    alt Document Not Found
        Repository-->>Service: Optional.empty()
        Service-->>Controller: throw DocumentNotFoundException
        Controller-->>User: 404 Not Found
    else Document Found
        Repository-->>Service: DocumentEntity
        
        Service->>Service: Extract s3Key from entity
        
        Service->>S3: getObject(s3Key)
        S3-->>Service: FileBytes
        
        Service-->>Controller: byte[]
        Controller-->>User: 200 OK (Binary File)
    end
```

---

## Flujo de Autenticación JWT

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant AuthController as 🔐 AuthController
    participant AuthService as 🔑 AuthService
    participant UserRepo as 👤 UserRepository
    participant JWT as 🎫 JwtProvider
    participant DB as 💾 Database
    
    User->>AuthController: POST /api/auth/login<br/>(username, password)
    
    AuthController->>AuthService: authenticate(username, password)
    
    AuthService->>UserRepo: findByUsername(username)
    UserRepo->>DB: SELECT * FROM users WHERE username = ?
    DB-->>UserRepo: User entity
    UserRepo-->>AuthService: User
    
    alt Invalid Credentials
        AuthService-->>AuthController: throw AuthenticationException
        AuthController-->>User: 401 Unauthorized
    else Valid Credentials
        AuthService->>JWT: generateToken(user)
        JWT-->>AuthService: JWT Token
        
        AuthService-->>AuthController: TokenResponse(token, expiresIn)
        AuthController-->>User: 200 OK<br/>{token: "eyJ...", expiresIn: 3600}
    end
    
    Note over User: Subsequent requests include:<br/>Authorization: Bearer eyJ...
```

---

## Arquitectura en Capas

```mermaid
graph TB
    subgraph "Layer 1: Presentation"
        REST[REST API<br/>Controllers]
        Exception[Exception Handlers]
    end
    
    subgraph "Layer 2: Business Logic"
        Services[Services<br/>Business Rules]
        Validation[Validation<br/>File/Data Checks]
    end
    
    subgraph "Layer 3: Data Access"
        Repositories[JPA Repositories]
        S3Client[S3 Client]
    end
    
    subgraph "Layer 4: Infrastructure"
        DB[💾 PostgreSQL]
        S3Storage[☁️ S3 Storage]
    end
    
    REST --> Services
    Exception --> Services
    Services --> Validation
    Services --> Repositories
    Services --> S3Client
    Repositories --> DB
    S3Client --> S3Storage
    
    style REST fill:#4CAF50,color:#fff
    style Services fill:#2196F3,color:#fff
    style Repositories fill:#FF9800,color:#fff
    style DB fill:#9C27B0,color:#fff
    style S3Storage fill:#F44336,color:#fff
```

---

## Stack Tecnológico Detallado

| Capa | Tecnología | Propósito |
|------|-----------|-----------|
| **Framework** | Spring Boot 3.4 | Backend framework, IoC, auto-configuration |
| **Web Layer** | Spring Web MVC | REST API, request handling |
| **Security** | Spring Security + JWT | Authentication, authorization |
| **Data Access** | Spring Data JPA | ORM, repository pattern |
| **Database** | PostgreSQL 15 | Relational database for metadata |
| **Storage** | AWS S3 | Object storage for files |
| **Validation** | Jakarta Validation | Input validation |
| **Logging** | SLF4J + Logback | Structured logging |
| **Build** | Maven 3.9 | Dependency management, build |
| **Testing** | JUnit 5 + Mockito + AssertJ | Unit & integration tests |
| **Coverage** | JaCoCo | Code coverage reporting |
| **DevOps** | Docker + Docker Compose | Containerization |
| **CI/CD** | GitHub Actions | Automated testing & deployment |
| **Documentation** | Swagger/OpenAPI | API documentation |
| **Observability** | Spring Actuator + Micrometer | Health checks, metrics |

---

## Decisiones Arquitectónicas (ADRs)

### ADR-001: Arquitectura en Capas
**Decisión**: Usar arquitectura en capas (Controller → Service → Repository)  
**Justificación**: 
- ✅ Separación de responsabilidades
- ✅ Testeable (cada capa independiente)
- ✅ Mantenible y escalable
- ✅ Estándar de la industria

### ADR-002: AWS S3 para Storage
**Decisión**: Usar S3 para almacenar archivos (no filesystem local)  
**Justificación**:
- ✅ Escalabilidad infinita
- ✅ Durabilidad 99.999999999%
- ✅ CDN-ready para distribución global
- ✅ Pay-as-you-go (sin infraestructura upfront)
- ✅ LocalStack permite testing local sin costos

### ADR-003: PostgreSQL para Metadatos
**Decisión**: Usar PostgreSQL en lugar de NoSQL  
**Justificación**:
- ✅ Datos estructurados (documentos, usuarios, permisos)
- ✅ Relaciones complejas (compartidos, versionado)
- ✅ ACID compliant para auditoría
- ✅ Conocimiento amplio en la industria

### ADR-004: JWT para Autenticación
**Decisión**: Stateless authentication con JWT  
**Justificación**:
- ✅ Sin necesidad de sesiones en servidor
- ✅ Escalable horizontalmente
- ✅ Mobile-friendly
- ✅ Soporta microservicios futuros

### ADR-005: Spring Boot 3.x (no 2.x)
**Decisión**: Usar Spring Boot 3.4 (última versión)  
**Justificación**:
- ✅ Java 17+ (features modernas)
- ✅ Jakarta EE (futuro del ecosistema)
- ✅ Performance mejorado
- ✅ Soporte largo plazo

---

## Seguridad en Capas

```mermaid
graph TB
    subgraph "Security Layers"
        HTTPS[🔒 Layer 1: HTTPS<br/>Transport Security]
        JWT[🎫 Layer 2: JWT Auth<br/>Identity Verification]
        RBAC[👥 Layer 3: Role-Based Access<br/>Authorization]
        Validation[✅ Layer 4: Input Validation<br/>Data Sanitization]
        Encryption[🔐 Layer 5: S3 Encryption<br/>Data at Rest]
        Audit[📝 Layer 6: Audit Logs<br/>Compliance]
    end
    
    HTTPS --> JWT
    JWT --> RBAC
    RBAC --> Validation
    Validation --> Encryption
    Encryption --> Audit
    
    style HTTPS fill:#4CAF50,color:#fff
    style JWT fill:#2196F3,color:#fff
    style RBAC fill:#FF9800,color:#fff
    style Validation fill:#9C27B0,color:#fff
    style Encryption fill:#F44336,color:#fff
    style Audit fill:#607D8B,color:#fff
```

---

## Escalabilidad

### Horizontal Scaling

```mermaid
graph LR
    LB[⚖️ Load Balancer]
    
    subgraph "Application Tier (Auto-scaling)"
        App1[🚀 Spring Boot<br/>Instance 1]
        App2[🚀 Spring Boot<br/>Instance 2]
        App3[🚀 Spring Boot<br/>Instance 3]
    end
    
    DB[(💾 PostgreSQL<br/>RDS)]
    S3[(☁️ S3 Bucket)]
    
    LB --> App1
    LB --> App2
    LB --> App3
    
    App1 --> DB
    App2 --> DB
    App3 --> DB
    
    App1 --> S3
    App2 --> S3
    App3 --> S3
    
    style LB fill:#4CAF50,color:#fff
    style App1 fill:#2196F3,color:#fff
    style App2 fill:#2196F3,color:#fff
    style App3 fill:#2196F3,color:#fff
```

**Stateless Design** permite escalar horizontalmente:
- ✅ JWT = No sesiones en servidor
- ✅ S3 = Storage compartido
- ✅ PostgreSQL = Single source of truth

---

## Monitoreo y Observabilidad

```mermaid
graph TB
    subgraph "Application"
        App[🚀 Spring Boot]
        Actuator[📊 Actuator Endpoints]
    end
    
    subgraph "Metrics Collection"
        Micrometer[📈 Micrometer]
        Prometheus[📊 Prometheus]
    end
    
    subgraph "Visualization"
        Grafana[📉 Grafana Dashboards]
    end
    
    subgraph "Alerting"
        Alerts[🚨 Alertmanager]
        PagerDuty[📞 PagerDuty/Slack]
    end
    
    App --> Actuator
    Actuator --> Micrometer
    Micrometer --> Prometheus
    Prometheus --> Grafana
    Prometheus --> Alerts
    Alerts --> PagerDuty
    
    style App fill:#4CAF50,color:#fff
    style Prometheus fill:#E6522C,color:#fff
    style Grafana fill:#F46800,color:#fff
```

---

## Deployment Architecture

### Local Development
```mermaid
graph TB
    Dev[💻 Developer Machine]
    
    subgraph "Docker Compose"
        App[🚀 Spring Boot<br/>Container]
        LocalStack[☁️ LocalStack<br/>S3 Mock]
        DB[💾 PostgreSQL<br/>Container]
    end
    
    Dev -->|mvn spring-boot:run| App
    App --> LocalStack
    App --> DB
    
    style App fill:#4CAF50,color:#fff
    style LocalStack fill:#FF9800,color:#fff
    style DB fill:#2196F3,color:#fff
```

### Production (AWS)
```mermaid
graph TB
    Internet[🌐 Internet]
    
    subgraph "AWS Cloud"
        ALB[⚖️ Application<br/>Load Balancer]
        
        subgraph "ECS Cluster"
            Task1[🚀 ECS Task 1]
            Task2[🚀 ECS Task 2]
        end
        
        RDS[(💾 RDS PostgreSQL<br/>Multi-AZ)]
        S3[(☁️ S3 Bucket<br/>Regional)]
        CloudWatch[📊 CloudWatch<br/>Logs/Metrics]
    end
    
    Internet --> ALB
    ALB --> Task1
    ALB --> Task2
    Task1 --> RDS
    Task2 --> RDS
    Task1 --> S3
    Task2 --> S3
    Task1 --> CloudWatch
    Task2 --> CloudWatch
    
    style ALB fill:#4CAF50,color:#fff
    style Task1 fill:#2196F3,color:#fff
    style Task2 fill:#2196F3,color:#fff
    style RDS fill:#FF9800,color:#fff
    style S3 fill:#F44336,color:#fff
```

---

## Próxima Evolución: Microservicios

```mermaid
graph TB
    Gateway[🚪 API Gateway<br/>Spring Cloud Gateway]
    
    subgraph "Microservices"
        AuthMS[🔐 Auth Service<br/>:8081]
        DocMS[📄 Document Service<br/>:8082]
        NotifyMS[📧 Notification Service<br/>:8083]
        SearchMS[🔍 Search Service<br/>:8084]
    end
    
    subgraph "Message Bus"
        Kafka[📨 Apache Kafka]
    end
    
    subgraph "Service Discovery"
        Eureka[🗺️ Eureka Server]
    end
    
    Gateway --> AuthMS
    Gateway --> DocMS
    Gateway --> NotifyMS
    Gateway --> SearchMS
    
    AuthMS --> Eureka
    DocMS --> Eureka
    NotifyMS --> Eureka
    SearchMS --> Eureka
    
    DocMS --> Kafka
    NotifyMS --> Kafka
    SearchMS --> Kafka
    
    style Gateway fill:#4CAF50,color:#fff
    style AuthMS fill:#9C27B0,color:#fff
    style DocMS fill:#2196F3,color:#fff
    style NotifyMS fill:#FF9800,color:#fff
    style Kafka fill:#F44336,color:#fff
```

---

## Conclusión

Esta arquitectura demuestra:
- ✅ **Clean Architecture** - Separación clara de capas
- ✅ **Cloud-Native** - Diseñado para la nube desde día 1
- ✅ **Scalable** - Horizontal scaling ready
- ✅ **Secure** - Múltiples capas de seguridad
- ✅ **Observable** - Metrics, logs, health checks
- ✅ **Testable** - Cada componente aislado
- ✅ **Maintainable** - Código organizado y documentado

**Este nivel de arquitectura es lo que empresas buscan en candidatos Senior.**
