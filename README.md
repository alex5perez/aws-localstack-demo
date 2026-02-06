# AWS LocalStack Demo 🚀

Proyecto de demostración para trabajar con **AWS S3** usando **LocalStack** y **Maven**. Ideal para aprender y practicar operaciones de S3 sin costos de AWS.

## 📋 Características

- ✅ Creación de buckets S3
- ✅ Subida de archivos
- ✅ Listado de objetos
- ✅ Descarga de archivos
- ✅ Tests de integración con JUnit 5
- ✅ AWS SDK v2 (última versión)

## 🛠️ Tecnologías

- **Java 17**
- **Maven**
- **AWS SDK v2**
- **LocalStack** (simula servicios AWS localmente)
- **JUnit 5** (testing)

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

## 🚀 Instalación y Uso

### 1. Clonar el repositorio
```bash
git clone <tu-repo>
cd aws-localstack-demo
```

### 2. Compilar el proyecto
```bash
mvn clean compile
```

### 3. Ejecutar la aplicación
```bash
mvn exec:java -Dexec.mainClass="com.alexp.aws.S3LocalStackDemo"
```

### Salida esperada:
```
=== AWS S3 LocalStack Demo ===

✓ Bucket created: demo-bucket
✓ File uploaded: test-file.txt

--- Objects in bucket 'demo-bucket': ---
  - test-file.txt (Size: 42 bytes)

--- Downloading file: test-file.txt ---
Content: Hello from LocalStack! This is a demo file.

✓ Demo completed successfully!
```

## 🧪 Ejecutar Tests

```bash
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

Ideas para extender este proyecto:

- [ ] Añadir operaciones de eliminación de objetos
- [ ] Implementar versionado de objetos
- [ ] Añadir metadatos a los archivos
- [ ] Integrar con otros servicios AWS (DynamoDB, SQS, etc.)
- [ ] Crear una API REST con Spring Boot
- [ ] Añadir manejo de archivos grandes (multipart upload)

## 🔧 Troubleshooting

### Error: "Connection refused" al ejecutar
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
