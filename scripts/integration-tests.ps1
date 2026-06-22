#!/usr/bin/env pwsh
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'

function PostJson($path, $body, $token) {
    $headers = @{ 'Content-Type' = 'application/json' }
    if ($token) { $headers.Authorization = "Bearer $token" }
    Invoke-RestMethod -Uri ($base + $path) -Method Post -Headers $headers -Body ($body | ConvertTo-Json -Depth 5)
}

function PostNoBody($path, $token) {
    $headers = @{}
    if ($token) { $headers.Authorization = "Bearer $token" }
    Invoke-RestMethod -Uri ($base + $path) -Method Post -Headers $headers
}

Write-Host "== Registering driver (ignore if exists) =="
try {
    $drvReg = PostJson '/auth/driver/register' @{ name='Dave'; email='dave@example.com'; password='password'; vehicleNumber='XYZ-123'; available=$true }
    Write-Host ($drvReg | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "Driver may already exist: $($_.Exception.Message)"
}

Write-Host "== Driver login =="
$drvLogin = PostJson '/auth/driver/login' @{ email='dave@example.com'; password='password' }
$drvToken = $drvLogin.token
Write-Host "Driver token length:" $drvToken.Length

Write-Host "== Registering customer Bob (ignore if exists) =="
try {
    $bobReg = PostJson '/auth/customer/register' @{ name='Bob'; email='bob@example.com'; password='password'; phoneNumber='5551112222' }
    Write-Host ($bobReg | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "Customer may already exist: $($_.Exception.Message)"
}

Write-Host "== Customer login =="
$bobLogin = PostJson '/auth/customer/login' @{ email='bob@example.com'; password='password' }
$bobToken = $bobLogin.token

Write-Host "== Create a ride (Bob) =="
$ride = PostJson '/' @{ pickupLocation='Point C'; dropLocation='Point D'; fare=30 } $bobToken
Write-Host ($ride | ConvertTo-Json -Depth 5)

Write-Host "== Driver accepts the ride =="
$accept = PostNoBody ("/driver/rides/$($ride.id)/accept") $drvToken
Write-Host ($accept | ConvertTo-Json -Depth 5)

Write-Host "== Driver starts the ride =="
$start = PostNoBody ("/driver/rides/$($ride.id)/start") $drvToken
Write-Host ($start | ConvertTo-Json -Depth 5)

Write-Host "== Driver completes the ride =="
$complete = PostNoBody ("/driver/rides/$($ride.id)/complete") $drvToken
Write-Host ($complete | ConvertTo-Json -Depth 5)

Write-Host "== Negative test: customer tries to accept a ride (should fail) =="
try {
    $bad = PostNoBody ("/driver/rides/$($ride.id)/accept") $bobToken
    Write-Host "Unexpected success:" ($bad | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "Expected failure:" $_.Exception.Message
}

Write-Host "== Negative test: create ride without auth (should 401) =="
try {
    $anonRide = PostJson '/' @{ pickupLocation='X'; dropLocation='Y'; fare=10 } $null
    Write-Host "Unexpected success:" ($anonRide | ConvertTo-Json -Depth 5)
} catch {
    Write-Host "Expected failure (unauthenticated):" $_.Exception.Message
}

Write-Host "== Concurrency test: two drivers try to accept same ride =="
# Create a fresh ride by Bob
$ride2 = PostJson '/' @{ pickupLocation='P1'; dropLocation='P2'; fare=20 } $bobToken
Write-Host "Created ride id:" $ride2.id

# Use two parallel jobs attempting to accept the same ride with the same driver token
$script = {
    param($base, $rideId, $token)
    try {
        Invoke-RestMethod -Uri ("$base/driver/rides/$rideId/accept") -Method Post -Headers @{ Authorization = "Bearer $token" }
    } catch {
        Write-Output "JOB-ERROR: $($_.Exception.Message)"
    }
}

$job1 = Start-Job -ScriptBlock $script -ArgumentList $base, $ride2.id, $drvToken
$job2 = Start-Job -ScriptBlock $script -ArgumentList $base, $ride2.id, $drvToken
Wait-Job -Job $job1, $job2
$r1 = Receive-Job $job1 -ErrorAction SilentlyContinue
$r2 = Receive-Job $job2 -ErrorAction SilentlyContinue
Write-Host "Job1 result:" ($r1 | ConvertTo-Json -Depth 5)
Write-Host "Job2 result:" ($r2 | ConvertTo-Json -Depth 5)

Write-Host "== Done =="
