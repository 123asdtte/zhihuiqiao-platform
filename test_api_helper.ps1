# 通用 API 测试辅助脚本
$baseUrl = 'http://localhost:8081'

function Get-Token($username, $password) {
    $body = @{username=$username;password=$password} | ConvertTo-Json
    try {
        $res = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -ContentType 'application/json' -Body $body -UseBasicParsing -ErrorAction Stop
        $json = $res.Content | ConvertFrom-Json
        return $json.data.token
    } catch {
        Write-Output "Login failed for $username`: $($_.Exception.Message)"
        return $null
    }
}

function Invoke-Api($method, $path, $token, $body=$null) {
    $headers = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
    $uri = "$baseUrl$path"
    try {
        $res = Invoke-WebRequest -Uri $uri -Method $method -Headers $headers -Body $body -UseBasicParsing -ErrorAction Stop
        return @{ success=$true; status=$res.StatusCode; content=($res.Content | ConvertFrom-Json) }
    } catch {
        $errMsg = $_.Exception.Message
        $responseBody = $null
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $responseBody = $reader.ReadToEnd()
        }
        return @{ success=$false; status=$_.Exception.Response.StatusCode.value__; message=$errMsg; body=$responseBody }
    }
}

function Assert($condition, $message) {
    if ($condition) {
        Write-Output "  [PASS] $message"
    } else {
        Write-Output "  [FAIL] $message"
    }
}
