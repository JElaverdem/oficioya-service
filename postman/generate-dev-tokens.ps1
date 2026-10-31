param(
    [string]$Secret = $env:JWT_SECRET,
    [string]$Issuer = "oficioya",
    [int]$TtlMinutes = 30,
    [ValidateSet("CONTRATANTE", "TRABAJADOR", "ADMIN", "BOTH")]
    [string]$Role = "BOTH",
    [string]$UserId
)

# Genera tokens JWT de desarrollo con el mismo secreto que usa oficioya-service.
#
# Uso:
#   .\postman\generate-dev-tokens.ps1
#   .\postman\generate-dev-tokens.ps1 -Role TRABAJADOR
#   .\postman\generate-dev-tokens.ps1 -Role ADMIN -TtlMinutes 60
#   .\postman\generate-dev-tokens.ps1 -Role TRABAJADOR -UserId <uuid-del-trabajador>
#
# Copiar el valor de TRABAJADOR=... en la variable workerToken del environment de Postman.
#
# Nota: el login actual crea un usuario nuevo en cada llamada, asi que para aceptar o
# rechazar una solicitud existente hay que generar el token con el UUID que ya esta
# guardado en request.workerId (-UserId). Si no se indica, se usa un UUID aleatorio.

if ([string]::IsNullOrWhiteSpace($Secret)) {
    $Secret = "oficioya-service-secret-key-must-be-at-least-256-bits-long-1234567890"
}

$subject = if ([string]::IsNullOrWhiteSpace($UserId)) {
    [guid]::NewGuid().ToString()
} else {
    try {
        [guid]::Parse($UserId).ToString()
    }
    catch {
        throw "UserId no es un UUID valido: $UserId"
    }
}

function ConvertTo-Base64Url([byte[]]$Bytes) {
    return [Convert]::ToBase64String($Bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

function New-DevToken([string]$TokenRole) {
    $header = '{"alg":"HS256","typ":"JWT"}'
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $payload = [ordered]@{
        sub  = $script:subject
        role = $TokenRole
        iss  = $Issuer
        iat  = $now
        exp  = $now + ($TtlMinutes * 60)
    } | ConvertTo-Json -Compress

    $headerPart = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($header))
    $payloadPart = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($payload))
    $unsigned = "$headerPart.$payloadPart"

    $hmac = [Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($Secret))
    try {
        $signature = ConvertTo-Base64Url ($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($unsigned)))
    }
    finally {
        $hmac.Dispose()
    }

    return "$unsigned.$signature"
}

$roles = if ($Role -eq "BOTH") { @("CONTRATANTE", "TRABAJADOR") } else { @($Role) }

foreach ($currentRole in $roles) {
    $token = New-DevToken $currentRole
    Write-Output "$currentRole=$token"
}
