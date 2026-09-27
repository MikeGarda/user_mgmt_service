<#
.SYNOPSIS
  Aufgabe 6: End-to-End-Test der Modulzuweisung
  Client -> user_mgmt_service (PUT /users/{id}/modules/{moduleId}) -> module_service -> MySQL

.BEISPIEL
  kubectl port-forward svc/backend -n user-mgmt-staging 8080:8080   (eigenes Fenster)
  Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
  .\e2e\assign-module.ps1
#>
param([string]$BaseUrl = "http://localhost:8080")

$ModuleOk      = "c02f58f2-3aca-4f1e-8076-bacf6f1999e6"   # CLOUD-ARCH (Stamm-Modul)
$ModuleUnknown = "00000000-0000-0000-0000-000000000000"
$OtherUser     = "11111111-2222-3333-4444-555555555555"
$script:results = @()

function Invoke-Api($Method, $Path, $Token, $Body) {
  $headers = @{}
  if ($Token) { $headers["Authorization"] = $Token }
  $params = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; UseBasicParsing = $true }
  if ($Body) { $params["Body"] = ($Body | ConvertTo-Json); $params["ContentType"] = "application/json" }
  try {
    $r = Invoke-WebRequest @params
    return [pscustomobject]@{ Status = [int]$r.StatusCode; Content = $r.Content; Headers = $r.Headers }
  } catch {
    $resp = $_.Exception.Response
    if ($null -eq $resp) { Write-Host "Keine Verbindung zu $BaseUrl - laeuft der Port-Forward?" -ForegroundColor Red; exit 1 }
    $content = ""
    try { $reader = New-Object System.IO.StreamReader($resp.GetResponseStream()); $content = $reader.ReadToEnd() } catch {}
    return [pscustomobject]@{ Status = [int]$resp.StatusCode; Content = $content; Headers = $null }
  }
}

function Check($Name, $Expected, $Res) {
  $script:results += [pscustomobject]@{
    Test     = $Name
    Erwartet = $Expected
    Erhalten = $Res.Status
    Ergebnis = $(if ($Res.Status -eq $Expected) { "OK" } else { "FEHLER" })
    Antwort  = $Res.Content
  }
}

# 1) Test-User registrieren und einloggen
$email = "e2e-" + ([guid]::NewGuid().ToString().Substring(0, 8)) + "@test.ch"
$password = "Test1234!"
$reg = Invoke-Api "POST" "/users/register" $null @{ firstName = "E2E"; lastName = "Test"; email = $email; password = $password }
Check "Registrierung" 201 $reg

$login = Invoke-Api "POST" "/users/login" $null @{ email = $email; password = $password }
Check "Login" 200 $login
$token = $login.Headers["Authorization"]
if ($token -is [array]) { $token = $token[0] }
if (-not $token) { Write-Host "Kein JWT erhalten - Abbruch." -ForegroundColor Red; $script:results | Format-Table -AutoSize; exit 1 }

$me = Invoke-Api "GET" "/users/me" $token $null
$userId = ($me.Content | ConvertFrom-Json).id
Write-Host "Test-User: $email  (id $userId)" -ForegroundColor Cyan

# 2) Modulzuweisungen
Check "Zuweisung (Modul existiert)"         204 (Invoke-Api "PUT" "/users/$userId/modules/$ModuleOk" $token $null)
Check "Zuweisung wiederholt (idempotent)"   204 (Invoke-Api "PUT" "/users/$userId/modules/$ModuleOk" $token $null)
Check "Unbekanntes Modul"                   404 (Invoke-Api "PUT" "/users/$userId/modules/$ModuleUnknown" $token $null)
Check "Fremder User ohne Berechtigung"      403 (Invoke-Api "PUT" "/users/$OtherUser/modules/$ModuleOk" $token $null)
Check "Ungueltige Modul-ID"                 400 (Invoke-Api "PUT" "/users/$userId/modules/keine-uuid" $token $null)
Check "Ohne Login"                          403 (Invoke-Api "PUT" "/users/$userId/modules/$ModuleOk" $null $null)

$script:results | Format-Table Test, Erwartet, Erhalten, Ergebnis -AutoSize
$script:results | Where-Object { $_.Antwort } | Format-List Test, Antwort
