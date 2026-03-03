# 🔧 Solución WSL/Docker - Comandos Rápidos

## ❌ Problema: "The service cannot be started" o error 0x80070422

---

## ✅ SOLUCIÓN RÁPIDA (Administrador)

### **Opción A: PowerShell como Administrador**

1. Presiona `Windows + X`
2. Selecciona **"Terminal (Admin)"** o **"PowerShell (Administrador)"**
3. Ejecuta estos comandos:

```powershell
# Habilitar el servicio WSL
Set-Service -Name WSLService -StartupType Automatic

# Iniciar el servicio
Start-Service WSLService

# Verificar que está corriendo
Get-Service WSLService
```

4. Cierra la terminal de administrador
5. En tu terminal normal, verifica:

```powershell
wsl --status
```

---

### **Opción B: Interfaz Gráfica (services.msc)**

1. Presiona `Windows + R`
2. Escribe: `services.msc` y presiona Enter
3. Busca **"WSL Service"** en la lista
4. Click derecho → **Propiedades**
5. Cambiar "Tipo de inicio" de **Deshabilitado** a **Automático**
6. Click en botón **Iniciar**
7. Click en **Aceptar**

---

## 🔍 COMANDOS DE DIAGNÓSTICO

### Verificar estado de WSL
```powershell
wsl --status
```

**Salida esperada:**
```
Distribución predeterminada: Ubuntu
Versión predeterminada: 2
```

---

### Verificar servicios WSL
```powershell
Get-Service | Where-Object {$_.Name -like "*wsl*" -or $_.Name -like "*lxss*"} | Select-Object Name, Status, StartType
```

**Salida esperada:**
```
Name        Status  StartType
----        ------  ---------
WSLService  Running Automatic
```

---

### Verificar Docker
```powershell
docker --version
docker ps
```

---

## 🐳 INICIAR LOCALSTACK (Después de arreglar)

### Paso 1: Levantar LocalStack
```powershell
cd C:\Users\alexp\Documents\Proyecto2026Feb
docker-compose up -d
```

### Paso 2: Verificar que está corriendo
```powershell
docker ps
```

**Deberías ver:**
```
CONTAINER ID   IMAGE                        STATUS
abc123         localstack/localstack        Up X seconds
```

### Paso 3: Ver logs (si hay problemas)
```powershell
docker logs localstack-s3
```

---

## 🚀 COMANDOS COMPLETOS DEL PROYECTO

### 1. Levantar LocalStack
```powershell
docker-compose up -d
```

### 2. Verificar LocalStack
```powershell
docker ps | Select-String localstack
```

### 3. Compilar proyecto
```powershell
mvn clean compile
```

### 4. Ejecutar aplicación (Terminal 1 - mantener abierta)
```powershell
mvn spring-boot:run
```

### 5. Ejecutar tests (Terminal 2 - mientras la app corre)
```powershell
.\test-daily.ps1
```

### 6. Test manual rápido
```powershell
# Health check
curl http://localhost:8080/api/documents/health

# Listar documentos
curl http://localhost:8080/api/documents
```

---

## 🛑 DETENER TODO

### Detener la aplicación
En la terminal donde corre `mvn spring-boot:run`:
- Presiona `Ctrl + C`

### Detener LocalStack
```powershell
docker-compose down
```

### Detener Docker Desktop
- Click derecho en el ícono de Docker (bandeja del sistema)
- Seleccionar "Quit Docker Desktop"

---

## 🔄 REINICIAR TODO DESDE CERO

```powershell
# 1. Detener todo
docker-compose down

# 2. Limpiar (opcional)
docker system prune -f

# 3. Levantar de nuevo
docker-compose up -d

# 4. Verificar
docker ps

# 5. Ejecutar app
mvn spring-boot:run
```

---

## ⚠️ PROBLEMAS COMUNES

### Error: "Port 8080 already in use"
```powershell
# Encontrar qué está usando el puerto
netstat -ano | Select-String "8080"

# Matar el proceso (reemplaza PID con el número que viste)
taskkill /PID <PID> /F
```

### Error: "Connection refused" al hacer curl
**Causa**: La aplicación no está corriendo
**Solución**: Ejecuta `mvn spring-boot:run` en otra terminal

### Error: LocalStack no responde
```powershell
# Ver logs
docker logs localstack-s3

# Reiniciar LocalStack
docker-compose down
docker-compose up -d
```

### Error: Maven no encontrado
```powershell
# Verificar instalación
mvn -version

# Si no está instalado, descargar desde:
# https://maven.apache.org/download.cgi
```

---

## 📝 CHECKLIST DIARIO

Antes de empezar a trabajar cada día:

- [ ] Docker Desktop está corriendo (ícono en bandeja)
- [ ] LocalStack levantado: `docker-compose up -d`
- [ ] Verificar: `docker ps`
- [ ] Compilar: `mvn clean compile`
- [ ] Ejecutar: `mvn spring-boot:run`
- [ ] Probar: `.\test-daily.ps1` o `curl http://localhost:8080/api/documents/health`

Al terminar el día:

- [ ] Detener app: `Ctrl + C`
- [ ] Detener LocalStack: `docker-compose down`
- [ ] (Opcional) Detener Docker Desktop

---

## 🆘 SI NADA FUNCIONA

### Reinicio completo de WSL
```powershell
# Como Administrador:
wsl --shutdown
wsl --unregister Ubuntu
wsl --install Ubuntu
```

### Reinstalar Docker Desktop
1. Desinstalar Docker Desktop
2. Reiniciar Windows
3. Descargar última versión: https://www.docker.com/products/docker-desktop
4. Instalar
5. Reiniciar Windows

---

**Guarda este archivo en favoritos o imprímelo** 📌
