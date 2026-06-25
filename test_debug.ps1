. .\test_api_helper.ps1

$adminToken = Get-Token 'admin' '123456'
$studentToken = Get-Token 'student01' '123456'

Write-Output "--- admin audit list ---"
$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10&status=pending_audit' $adminToken $null
Write-Output "status: $($res.status), success: $($res.success)"
if ($res.content) { $res.content | ConvertTo-Json -Depth 3 }
if ($res.body) { Write-Output $res.body }

Write-Output ""
Write-Output "--- admin users with student token ---"
$res = Invoke-Api GET '/api/admin/users?pageNum=1&pageSize=10' $studentToken $null
Write-Output "status: $($res.status), success: $($res.success)"
if ($res.content) { $res.content | ConvertTo-Json -Depth 3 }
if ($res.body) { Write-Output $res.body }

Write-Output ""
Write-Output "--- resource list first item ---"
$res = Invoke-Api GET '/api/resource/list?pageNum=1&pageSize=1' $studentToken $null
Write-Output "status: $($res.status)"
if ($res.content) { $res.content.data.records[0] | ConvertTo-Json -Depth 3 }
