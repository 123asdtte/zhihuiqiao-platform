. .\test_api_helper.ps1

$studentToken = Get-Token 'student01' '123456'

$bookingBody = @{
    resourceId = 6
    startTime = '2026-08-01T09:00:00'
    endTime = '2026-08-01T17:00:00'
    purpose = 'API test booking'
} | ConvertTo-Json

Write-Output "--- submit booking ---"
$res = Invoke-Api POST '/api/resource/booking' $studentToken $bookingBody
Write-Output $res | ConvertTo-Json -Depth 2
