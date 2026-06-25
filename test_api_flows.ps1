. .\test_api_helper.ps1

Write-Output "===== 登录各角色账号 ====="
$studentToken = Get-Token 'student01' '123456'
$teacherToken = Get-Token 'teacher01' '123456'
$adminToken   = Get-Token 'admin' '123456'

Assert ($studentToken -ne $null) "student01 login"
Assert ($teacherToken -ne $null) "teacher01 login"
Assert ($adminToken -ne $null) "admin login"

Write-Output ""
Write-Output "===== Test 1: Research Project Audit Flow ====="
$projectBody = @{
    projectName = 'API Test Project'
    projectCode = 'PRJ-API-001'
    projectType = '应用研究'
    researchFields = 'AI'
    projectDescription = 'API test'
    requirements = 'coding'
    expectedOutcomes = 'paper'
    startDate = '2026-07-01'
    endDate = '2026-12-31'
    maxMembers = 5
} | ConvertTo-Json

$res = Invoke-Api POST '/api/research/project' $teacherToken $projectBody
Assert ($res.success -and $res.content.code -eq 200) "teacher publish project success"
$projectId = [long]$res.content.data

$res = Invoke-Api GET "/api/research/project/$projectId" $studentToken $null
Assert ($res.success -and $res.content.data.status -eq 'pending_audit') "new project status is pending_audit"

$res = Invoke-Api GET '/api/research/project/list?pageNum=1&pageSize=10' $studentToken $null
$visible = @($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Assert (-not $visible) "pending project not visible in student list"

$res = Invoke-Api GET '/api/admin/audit/projects?pageNum=1&pageSize=10&status=pending_audit' $adminToken $null
$inAdmin = @($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Assert $inAdmin "pending project visible in admin audit list"

$res = Invoke-Api PUT "/api/admin/audit/project/$projectId/status?status=recruiting" $adminToken $null
Assert ($res.success -and $res.content.code -eq 200) "admin approve project"

$res = Invoke-Api GET '/api/research/project/list?pageNum=1&pageSize=10' $studentToken $null
$visible = @($res.content.data.records | Where-Object { $_.id -eq $projectId }).Count -gt 0
Assert $visible "approved project visible in student list"

Write-Output ""
Write-Output "===== Test 2: Enterprise Demand Audit Flow ====="
$demandBody = @{
    demandTitle = 'API Test Demand'
    demandDescription = 'API demand test'
    industryField = 'IT'
    budget = '10000'
    contactName = 'Test'
    contactPhone = '13800138000'
} | ConvertTo-Json

$res = Invoke-Api POST '/api/research/demand' $teacherToken $demandBody
Assert ($res.success -and $res.content.code -eq 200) "teacher publish demand success"
$demandId = [long]$res.content.data

$res = Invoke-Api GET '/api/research/demand/list?pageNum=1&pageSize=10' $studentToken $null
$visible = @($res.content.data.records | Where-Object { $_.id -eq $demandId }).Count -gt 0
Assert (-not $visible) "pending demand not visible in list"

$res = Invoke-Api PUT "/api/admin/audit/demand/$demandId/status?status=open" $adminToken $null
Assert ($res.success -and $res.content.code -eq 200) "admin approve demand"

Write-Output ""
Write-Output "===== Test 3: Resource Booking Cancel ====="
$res = Invoke-Api GET '/api/resource/list?pageNum=1&pageSize=1' $studentToken $null
$resourceId = $res.content.data.records[0].id

$bookingBody = "{`"resourceId`":$resourceId,`"startTime`":`"2026-07-01 09:00:00`",`"endTime`":`"2026-07-01 17:00:00`",`"purpose`":`"API test booking`"}"

$res = Invoke-Api POST '/api/resource/booking' $studentToken $bookingBody
Assert ($res.success -and $res.content.code -eq 200) "student submit booking"
$bookingId = [long]$res.content.data

$res = Invoke-Api PUT "/api/resource/booking/$bookingId/cancel" $studentToken $null
Assert ($res.success -and $res.content.code -eq 200) "student cancel pending booking"

Write-Output ""
Write-Output "===== Test 4: Learning Resource Audit Flow ====="
$learningBody = @{
    resourceName = 'API Test Learning'
    resourceType = 'video'
    subject = 'CS'
    description = 'API learning test'
    content = 'test content'
    status = 2
} | ConvertTo-Json

$res = Invoke-Api POST '/api/learning/resource' $teacherToken $learningBody
Assert ($res.success -and $res.content.code -eq 200) "teacher publish learning resource"
$learningId = [long]$res.content.data

$res = Invoke-Api GET '/api/learning/resource/list?pageNum=1&pageSize=10' $studentToken $null
$visible = @($res.content.data.records | Where-Object { $_.id -eq $learningId }).Count -gt 0
Assert (-not $visible) "pending learning resource not visible"

$res = Invoke-Api PUT "/api/admin/audit/learning-resource/$learningId/status?status=1" $adminToken $null
Assert ($res.success -and $res.content.code -eq 200) "admin approve learning resource"

Write-Output ""
Write-Output "===== Test 5: Data Permission and RBAC ====="
$res = Invoke-Api GET '/api/research/profile/2' $studentToken $null
Assert ($res.success -and $res.content.code -eq 200) "student view own profile"

$res = Invoke-Api GET '/api/research/profile/3' $studentToken $null
Assert (-not $res.success -or $res.content.code -ne 200) "student cannot view others profile"

$res = Invoke-Api GET '/api/admin/users?pageNum=1&pageSize=10' $studentToken $null
Assert (-not $res.success) "student cannot access admin API"

$applyBody = @{
    projectId = $projectId
    applicantId = 2
    applyReason = 'API test apply'
} | ConvertTo-Json

$res = Invoke-Api POST '/api/research/application' $studentToken $applyBody
Assert ($res.success -and $res.content.code -eq 200) "student submit project application"

Write-Output ""
Write-Output "===== API Tests Completed ====="
