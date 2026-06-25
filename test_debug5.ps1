. .\test_api_helper.ps1

$teacherToken = Get-Token 'teacher01' '123456'
$adminToken   = Get-Token 'admin' '123456'

$projectBody = @{
    projectName = 'Debug5 Project'
    projectCode = 'PRJ-DEBUG5-001'
    projectType = '应用研究'
    researchFields = 'AI'
    projectDescription = 'Debug5'
    requirements = 'coding'
    expectedOutcomes = 'paper'
    startDate = '2026-07-01'
    endDate = '2026-12-31'
    maxMembers = 5
} | ConvertTo-Json

$res = Invoke-Api POST '/api/research/project' $teacherToken $projectBody
$projectId = [long]$res.content.data
Write-Output "projectId type: $($projectId.GetType().FullName), value: $projectId"

$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10&status=pending_audit' $adminToken $null
$records = $res.content.data.records
Write-Output "records count: $($records.Count)"
Write-Output "first record id type: $($records[0].id.GetType().FullName), value: $($records[0].id)"

$match = $records | Where-Object { $_.id -eq $projectId }
Write-Output "match: $match"
Write-Output "match count: $($match.Count)"

# Try explicit cast
$match2 = $records | Where-Object { [long]$_.id -eq [long]$projectId }
Write-Output "match2 count: $($match2.Count)"

# Try string comparison
$match3 = $records | Where-Object { $_.id.ToString() -eq $projectId.ToString() }
Write-Output "match3 count: $($match3.Count)"
