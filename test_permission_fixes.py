# 角色权限修复验证脚本
# 验证各模块后端接口是否按角色/所有者正确隔离

import json
import urllib.request
import urllib.error

BASE_URL = 'http://localhost:8081'


def login(username: str, password: str) -> str:
    """登录并返回 JWT token"""
    data = json.dumps({'username': username, 'password': password}).encode('utf-8')
    req = urllib.request.Request(
        f'{BASE_URL}/auth/login',
        data=data,
        headers={'Content-Type': 'application/json; charset=utf-8'},
        method='POST'
    )
    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read().decode('utf-8'))
        return result['data']['token']


def request_json(method: str, path: str, token: str = None, body: dict = None) -> dict:
    """发起 JSON 请求，返回 {ok, status, code, body, data}

    业务错误通过 code != 200 表达，HTTP 通常为 200。
    """
    headers = {'Content-Type': 'application/json; charset=utf-8'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    data = json.dumps(body, ensure_ascii=False).encode('utf-8') if body is not None else None
    req = urllib.request.Request(f'{BASE_URL}{path}', data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            body_text = resp.read().decode('utf-8')
            parsed = json.loads(body_text)
            return {'ok': True, 'status': resp.status, 'code': parsed.get('code'),
                    'body': body_text, 'data': parsed.get('data')}
    except urllib.error.HTTPError as e:
        body_text = e.read().decode('utf-8')
        parsed = json.loads(body_text) if body_text else {}
        return {'ok': False, 'status': e.code, 'code': parsed.get('code'),
                'body': body_text, 'data': parsed.get('data')}


def assert_pass(condition: bool, message: str):
    if condition:
        print(f'  [PASS] {message}')
    else:
        print(f'  [FAIL] {message}')


print('===== 1. 资源流转模块权限验证 =====')
student_token = login('student01', '123456')
teacher_token = login('teacher01', '123456')
enterprise_token = login('enterprise01', '123456')
admin_token = login('admin', '123456')

# 1.1 企业发布资源应被拒绝
res = request_json('POST', '/api/resource/publish', enterprise_token, {
    'resourceName': '企业测试资源', 'resourceType': '实验设备', 'description': '测试',
    'borrowRules': '测试规则', 'location': '测试地点'
})
assert_pass(res['code'] != 200 and '无权发布闲置资源' in res['body'],
            'enterprise01 发布闲置资源被拒绝')

# 1.2 学生发布资源应成功
res = request_json('POST', '/api/resource/publish', student_token, {
    'resourceName': '学生测试资源', 'resourceType': '图书资料', 'description': '测试资源',
    'borrowRules': '测试规则', 'location': '测试地点'
})
assert_pass(res['code'] == 200, f'student01 发布闲置资源成功 (code={res["code"]})')
resource_id = res['data']

# 学生将资源状态改为可借用
res = request_json('PUT', f'/api/resource/{resource_id}/status?status=available', student_token)
assert_pass(res['code'] == 200, 'student01 将自有资源改为 available')

# 1.3 非所有者修改资源状态应被拒绝
res = request_json('PUT', f'/api/resource/{resource_id}/status?status=rented', teacher_token)
assert_pass(res['code'] != 200 and '无权修改该资源状态' in res['body'],
            'teacher01 无法修改他人资源状态')

# 1.4 企业预约资源应被拒绝
res = request_json('POST', '/api/resource/booking', enterprise_token, {
    'resourceId': resource_id, 'startTime': '2026-07-01 09:00:00', 'endTime': '2026-07-01 18:00:00'
})
assert_pass(res['code'] != 200 and '无权预约闲置资源' in res['body'],
            'enterprise01 预约资源被拒绝')

# 1.5 教师预约学生资源应成功（教师属于可借用角色）
res = request_json('POST', '/api/resource/booking', teacher_token, {
    'resourceId': resource_id, 'startTime': '2026-07-01 09:00:00', 'endTime': '2026-07-01 18:00:00'
})
assert_pass(res['code'] == 200, f'teacher01 预约资源成功 (code={res["code"]})')
booking_id = res['data']

# 1.6 非所有者审批预约应被拒绝（学生在自己是所有者时可通过，这里用老师测试非所有者）
res = request_json('PUT', f'/api/resource/booking/{booking_id}/audit?status=approved', teacher_token)
assert_pass(res['code'] != 200 and '无权审批该预约' in res['body'],
            'teacher01 无法审批他人资源的预约')

# 所有者审批预约
res = request_json('PUT', f'/api/resource/booking/{booking_id}/audit?status=approved', student_token)
assert_pass(res['code'] == 200, f'student01 审批自有资源预约成功 (code={res["code"]})')

# 1.7 非授权用户归还资源应被拒绝（这里用 admin 测试，admin 可以通过，换一个非所有者非借用人）
# 借用人是 teacher01，所有者是 student01，用 enterprise 已被拒绝预约，无其他账号，用非借用人测试：
# 由于 admin 同时是管理员，有权归还，因此重新用 student02 不太现实；这里用非借用人且非所有者测试：
# 先临时用 admin 测试有权归还
res = request_json('PUT', f'/api/resource/booking/{booking_id}/return', admin_token)
assert_pass(res['code'] == 200, 'admin 有权归还资源')

# 为了测试非授权用户归还，再创建一个预约并批准
res = request_json('POST', '/api/resource/booking', teacher_token, {
    'resourceId': resource_id, 'startTime': '2026-07-02 09:00:00', 'endTime': '2026-07-02 18:00:00'
})
assert_pass(res['code'] == 200, 'teacher01 再次预约成功')
booking_id2 = res['data']
res = request_json('PUT', f'/api/resource/booking/{booking_id2}/audit?status=approved', student_token)
assert_pass(res['code'] == 200, 'student01 再次审批成功')

# 非所有者/非借用人/非管理员尝试归还（enterprise 无预约权限，直接作为非授权角色）
# enterprise 无 booking 可用，使用 path 上存在的 bookingId 即可触发权限校验
res = request_json('PUT', f'/api/resource/booking/{booking_id2}/return', admin_token)
# admin 有权，先换成另一个用户：这里用 student01 自己（所有者）归还也可以
# 但我们要测非授权，没有第三个学生账号。改用 enterprise  token 调用：enterprise 不是借用人/所有者/管理员，应拒绝
# 但 enterprise 没 booking，不过由于校验在 booking 存在之后，可直接用 booking_id2
res = request_json('PUT', f'/api/resource/booking/{booking_id2}/return', enterprise_token)
assert_pass(res['code'] != 200 and '无权归还该资源' in res['body'],
            'enterprise01 无法归还他人资源')

# 借用人正常归还
res = request_json('PUT', f'/api/resource/booking/{booking_id2}/return', teacher_token)
assert_pass(res['code'] == 200, 'teacher01 作为借用人归还成功')

# 1.8 非所有者查看流转记录应被拒绝
res = request_json('GET', f'/api/resource/{resource_id}/transfer-logs', teacher_token)
assert_pass(res['code'] != 200 and '无权查看该资源的流转记录' in res['body'],
            'teacher01 无法查看他人资源流转记录')

# 所有者可查看
res = request_json('GET', f'/api/resource/{resource_id}/transfer-logs', student_token)
assert_pass(res['code'] == 200, 'student01 可查看自有资源流转记录')


print('===== 2. 教学辅助模块权限验证 =====')

# 2.1 学生发布学习资源应被拒绝
res = request_json('POST', '/api/learning/resource', student_token, {
    'resourceName': '学生测试学习资源', 'resourceType': 'book', 'subject': '测试学科',
    'description': '测试', 'contentUrl': 'https://example.com/test', 'difficultyLevel': '初级'
})
assert_pass(res['code'] != 200 and '无权发布学习资源' in res['body'],
            'student01 发布学习资源被拒绝')

# 2.2 教师发布学习资源应成功
res = request_json('POST', '/api/learning/resource', teacher_token, {
    'resourceName': '教师测试学习资源', 'resourceType': 'book', 'subject': '测试学科',
    'description': '测试', 'contentUrl': 'https://example.com/test', 'difficultyLevel': '初级'
})
assert_pass(res['code'] == 200, f'teacher01 发布学习资源成功 (code={res["code"]})')
learning_resource_id = res['data']

# 2.3 非所有者删除学习资源应被拒绝
res = request_json('DELETE', f'/api/learning/resource/{learning_resource_id}', student_token)
assert_pass(res['code'] != 200 and '无权删除该学习资源' in res['body'],
            'student01 无法删除他人学习资源')

# 2.4 学生创建学习记录（收藏该资源）
res = request_json('POST', '/api/learning/record', student_token, {
    'resourceId': learning_resource_id, 'status': 'favorite'
})
assert_pass(res['code'] == 200, f'student01 创建学习记录成功 (code={res["code"]})')
record_id = res['data']

# 2.5 非所有者删除学习记录应被拒绝
res = request_json('DELETE', f'/api/learning/record/{record_id}', teacher_token)
assert_pass(res['code'] != 200 and '无权删除该学习记录' in res['body'],
            'teacher01 无法删除他人学习记录')

# 2.6 所有者删除学习记录应成功
res = request_json('DELETE', f'/api/learning/record/{record_id}', student_token)
assert_pass(res['code'] == 200, 'student01 删除自有学习记录成功')

# 2.7 所有者删除学习资源应成功
res = request_json('DELETE', f'/api/learning/resource/{learning_resource_id}', teacher_token)
assert_pass(res['code'] == 200, 'teacher01 删除自有学习资源成功')


print('===== 3. 科研撮合模块权限验证 =====')

# 3.1 企业维护科研画像应被拒绝
res = request_json('POST', '/api/research/profile', enterprise_token, {
    'userId': 1, 'researchInterests': '测试', 'skills': '测试'
})
assert_pass(res['code'] != 200 and '当前角色无法维护科研画像' in res['body'],
            'enterprise01 维护科研画像被拒绝')

# 3.2 学生维护科研画像应成功
res = request_json('POST', '/api/research/profile', student_token, {
    'userId': 1, 'researchInterests': 'AI', 'skills': 'Python'
})
assert_pass(res['code'] == 200, f'student01 维护科研画像成功 (code={res["code"]})')

# 3.3 非项目发布者无法审批项目申请
# 先由教师发布项目
teacher_project = {
    'projectName': '权限测试项目', 'projectCode': 'PRJ-PERM-001', 'projectType': '应用研究',
    'researchFields': 'AI', 'projectDescription': '测试', 'requirements': '测试',
    'expectedOutcomes': '测试', 'maxMembers': 5, 'startDate': '2026-07-01',
    'endDate': '2026-12-31'
}
res = request_json('POST', '/api/research/project', teacher_token, teacher_project)
assert_pass(res['code'] == 200, f'teacher01 发布项目成功 (code={res["code"]})')
project_id = res['data']

# 学生申请项目
res = request_json('POST', '/api/research/application', student_token, {
    'projectId': project_id, 'message': '申请加入测试'
})
assert_pass(res['code'] == 200, f'student01 申请项目成功 (code={res["code"]})')
application_id = res['data']

# 另一学生尝试审批应被拒绝
# 没有 student02，用 student01 自己申请自己审批其实也不允许（不是发布者）
res = request_json('PUT', f'/api/research/application/{application_id}/audit?status=approved', student_token)
assert_pass(res['code'] != 200 and '无权审批该项目的申请' in res['body'],
            'student01 无法审批他人项目的申请')

# 项目发布者审批应成功
res = request_json('PUT', f'/api/research/application/{application_id}/audit?status=approved', teacher_token)
assert_pass(res['code'] == 200, 'teacher01 审批自有项目申请成功')


print('===== 4. 数据看板权限验证 =====')

# 4.1 非 admin 无法查看全平台统计
res = request_json('GET', '/api/dashboard/stats', student_token)
assert_pass(res['code'] != 200 and '无权查看全平台统计数据' in res['body'],
            'student01 无法查看全平台统计')

# 4.2 admin 可查看全平台统计
res = request_json('GET', '/api/dashboard/stats', admin_token)
assert_pass(res['code'] == 200, 'admin 可查看全平台统计')

print('===== 验证结束 =====')
