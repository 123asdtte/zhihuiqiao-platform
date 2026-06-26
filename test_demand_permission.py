# 企业需求发布权限测试脚本
# 验证：仅 enterprise 和 admin 可发布企业需求，student/teacher 应被拒绝

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


def publish_demand(token: str) -> dict:
    """调用企业需求发布接口，返回 {ok, status, body, code}

    注意：后端统一返回 HTTP 200，业务错误通过 Result.code / Result.message 表达，
    因此需要解析 JSON 判断 code 是否为 200。
    """
    demand = {
        'demandTitle': '测试企业需求标题',
        'demandType': '技术攻关',
        'industryField': '智能制造',
        'demandDescription': '这是一条用于权限测试的企业需求描述',
        'budgetRange': '5万-10万',
        'cooperationMode': '委托开发',
        'contactPerson': '测试联系人',
        'status': 'pending_audit'
    }
    data = json.dumps(demand, ensure_ascii=False).encode('utf-8')
    req = urllib.request.Request(
        f'{BASE_URL}/api/research/demand',
        data=data,
        headers={
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json; charset=utf-8'
        },
        method='POST'
    )
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode('utf-8')
            parsed = json.loads(body)
            return {'ok': True, 'status': resp.status, 'body': body, 'code': parsed.get('code')}
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        parsed = json.loads(body) if body else {}
        return {'ok': False, 'status': e.code, 'body': body, 'code': parsed.get('code')}


def assert_condition(condition: bool, message: str):
    """简单的断言输出"""
    if condition:
        print(f'  [PASS] {message}')
    else:
        print(f'  [FAIL] {message}')


if __name__ == '__main__':
    print('===== 企业需求发布权限测试 =====')

    # 1. 企业账号应能成功发布
    enterprise_token = login('enterprise01', '123456')
    assert_condition(enterprise_token is not None, 'enterprise01 登录成功')
    enterprise_res = publish_demand(enterprise_token)
    assert_condition(enterprise_res['code'] == 200, f"enterprise01 发布企业需求成功 (code={enterprise_res['code']})")

    # 2. 管理员账号应能成功发布
    admin_token = login('admin', '123456')
    assert_condition(admin_token is not None, 'admin 登录成功')
    admin_res = publish_demand(admin_token)
    assert_condition(admin_res['code'] == 200, f"admin 发布企业需求成功 (code={admin_res['code']})")

    # 3. 学生账号应被拒绝，且提示信息正确
    student_token = login('student01', '123456')
    assert_condition(student_token is not None, 'student01 登录成功')
    student_res = publish_demand(student_token)
    assert_condition(student_res['code'] != 200, f"student01 发布企业需求被拒绝 (code={student_res['code']})")
    assert_condition('无权发布企业需求' in student_res['body'], 'student01 收到正确的拒绝提示')

    # 4. 教师账号应被拒绝，且提示信息正确
    teacher_token = login('teacher01', '123456')
    assert_condition(teacher_token is not None, 'teacher01 登录成功')
    teacher_res = publish_demand(teacher_token)
    assert_condition(teacher_res['code'] != 200, f"teacher01 发布企业需求被拒绝 (code={teacher_res['code']})")
    assert_condition('无权发布企业需求' in teacher_res['body'], 'teacher01 收到正确的拒绝提示')

    print('===== 测试结束 =====')
