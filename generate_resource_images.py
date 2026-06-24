#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为 idle_resource 表中的资源生成 AI 图片 URL 并更新到数据库。
图片使用 TRAE 提供的 text_to_image 服务生成。
"""

import urllib.parse
import mysql.connector

# 数据库连接配置
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "zhihuiqiao",
    "charset": "utf8mb4"
}

# 图片生成服务基础 URL
IMAGE_BASE_URL = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image"

# 资源 ID 与对应英文提示词、图片尺寸
RESOURCE_IMAGES = {
    1: {
        "prompt": "A professional digital oscilloscope on a clean lab bench, electronic test equipment, modern scientific instrument, product photography style, neutral background",
        "size": "landscape_4_3"
    },
    2: {
        "prompt": "A high performance deep learning server workstation with NVIDIA RTX 4090 GPU, computer hardware in server rack, blue LED lights, tech style photography",
        "size": "landscape_4_3"
    },
    3: {
        "prompt": "A stack of artificial intelligence textbooks on a wooden desk, modern AI book cover design, clean academic style, soft natural lighting",
        "size": "portrait_4_3"
    },
    4: {
        "prompt": "A portable mini projector on a white table, modern compact digital device, sleek design, product photography with clean background",
        "size": "landscape_4_3"
    },
    5: {
        "prompt": "A modern small meeting room for 8 people, with whiteboard, conference table and video equipment, bright interior design, clean office space",
        "size": "landscape_4_3"
    }
}


def build_image_url(prompt: str, size: str) -> str:
    """根据提示词和尺寸构建图片生成 URL。"""
    encoded_prompt = urllib.parse.quote(prompt)
    return f"{IMAGE_BASE_URL}?prompt={encoded_prompt}&image_size={size}"


def update_resource_images():
    """连接数据库并更新资源图片字段。"""
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    try:
        for resource_id, config in RESOURCE_IMAGES.items():
            image_url = build_image_url(config["prompt"], config["size"])
            cursor.execute(
                "UPDATE idle_resource SET images = %s WHERE id = %s",
                (image_url, resource_id)
            )
            print(f"资源 ID {resource_id} 已更新图片 URL: {image_url[:80]}...")

        conn.commit()
        print(f"\n共更新 {cursor.rowcount} 条资源记录。")
    except Exception as e:
        conn.rollback()
        print(f"更新失败: {e}")
        raise
    finally:
        cursor.close()
        conn.close()


if __name__ == "__main__":
    update_resource_images()
