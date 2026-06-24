#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全链路集成测试脚本

验证「智汇桥」四大模块（科研撮合、资源流转、教学辅助、系统管理）的核心接口
在本地开发环境是否能够按照预期协同工作。

测试账号（密码均为 123456）：
- admin        管理员
- student01    学生
- teacher01    教师
- enterprise01 企业
"""

import sys
import requests
from typing import Callable

# 基础配置
BASE_URL = "http://localhost:8081"
TIMEOUT = 10

# 全局状态
TOKENS: dict[str, str] = {}
"""各角色登录后获取的 JWT Token"""

USER_IDS: dict[str, int] = {}
"""各角色对应的用户 ID"""


def login(username: str, password: str) -> str:
    """
    用户登录并返回 JWT Token。

    :param username: 用户名
    :param password: 密码
    :return: JWT Token
    """
    resp = requests.post(
        f"{BASE_URL}/auth/login",
        json={"username": username, "password": password},
        timeout=TIMEOUT,
    )
    resp.raise_for_status()
    data = resp.json()
    assert data.get("code") == 200, f"登录失败：{data.get('message')}"
    return data["data"]["token"]


def fetch_user_id(role: str) -> int:
    """
    通过 /auth/info 获取当前登录用户的 ID。

    :param role: 角色标识
    :return: 用户 ID
    """
    resp = requests.get(
        f"{BASE_URL}/auth/info",
        headers=auth_headers(role),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询个人信息")
    user_id = data.get("id")
    assert user_id is not None, "无法获取用户 ID"
    return int(user_id)


def auth_headers(role: str) -> dict[str, str]:
    """
    获取指定角色的认证请求头。

    :param role: 角色标识
    :return: 包含 Authorization 的请求头字典
    """
    return {"Authorization": f"Bearer {TOKENS[role]}"}


def check_response(resp: requests.Response, operation: str) -> dict:
    """
    校验接口响应是否为业务成功。

    :param resp: HTTP 响应对象
    :param operation: 操作描述，用于报错信息
    :return: 响应体中的 data 字段
    """
    resp.raise_for_status()
    data = resp.json()
    assert data.get("code") == 200, f"{operation} 失败：{data.get('message')}"
    return data.get("data", {})


def test_case(name: str) -> Callable:
    """
    测试用例装饰器：打印用例名称并捕获异常。

    :param name: 用例名称
    :return: 装饰器函数
    """
    def decorator(func: Callable) -> Callable:
        def wrapper(*args, **kwargs):
            try:
                func(*args, **kwargs)
                print(f"  [PASS] {name}")
                return True
            except Exception as e:
                print(f"  [FAIL] {name}: {e}")
                return False
        return wrapper
    return decorator


# ==================== 一、认证模块 ====================

@test_case("管理员登录")
def test_admin_login():
    TOKENS["admin"] = login("admin", "123456")
    assert TOKENS["admin"]


@test_case("学生登录")
def test_student_login():
    TOKENS["student"] = login("student01", "123456")
    assert TOKENS["student"]


@test_case("教师登录")
def test_teacher_login():
    TOKENS["teacher"] = login("teacher01", "123456")
    assert TOKENS["teacher"]


@test_case("企业登录")
def test_enterprise_login():
    TOKENS["enterprise"] = login("enterprise01", "123456")
    assert TOKENS["enterprise"]


@test_case("获取各角色用户 ID")
def test_fetch_user_ids():
    for role in ["admin", "student", "teacher", "enterprise"]:
        USER_IDS[role] = fetch_user_id(role)
    assert len(USER_IDS) == 4


# ==================== 二、科研撮合模块 ====================

@test_case("学生查看科研项目列表")
def test_student_project_list():
    resp = requests.get(
        f"{BASE_URL}/api/research/project/list",
        headers=auth_headers("student"),
        params={"pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询科研项目列表")
    assert data.get("records") is not None


@test_case("学生查看项目详情")
def test_student_project_detail():
    resp = requests.get(
        f"{BASE_URL}/api/research/project/1",
        headers=auth_headers("student"),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询项目详情")
    assert data.get("projectName")


@test_case("学生提交项目申请")
def test_student_apply_project():
    payload = {
        "projectId": 1,
        "applicantId": USER_IDS["student"],
        "applyReason": "对推荐系统方向非常感兴趣，希望加入课题组学习。",
    }
    resp = requests.post(
        f"{BASE_URL}/api/research/application",
        headers=auth_headers("student"),
        json=payload,
        timeout=TIMEOUT,
    )
    data = resp.json()
    # 如果接口提示已提交过申请，说明幂等校验正常，也视为该链路可用
    if data.get("code") != 200 and "已提交过申请" in (data.get("message") or ""):
        return
    check_response(resp, "提交项目申请")


@test_case("教师查看我的项目申请")
def test_teacher_applications():
    resp = requests.get(
        f"{BASE_URL}/api/research/application/my",
        headers=auth_headers("teacher"),
        params={"applicantId": USER_IDS["teacher"], "pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    check_response(resp, "查询我的申请")


@test_case("企业发布需求")
def test_enterprise_publish_demand():
    payload = {
        "demandTitle": "集成测试企业需求示例",
        "demandType": "技术攻关",
        "industryField": "人工智能",
        "demandDescription": "这是一个由集成测试脚本生成的企业需求，用于验证需求发布链路。",
        "technicalRequirements": "熟悉 Python 与深度学习框架",
        "budgetRange": "5-10万",
        "cooperationMode": "联合研发",
        "contactInfo": "王经理 13800138000",
    }
    resp = requests.post(
        f"{BASE_URL}/api/research/demand",
        headers=auth_headers("enterprise"),
        json=payload,
        timeout=TIMEOUT,
    )
    check_response(resp, "发布企业需求")


@test_case("学生查看企业需求列表")
def test_student_demand_list():
    resp = requests.get(
        f"{BASE_URL}/api/research/demand/list",
        headers=auth_headers("student"),
        params={"pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询企业需求列表")
    assert data.get("records") is not None


# ==================== 三、资源流转模块 ====================

@test_case("学生查看闲置资源列表")
def test_student_resource_list():
    resp = requests.get(
        f"{BASE_URL}/api/resource/list",
        headers=auth_headers("student"),
        params={"pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询闲置资源列表")
    assert data.get("records") is not None


@test_case("学生预约闲置资源")
def test_student_book_resource():
    payload = {
        "resourceId": 1,
        "borrowerId": USER_IDS["student"],
        "startTime": "2026-07-01 09:00:00",
        "endTime": "2026-07-01 17:00:00",
        "purpose": "集成测试预约示例",
    }
    resp = requests.post(
        f"{BASE_URL}/api/resource/booking",
        headers=auth_headers("student"),
        json=payload,
        timeout=TIMEOUT,
    )
    check_response(resp, "预约闲置资源")


@test_case("教师查看我的资源预约")
def test_teacher_bookings():
    resp = requests.get(
        f"{BASE_URL}/api/resource/booking/my",
        headers=auth_headers("teacher"),
        params={"borrowerId": USER_IDS["teacher"], "pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    check_response(resp, "查询我的资源预约")


# ==================== 四、教学辅助模块 ====================

@test_case("学生查看学习资源列表")
def test_student_learning_list():
    resp = requests.get(
        f"{BASE_URL}/api/learning/resource/list",
        headers=auth_headers("student"),
        params={"pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询学习资源列表")
    assert data.get("records") is not None


@test_case("学生查看学习资源详情")
def test_student_learning_detail():
    resp = requests.get(
        f"{BASE_URL}/api/learning/resource/1",
        headers=auth_headers("student"),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询学习资源详情")
    assert data.get("resourceName")


@test_case("学生学习资源并生成记录")
def test_student_start_learning():
    payload = {
        "resourceId": 1,
        "progress": 10,
        "status": "learning",
    }
    resp = requests.post(
        f"{BASE_URL}/api/learning/record",
        headers=auth_headers("student"),
        json=payload,
        timeout=TIMEOUT,
    )
    check_response(resp, "保存学习记录")


@test_case("学生收藏学习资源")
def test_student_favorite_resource():
    resp = requests.post(
        f"{BASE_URL}/api/learning/record/favorite/1",
        headers=auth_headers("student"),
        timeout=TIMEOUT,
    )
    check_response(resp, "收藏学习资源")


@test_case("学生查看学习中心")
def test_student_learning_center():
    resp = requests.get(
        f"{BASE_URL}/api/learning/record/my",
        headers=auth_headers("student"),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询学习中心")
    assert isinstance(data, list)


@test_case("教师发布学习资源")
def test_teacher_publish_learning():
    payload = {
        "resourceName": "集成测试学习资源示例",
        "resourceType": "course",
        "subject": "计算机科学",
        "description": "由集成测试脚本生成的学习资源，用于验证发布链路。",
        "coverUrl": "",
        "contentUrl": "https://example.com/test-course",
        "difficultyLevel": "初级",
    }
    resp = requests.post(
        f"{BASE_URL}/api/learning/resource",
        headers=auth_headers("teacher"),
        json=payload,
        timeout=TIMEOUT,
    )
    check_response(resp, "发布学习资源")


# ==================== 五、系统管理模块 ====================

@test_case("管理员查看数据看板统计")
def test_admin_dashboard():
    resp = requests.get(
        f"{BASE_URL}/api/dashboard/stats",
        headers=auth_headers("admin"),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询数据看板统计")
    assert "userCount" in data


@test_case("管理员查看用户列表")
def test_admin_user_list():
    resp = requests.get(
        f"{BASE_URL}/api/admin/users",
        headers=auth_headers("admin"),
        params={"pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询用户列表")
    assert data.get("records") is not None


@test_case("管理员查看审核统计")
def test_admin_audit_stats():
    resp = requests.get(
        f"{BASE_URL}/api/admin/audit/stats",
        headers=auth_headers("admin"),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询审核统计")
    assert "projectCount" in data


@test_case("管理员查看科研项目审核列表")
def test_admin_audit_projects():
    resp = requests.get(
        f"{BASE_URL}/api/admin/audit/projects",
        headers=auth_headers("admin"),
        params={"pageNum": 1, "pageSize": 10},
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询科研项目审核列表")
    assert data.get("records") is not None


@test_case("管理员更新科研项目状态")
def test_admin_update_project_status():
    resp = requests.put(
        f"{BASE_URL}/api/admin/audit/project/1/status",
        headers=auth_headers("admin"),
        params={"status": "recruiting"},
        timeout=TIMEOUT,
    )
    check_response(resp, "更新科研项目状态")


# ==================== 六、个人中心 ====================

@test_case("学生查看个人信息")
def test_student_profile():
    resp = requests.get(
        f"{BASE_URL}/auth/info",
        headers=auth_headers("student"),
        timeout=TIMEOUT,
    )
    data = check_response(resp, "查询个人信息")
    assert data.get("username") == "student01"


@test_case("学生更新个人资料")
def test_student_update_profile():
    payload = {
        "realName": "张同学",
        "email": "student01_updated@zhihuiqiao.com",
        "phone": "13800138001",
    }
    resp = requests.put(
        f"{BASE_URL}/auth/profile",
        headers=auth_headers("student"),
        json=payload,
        timeout=TIMEOUT,
    )
    check_response(resp, "更新个人资料")


# ==================== 主函数 ====================

def main():
    """
    执行全部集成测试用例并输出统计结果。
    """
    print("=" * 60)
    print("「智汇桥」全链路集成测试开始")
    print("=" * 60)

    test_groups = [
        ("认证模块", [
            test_admin_login,
            test_student_login,
            test_teacher_login,
            test_enterprise_login,
            test_fetch_user_ids,
        ]),
        ("科研撮合模块", [
            test_student_project_list,
            test_student_project_detail,
            test_student_apply_project,
            test_teacher_applications,
            test_enterprise_publish_demand,
            test_student_demand_list,
        ]),
        ("资源流转模块", [
            test_student_resource_list,
            test_student_book_resource,
            test_teacher_bookings,
        ]),
        ("教学辅助模块", [
            test_student_learning_list,
            test_student_learning_detail,
            test_student_start_learning,
            test_student_favorite_resource,
            test_student_learning_center,
            test_teacher_publish_learning,
        ]),
        ("系统管理模块", [
            test_admin_dashboard,
            test_admin_user_list,
            test_admin_audit_stats,
            test_admin_audit_projects,
            test_admin_update_project_status,
        ]),
        ("个人中心", [
            test_student_profile,
            test_student_update_profile,
        ]),
    ]

    total = 0
    passed = 0
    failed_groups = []

    for group_name, cases in test_groups:
        print(f"\n【{group_name}】")
        group_passed = 0
        group_total = len(cases)
        for case in cases:
            total += 1
            if case():
                passed += 1
                group_passed += 1
        if group_passed < group_total:
            failed_groups.append(group_name)

    print("\n" + "=" * 60)
    print(f"测试用例总数：{total}")
    print(f"通过：{passed}")
    print(f"失败：{total - passed}")
    if failed_groups:
        print(f"存在失败的模块：{', '.join(failed_groups)}")
    else:
        print("所有模块均通过集成测试！")
    print("=" * 60)

    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
