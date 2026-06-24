#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用 Selenium + Edge 浏览器截取 Day 9 开发联调页面截图
"""

import os
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.edge.options import Options
from selenium.webdriver.edge.service import Service
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# 输出目录
OUTPUT_DIR = r"C:\Users\luofeng\Desktop\实训\docs\day9"
os.makedirs(OUTPUT_DIR, exist_ok=True)

BASE_URL = "http://localhost:5175"


def create_driver():
    """创建 headless Edge 浏览器实例"""
    options = Options()
    options.add_argument("--headless")
    options.add_argument("--disable-gpu")
    options.add_argument("--window-size=1920,1080")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    # 避免 webdriver 被检测
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    options.add_experimental_option("useAutomationExtension", False)
    return webdriver.Edge(options=options)


def full_page_screenshot(driver, path):
    """截取完整页面截图：调整窗口高度为文档高度"""
    # 等待页面渲染完成
    time.sleep(2)
    # 获取文档高度
    total_height = driver.execute_script("return document.body.scrollHeight")
    # 设置窗口大小，宽度保持 1920
    driver.set_window_size(1920, max(total_height, 1080))
    time.sleep(1)
    driver.save_screenshot(path)
    # 恢复默认窗口大小
    driver.set_window_size(1920, 1080)


def login(driver, username, password):
    """执行登录操作"""
    driver.get(f"{BASE_URL}/login")
    wait = WebDriverWait(driver, 10)

    # 等待输入框出现
    username_input = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "input[type='text']")))
    password_input = driver.find_element(By.CSS_SELECTOR, "input[type='password']")

    username_input.clear()
    username_input.send_keys(username)
    password_input.clear()
    password_input.send_keys(password)

    # 点击登录按钮
    login_btn = driver.find_element(By.XPATH, "//button[contains(., '登')]")
    login_btn.click()

    # 等待页面跳转
    wait.until(EC.url_contains("/app/home"))
    time.sleep(2)


def logout(driver):
    """退出登录"""
    driver.get(f"{BASE_URL}/login")
    time.sleep(1)


def main():
    driver = create_driver()
    try:
        # 1. admin 登录并截图管理页面
        login(driver, "admin", "123456")

        # 用户管理页
        driver.get(f"{BASE_URL}/app/admin/users")
        full_page_screenshot(driver, os.path.join(OUTPUT_DIR, "user_manage.png"))
        print("已保存 user_manage.png")

        # 内容审核页
        driver.get(f"{BASE_URL}/app/admin/audit")
        full_page_screenshot(driver, os.path.join(OUTPUT_DIR, "audit_manage.png"))
        print("已保存 audit_manage.png")

        # 2. teacher01 登录并截图教学辅助和个人中心
        logout(driver)
        login(driver, "teacher01", "123456")

        # 学习资源详情页（点击第一个资源）
        driver.get(f"{BASE_URL}/app/learning/resources")
        wait = WebDriverWait(driver, 10)
        # 等待第一个资源卡片出现
        first_card = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".resource-name")))
        first_card.click()
        wait.until(EC.url_contains("/app/learning/detail/"))
        full_page_screenshot(driver, os.path.join(OUTPUT_DIR, "learning_resource_detail.png"))
        print("已保存 learning_resource_detail.png")

        # 发布学习资源页
        driver.get(f"{BASE_URL}/app/learning/publish")
        full_page_screenshot(driver, os.path.join(OUTPUT_DIR, "learning_resource_publish.png"))
        print("已保存 learning_resource_publish.png")

        # 个人中心页
        driver.get(f"{BASE_URL}/app/user/profile")
        full_page_screenshot(driver, os.path.join(OUTPUT_DIR, "user_profile.png"))
        print("已保存 user_profile.png")

    finally:
        driver.quit()


if __name__ == "__main__":
    main()
