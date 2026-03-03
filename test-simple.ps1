# Script de Testing Simple
$baseUrl = "http://localhost:8080/api/documents"
$passed = 0
$failed = 0

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   DAILY TESTING - Document Management  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# TEST 1: Health Check
Write-Host ""
Write-Host "[1/6] Testing Health Endpoint..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method Get
    if ($health) {
        Write-Host "PASS: Health Check" -ForegroundColor Green
        $passed++
    }
} catch {
    Write-Host "FAIL: Health Check" -ForegroundColor Red
    $failed++
}

# TEST 2: List documents
Write-Host ""
Write-Host "[2/6] Testing List Documents..." -ForegroundColor Cyan
try {
    $list = Invoke-RestMethod -Uri $baseUrl -Method Get
    Write-Host "PASS: List Documents (Found: $($list.Count))" -ForegroundColor Green
    $passed++
} catch {
    Write-Host "FAIL: List Documents" -ForegroundColor Red
    $failed++
}

# TEST 3: Upload document
Write-Host ""
Write-Host "[3/6] Testing Upload Document..." -ForegroundColor Cyan
try {
    "Test content for validation" | Out-File -FilePath "test-upload.txt" -Encoding UTF8
    
    $boundary = [System.Guid]::NewGuid().ToString()
    $fileBytes = [System.IO.File]::ReadAllBytes("test-upload.txt")
    
    $bodyLines = @(
        "--$boundary",
        'Content-Disposition: form-data; name="name"',
        "",
        "Daily Test Document",
        "--$boundary",
        'Content-Disposition: form-data; name="file"; filename="test-upload.txt"',
        "Content-Type: text/plain",
        "",
        [System.Text.Encoding]::UTF8.GetString($fileBytes),
        "--$boundary--"
    )
    
    $body = $bodyLines -join "`r`n"
    
    $uploadResponse = Invoke-RestMethod -Uri $baseUrl -Method Post `
        -ContentType "multipart/form-data; boundary=$boundary" `
        -Body $body
    
    $script:documentId = $uploadResponse.id
    
    if ($documentId) {
        Write-Host "PASS: Upload Document (ID: $documentId)" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "FAIL: Upload Document (No ID returned)" -ForegroundColor Red
        $failed++
    }
} catch {
    Write-Host "FAIL: Upload Document - $($_.Exception.Message)" -ForegroundColor Red
    $failed++
}

# TEST 4: List documents again
Write-Host ""
Write-Host "[4/6] Testing List After Upload..." -ForegroundColor Cyan
try {
    $list = Invoke-RestMethod -Uri $baseUrl -Method Get
    if ($list.Count -gt 0) {
        Write-Host "PASS: List After Upload (Found: $($list.Count))" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "FAIL: List After Upload (Expected at least 1)" -ForegroundColor Red
        $failed++
    }
} catch {
    Write-Host "FAIL: List After Upload" -ForegroundColor Red
    $failed++
}

# TEST 5: Download document
Write-Host ""
Write-Host "[5/6] Testing Download Document..." -ForegroundColor Cyan
try {
    if ($documentId) {
        Invoke-RestMethod -Uri "$baseUrl/$documentId/download" -Method Get -OutFile "downloaded.txt"
        $content = Get-Content "downloaded.txt" -Raw
        if ($content -like "*Test content*") {
            Write-Host "PASS: Download Document" -ForegroundColor Green
            $passed++
        } else {
            Write-Host "FAIL: Download Document (Content mismatch)" -ForegroundColor Red
            $failed++
        }
    } else {
        Write-Host "SKIP: Download Document (No ID)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "FAIL: Download Document" -ForegroundColor Red
    $failed++
}

# TEST 6: Delete document
Write-Host ""
Write-Host "[6/6] Testing Delete Document..." -ForegroundColor Cyan
try {
    if ($documentId) {
        Invoke-RestMethod -Uri "$baseUrl/$documentId" -Method Delete
        Write-Host "PASS: Delete Document" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "SKIP: Delete Document (No ID)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "FAIL: Delete Document" -ForegroundColor Red
    $failed++
}

# Cleanup
Write-Host ""
Write-Host "[Cleanup] Removing temporary files..." -ForegroundColor Cyan
Remove-Item -Path "test-upload.txt" -ErrorAction SilentlyContinue
Remove-Item -Path "downloaded.txt" -ErrorAction SilentlyContinue

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           TEST SUMMARY                 " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
$total = $passed + $failed
Write-Host "Total Tests: $total"
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($failed -eq 0) {
    Write-Host "ALL TESTS PASSED!" -ForegroundColor Green
} else {
    Write-Host "SOME TESTS FAILED" -ForegroundColor Red
}
Write-Host ""
