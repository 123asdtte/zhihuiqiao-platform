. .\test_api_helper.ps1

$studentToken = Get-Token 'student01' '123456'
$teacherToken = Get-Token 'teacher01' '123456'
$adminToken   = Get-Token 'admin' '123456'

# Check project 13 status
Write-Output "--- project 13 detail ---"
$res = Invoke-Api GET '/api/research/project/13' $studentToken $null
Write-Output $res.content.data | ConvertTo-Json -Depth 2

# Check admin audit list without status filter
Write-Output ""
Write-Output "--- admin audit list all ---"
$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10' $adminToken $null
Write-Output "count: $($res.content.data.total), records: $(($res.content.data.records | Select-Object id, status, projectName) | ConvertTo-Json -Depth 2)"

# Check admin audit list with status filter
Write-Output ""
Write-Output "--- admin audit list pending_audit ---"
$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10&status=pending_audit' $adminToken $null
Write-Output "count: $($res.content.data.total), records: $(($res.content.data.records | Select-Object id, status, projectName) | ConvertTo-Json -Depth 2)"
