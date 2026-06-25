. .\test_api_helper.ps1

$studentToken = Get-Token 'student01' '123456'
$teacherToken = Get-Token 'teacher01' '123456'
$adminToken   = Get-Token 'admin' '123456'

Write-Output "--- teacher publish new project ---"
$projectBody = @{
    projectName = 'Debug Test Project'
    projectCode = 'PRJ-DEBUG-001'
    projectType = '应用研究'
    researchFields = 'AI'
    projectDescription = 'Debug test'
    requirements = 'coding'
    expectedOutcomes = 'paper'
    startDate = '2026-07-01'
    endDate = '2026-12-31'
    maxMembers = 5
} | ConvertTo-Json

$res = Invoke-Api POST '/api/research/project' $teacherToken $projectBody
Write-Output "publish response: $($res | ConvertTo-Json -Depth 2)"
$projectId = $res.content.data

Write-Output ""
Write-Output "--- admin audit list pending ---"
$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10&status=pending_audit' $adminToken $null
Write-Output "count: $($res.content.data.total), ids: $(($res.content.data.records | Select-Object -ExpandProperty id) -join ',')"
$inAdmin = ($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Write-Output "project $projectId in admin list: $inAdmin"

Write-Output ""
Write-Output "--- approve project ---"
$res = Invoke-Api PUT "/api/admin/audit/project/$projectId/status?status=recruiting" $adminToken $null
Write-Output "approve response: $($res | ConvertTo-Json -Depth 2)"

Write-Output ""
Write-Output "--- student list after approve ---"
$res = Invoke-Api GET '/api/research/project/list?pageNum=1&pageSize=10' $studentToken $null
Write-Output "count: $($res.content.data.total), ids: $(($res.content.data.records | Select-Object -ExpandProperty id) -join ',')"
$visible = ($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Write-Output "project $projectId visible: $visible"

Write-Output ""
Write-Output "--- resource booking ---"
$res = Invoke-Api GET '/api/resource/list?pageNum=1&pageSize=1' $studentToken $null
$resource = $res.content.data.records[0]
Write-Output "resource: $($resource | ConvertTo-Json -Depth 2)"

$bookingBody = @{
    resourceId = $resource.id
    startTime = '2026-07-01T09:00:00'
    endTime = '2026-07-01T17:00:00'
    purpose = 'API test booking'
} | ConvertTo-Json

$res = Invoke-Api POST '/api/resource/booking' $studentToken $bookingBody
Write-Output "booking response: $($res | ConvertTo-Json -Depth 2)"
