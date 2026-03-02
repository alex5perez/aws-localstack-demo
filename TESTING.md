# 🧪 Guía de Testing Diaria

Esta guía te ayuda a probar tu aplicación cada día después de hacer commits.

---

## 📋 Checklist Rápido (5 minutos)

```bash
# 1. ¿LocalStack está corriendo?
docker ps | Select-String localstack

# 2. ¿La app compila?
mvn clean compile

# 3. ¿La app arranca?
mvn spring-boot:run

# 4. ¿El health check responde?
curl http://localhost:8080/api/documents/health
```

---

## 🚀 Paso a Paso Completo

### **Paso 1: Levantar LocalStack** (solo la primera vez del día)

```powershell
# Ir al directorio del proyecto
cd C:\Users\alexp\Documents\Proyecto2026Feb

# Levantar LocalStack con docker-compose
docker-compose up -d

# Verificar que está corriendo
docker ps

# Deberías ver algo como:
# CONTAINER ID   IMAGE                        STATUS
# abc123         localstack/localstack        Up 2 seconds
```

**Si da error**:
```powershell
# Ver logs de LocalStack
docker logs localstack-s3

# Si no existe, crear desde cero:
docker-compose down
docker-compose up -d
```

---

### **Paso 2: Compilar el Proyecto**

```powershell
# Limpiar y compilar (asegura que no hay errores)
mvn clean compile

# Resultado esperado:
# [INFO] BUILD SUCCESS
```

**Si da error**:
- Lee el error completo
- Normalmente es un error de sintaxis en el código
- Busca "error:" en el output

---

### **Paso 3: Ejecutar la Aplicación**

```powershell
# Ejecutar Spring Boot
mvn spring-boot:run

# La aplicación arranca en unos 10-15 segundos
# Verás logs como:
# Started DocumentManagementApplication in 12.345 seconds
# Tomcat started on port(s): 8080
```

**Mantén esta terminal abierta** (la app corre aquí)

**Para detener la app**: `Ctrl + C`

---

### **Paso 4: Probar los Endpoints** (en otra terminal)

Abre **otra terminal PowerShell** y prueba:

#### **4.1 Health Check** ✅
```powershell
curl http://localhost:8080/api/documents/health

# Resultado esperado:
# Document Management Service is running!
```

#### **4.2 Listar Documentos** (vacío al inicio)
```powershell
curl http://localhost:8080/api/documents

# Resultado esperado:
# []
```

#### **4.3 Subir un Documento** 📤

**Primero, crea un archivo de prueba**:
```powershell
# Crear archivo de prueba
echo "Este es un documento de prueba" > test.txt
```

**Subir el archivo**:
```powershell
curl -X POST http://localhost:8080/api/documents `
  -F "file=@test.txt" `
  -F "name=Documento de Prueba"

# Resultado esperado (JSON):
# {
#   "id": "abc-123-def-456",
#   "name": "Documento de Prueba",
#   "fileName": "test.txt",
#   "contentType": "text/plain",
#   "size": 32,
#   "s3Key": "documents/abc-123-def-456-test.txt",
#   "uploadDate": "2026-03-02T..."
# }
```

**Guarda el ID** que te devuelve (ej: `abc-123-def-456`)

#### **4.4 Listar Documentos** (ahora debe aparecer)
```powershell
curl http://localhost:8080/api/documents

# Ahora debería aparecer tu documento
```

#### **4.5 Descargar un Documento** 📥
```powershell
# Reemplaza {ID} con el ID que te devolvió el upload
curl http://localhost:8080/api/documents/{ID}/download -o descargado.txt

# Verifica que se descargó
Get-Content descargado.txt
```

#### **4.6 Eliminar un Documento** 🗑️
```powershell
# Reemplaza {ID} con el ID del documento
curl -X DELETE http://localhost:8080/api/documents/{ID}

# Resultado esperado: No devuelve nada (HTTP 204)

# Verificar que ya no existe
curl http://localhost:8080/api/documents
# Debería volver a estar vacío: []
```

---

## ⚠️ Probar Validaciones (lo nuevo de hoy)

### **Test 1: Archivo demasiado grande**
```powershell
# Crear archivo de 15MB (supera el límite de 10MB)
fsutil file createnew archivo_grande.txt 15728640

# Intentar subirlo
curl -X POST http://localhost:8080/api/documents `
  -F "file=@archivo_grande.txt" `
  -F "name=Archivo Grande"

# Resultado esperado (error 413):
# {
#   "timestamp": "2026-03-02T...",
#   "status": 413,
#   "error": "Payload Too Large",
#   "message": "File size exceeds the maximum allowed (10MB)",
#   "path": "/api/documents"
# }

# Limpiar
Remove-Item archivo_grande.txt
```

### **Test 2: Archivo vacío**
```powershell
# Crear archivo vacío
New-Item -Path "vacio.txt" -ItemType File

# Intentar subirlo
curl -X POST http://localhost:8080/api/documents `
  -F "file=@vacio.txt" `
  -F "name=Archivo Vacio"

# Resultado esperado (error 400):
# {
#   "status": 400,
#   "error": "Bad Request",
#   "message": "File cannot be empty"
# }

# Limpiar
Remove-Item vacio.txt
```

### **Test 3: Tipo de archivo no permitido**
```powershell
# Crear un archivo .exe simulado
echo "fake exe" > malicioso.exe

# Intentar subirlo
curl -X POST http://localhost:8080/api/documents `
  -F "file=@malicioso.exe" `
  -F "name=Archivo Malicioso"

# Resultado esperado (error 400):
# {
#   "status": 400,
#   "error": "Bad Request",
#   "message": "File type '...' is not allowed..."
# }

# Limpiar
Remove-Item malicioso.exe
```

### **Test 4: Documento inexistente**
```powershell
# Intentar descargar un documento que no existe
curl http://localhost:8080/api/documents/id-falso-123/download

# Resultado esperado (error 404):
# {
#   "status": 404,
#   "error": "Not Found",
#   "message": "Document not found with ID: id-falso-123"
# }
```

---

## 🎯 Testing Rápido Diario (Después de cada commit)

**Usa este script rápido** (copia y pega todo):

```powershell
# 1. Health check
Write-Host "`n=== HEALTH CHECK ===" -ForegroundColor Cyan
curl http://localhost:8080/api/documents/health

# 2. Crear archivo de prueba
Write-Host "`n=== CREATING TEST FILE ===" -ForegroundColor Cyan
echo "Test content" > daily-test.txt

# 3. Subir documento
Write-Host "`n=== UPLOADING DOCUMENT ===" -ForegroundColor Cyan
$response = curl -X POST http://localhost:8080/api/documents `
  -F "file=@daily-test.txt" `
  -F "name=Daily Test" | ConvertFrom-Json

$docId = $response.id
Write-Host "Document ID: $docId" -ForegroundColor Green

# 4. Listar documentos
Write-Host "`n=== LISTING DOCUMENTS ===" -ForegroundColor Cyan
curl http://localhost:8080/api/documents

# 5. Descargar
Write-Host "`n=== DOWNLOADING DOCUMENT ===" -ForegroundColor Cyan
curl "http://localhost:8080/api/documents/$docId/download" -o downloaded.txt
Write-Host "Content:" -ForegroundColor Yellow
Get-Content downloaded.txt

# 6. Eliminar
Write-Host "`n=== DELETING DOCUMENT ===" -ForegroundColor Cyan
curl -X DELETE "http://localhost:8080/api/documents/$docId"

# 7. Verificar que está vacío
Write-Host "`n=== VERIFYING DELETION ===" -ForegroundColor Cyan
curl http://localhost:8080/api/documents

# Limpiar
Remove-Item daily-test.txt, downloaded.txt

Write-Host "`n=== ALL TESTS PASSED ✓ ===" -ForegroundColor Green
```

Guarda esto en un archivo `test-daily.ps1` y ejecútalo con:
```powershell
.\test-daily.ps1
```

---

## 🛠️ Comandos Útiles

### **Ver logs de la aplicación en tiempo real**
```powershell
# Los logs aparecen en la terminal donde ejecutaste mvn spring-boot:run
# Busca líneas como:
# INFO  - Uploading document: ...
# ERROR - Document not found: ...
```

### **Ver qué está corriendo en el puerto 8080**
```powershell
netstat -ano | Select-String "8080"
```

### **Detener LocalStack**
```powershell
docker-compose down
```

### **Ver logs de LocalStack**
```powershell
docker logs localstack-s3
```

### **Reiniciar todo**
```powershell
# 1. Detener la app (Ctrl+C en su terminal)
# 2. Detener LocalStack
docker-compose down
# 3. Limpiar y reiniciar
docker-compose up -d
mvn clean spring-boot:run
```

---

## ❌ Problemas Comunes

### **Error: "Connection refused" o "Cannot connect to S3"**
**Causa**: LocalStack no está corriendo
**Solución**:
```powershell
docker-compose up -d
docker ps  # verificar que está corriendo
```

### **Error: "Port 8080 already in use"**
**Causa**: La aplicación ya está corriendo en otra terminal
**Solución**:
```powershell
# Encontrar el proceso
netstat -ano | Select-String "8080"
# Matar el proceso (reemplaza PID con el número que viste)
taskkill /PID <PID> /F
```

### **Error: "mvn: command not found"**
**Causa**: Maven no está en el PATH
**Solución**:
```powershell
# Verificar instalación de Maven
mvn -version
# Si no está instalado, instalar desde: https://maven.apache.org/download.cgi
```

### **Error al compilar: "package does not exist"**
**Causa**: Dependencias no descargadas
**Solución**:
```powershell
mvn clean install -U
```

---

## 📊 Checklist de Validación

Después de cada commit, verifica:

- [ ] ✅ LocalStack corriendo
- [ ] ✅ Aplicación compila sin errores
- [ ] ✅ Aplicación arranca correctamente
- [ ] ✅ Health check responde
- [ ] ✅ Puedo subir un documento válido
- [ ] ✅ Puedo listar documentos
- [ ] ✅ Puedo descargar un documento
- [ ] ✅ Puedo eliminar un documento
- [ ] ✅ Archivos inválidos son rechazados (nuevo hoy)
- [ ] ✅ Errores devuelven JSON estructurado (nuevo hoy)

---

## 🎯 Objetivo Diario

**Antes de hacer push cada día**:
1. Ejecuta el script de testing rápido
2. Verifica que todo funciona
3. Si algo falla, arréglalo antes de hacer commit
4. Solo haz push cuando todo esté verde ✅

**Tiempo estimado**: 3-5 minutos por día

---

## 💡 Tips Pro

1. **Deja LocalStack corriendo todo el día** (no lo detengas entre pruebas)
2. **Usa Postman** si prefieres interfaz gráfica en lugar de curl
3. **Lee los logs** cuando algo falle - ahí está la respuesta
4. **Prueba casos extremos** no solo el "happy path"

---

¡Guarda este archivo y úsalo cada día! 📖
