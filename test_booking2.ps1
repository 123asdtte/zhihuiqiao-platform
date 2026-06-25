. .\test_api_helper.ps1

$studentToken = Get-Token 'student01' '123456'

$resourceId = 6
$start = '2026-09-01 09:00:00'
$end = '2026-09-01 17:00:00'
$bookingBody = "{`"resourceId`":$resourceId,`"startTime`":`"$start`",`"endTime`":`"$end`",`"purpose`":`"API test booking`"}"

Write-Output "body: $bookingBody"

$res = Invoke-Api POST '/api/resource/booking' $studentToken $bookingBody
Write-Output $res | ConvertTo-Json -Depth 2
