. .\test_api_helper.ps1

$teacherToken = Get-Token 'teacher01' '123456'
$adminToken   = Get-Token 'admin' '123456'
$studentToken = Get-Token 'student01' '123456'

$projectBody = @{
    projectName = 'Debug4 Project'
    projectCode = 'PRJ-DEBUG4-001'
    projectType = '应用研究'
    researchFields = 'AI'
    projectDescription = 'Debug4'
    requirements = 'coding'
    expectedOutcomes = 'paper'
    startDate = '2026-07-01'
    endDate = '2026-12-31'
    maxMembers = 5
} | ConvertTo-Json

$res = Invoke-Api POST '/api/research/project' $teacherToken $projectBody
Write-Output "publish: $($res | ConvertTo-Json -Depth 2)"
$projectId = $res.content.data
Start-Sleep -Milliseconds 500

$res = Invoke-Api GET '/api/research/project/list?pageNum=1&pageSize=10' $studentToken $null
Write-Output "student list count: $($res.content.data.total)"

$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10&status=pending_audit' $adminToken $null
Write-Output "admin audit count: $($res.content.data.total), ids: $(($res.content.data.records | Select-Object -ExpandProperty id) -join ',')"
$found = ($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Write-Output "found project $projectId in admin audit: $found"

$res = Invoke-Api PUT "/api/admin/audit/project/$projectId/status?status=recruiting" $adminToken $null
Write-Output "approve: $($res | ConvertTo-Json -Depth 2)"
Start-Sleep -Milliseconds 500

$res = Invoke-Api GET '/api/research/project/list?pageNum=1&pageSize=10' $studentToken $null
Write-Output "student list count after approve: $($res.content.data.total), ids: $(($res.content.data.records | Select-Object -ExpandProperty id) -join ',')"
$visible = ($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Write-Output "project $projectId visible: $visible"

Write-Output ""
Write-Output "--- booking test ---"
$bookingBody = @{
    resourceId = 6
    startTime = '2026-08-01 09:00:00'
    endTime = '2026-08-01 17:00:00'
    purpose = 'API test booking'
} | ConvertTo-Json

$res = Invoke-Api POST '/api/resource/booking' $studentToken $bookingBody
Write-Output "booking: $($res | ConvertTo-Json -Depth 2)"
