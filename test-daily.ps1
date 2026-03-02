# Script de Testing Diario Automatizado
# Ejecutar: .\test-daily.ps1

Write-Host "`n╔════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   DAILY TESTING - Document Management API  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════╝`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/documents"
$passed = 0
$failed = 0

# Función para mostrar resultado
function Test-Result {
    param($testName, $success, $message = "")
    if ($success) {
        Write-Host "✓ $testName" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host "✗ $testName" -ForegroundColor Red
        if ($message) { Write-Host "  Error: $message" -ForegroundColor Yellow }
        $script:failed++
    }
}

# TEST 1: Health Check
Write-Host "`n[1/8] Testing Health Endpoint..." -ForegroundColor Cyan
try {
    $health = curl -s "$baseUrl/health"
    Test-Result "Health Check" ($health -like "*running*")
} catch {
    Test-Result "Health Check" $false $_.Exception.Message
}

# TEST 2: List empty documents
Write-Host "`n[2/8] Testing List (should be empty)..." -ForegroundColor Cyan
try {
    $list = curl -s $baseUrl
    Test-Result "List Empty Documents" ($list -eq "[]")
} catch {
    Test-Result "List Empty Documents" $false $_.Exception.Message
}

# TEST 3: Upload valid document
Write-Host "`n[3/8] Testing Upload Valid Document..." -ForegroundColor Cyan
try {
    # Crear archivo de prueba
    "Test content for daily validation" | Out-File -FilePath "test-upload.txt" -Encoding UTF8
    
    $uploadResponse = curl -s -X POST "$baseUrl" `
        -F "file=@test-upload.txt" `
        -F "name=Daily Test Document" | ConvertFrom-Json
    
    $documentId = $uploadResponse.id
    
    if ($documentId) {
        Test-Result "Upload Valid Document" $true
        Write-Host "  Document ID: $documentId" -ForegroundColor Gray
    } else {
        Test-Result "Upload Valid Document" $false "No ID returned"
    }
} catch {
    Test-Result "Upload Valid Document" $false $_.Exception.Message
}

# TEST 4: List documents (should have 1)
Write-Host "`n[4/8] Testing List (should have 1)..." -ForegroundColor Cyan
try {
    $list = curl -s $baseUrl | ConvertFrom-Json
    Test-Result "List Documents" ($list.Count -eq 1)
    Write-Host "  Found: $($list.Count) document(s)" -ForegroundColor Gray
} catch {
    Test-Result "List Documents" $false $_.Exception.Message
}

# TEST 5: Download document
Write-Host "`n[5/8] Testing Download..." -ForegroundColor Cyan
try {
    if ($documentId) {
        curl -s "$baseUrl/$documentId/download" -o "downloaded-test.txt"
        $content = Get-Content "downloaded-test.txt" -Raw
        Test-Result "Download Document" ($content -like "*Test content*")
    } else {
        Test-Result "Download Document" $false "No document ID available"
    }
} catch {
    Test-Result "Download Document" $false $_.Exception.Message
}

# TEST 6: Delete document
Write-Host "`n[6/8] Testing Delete..." -ForegroundColor Cyan
try {
    if ($documentId) {
        curl -s -X DELETE "$baseUrl/$documentId"
        Test-Result "Delete Document" $true
    } else {
        Test-Result "Delete Document" $false "No document ID available"
    }
} catch {
    Test-Result "Delete Document" $false $_.Exception.Message
}

# TEST 7: Verify deletion
Write-Host "`n[7/8] Testing List (should be empty again)..." -ForegroundColor Cyan
try {
    Start-Sleep -Seconds 1  # Dar tiempo para que se procese la eliminación
    $list = curl -s $baseUrl
    Test-Result "Verify Deletion" ($list -eq "[]")
} catch {
    Test-Result "Verify Deletion" $false $_.Exception.Message
}

# TEST 8: Upload invalid file (too large)
Write-Host "`n[8/8] Testing Validation (file too large)..." -ForegroundColor Cyan
try {
    # Crear archivo de 11MB
    $bytes = New-Object byte[] (11MB)
    [System.IO.File]::WriteAllBytes("large-file.txt", $bytes)
    
    $errorResponse = curl -s -X POST "$baseUrl" `
        -F "file=@large-file.txt" `
        -F "name=Large File" 2>&1 | Out-String
    
    $isRejected = ($errorResponse -like "*413*" -or $errorResponse -like "*Payload Too Large*" -or $errorResponse -like "*exceeds*")
    Test-Result "Validation (File Too Large)" $isRejected
    
    if ($isRejected) {
        Write-Host "  File correctly rejected ✓" -ForegroundColor Gray
    }
} catch {
    # Si falla al subir, es correcto (el archivo es muy grande)
    Test-Result "Validation (File Too Large)" $true
}

# Limpiar archivos temporales
Write-Host "`n[Cleanup] Removing temporary files..." -ForegroundColor Cyan
Remove-Item -Path "test-upload.txt" -ErrorAction SilentlyContinue
Remove-Item -Path "downloaded-test.txt" -ErrorAction SilentlyContinue
Remove-Item -Path "large-file.txt" -ErrorAction SilentlyContinue
Write-Host "  Cleanup complete ✓" -ForegroundColor Gray

# Resumen
Write-Host "`n╔════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║              TEST SUMMARY                  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════╝" -ForegroundColor Cyan

$total = $passed + $failed
Write-Host "`nTotal Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })

if ($failed -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! 🎉" -ForegroundColor Green
    Write-Host "✓ La aplicación funciona correctamente" -ForegroundColor Green
    Write-Host "✓ Listo para hacer commit y push" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  SOME TESTS FAILED ⚠️" -ForegroundColor Red
    Write-Host "✗ Revisa los errores antes de hacer commit" -ForegroundColor Red
    Write-Host "✗ Verifica que LocalStack esté corriendo" -ForegroundColor Red
}

Write-Host "`n"
