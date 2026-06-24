#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
清理 idle_resource 表中的重复数据，重新插入 5 条干净的示例资源，
并配图 TRAE text_to_image 生成的图片 URL。
"""

import urllib.parse
import mysql.connector

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "zhihuiqiao",
    "charset": "utf8mb4"
}

IMAGE_BASE_URL = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image"

RESOURCES = [
    {
        "name": "高精度示波器 TDS3054B",
        "type": "实验设备",
        "owner_id": 2,
        "description": "泰克数字荧光示波器，500MHz带宽，4通道，适合电子电路实验。",
        "prompt": "A professional digital oscilloscope on a clean lab bench, electronic test equipment, modern scientific instrument, product photography style, neutral background",
        "size": "landscape_4_3",
        "location": "工科楼A301",
        "original_price": 85000.00,
        "rental_price": 50.00,
        "borrow_rules": "需提前预约，使用前后需登记，损坏照价赔偿",
        "views": 32
    },
    {
        "name": "深度学习服务器 RTX4090",
        "type": "实验设备",
        "owner_id": 3,
        "description": "配备RTX4090显卡的高性能服务器，适合深度学习模型训练。",
        "prompt": "A high performance deep learning server workstation with NVIDIA RTX 4090 GPU, computer hardware in server rack, blue LED lights, tech style photography",
        "size": "landscape_4_3",
        "location": "计算机楼B205",
        "original_price": 35000.00,
        "rental_price": 100.00,
        "borrow_rules": "仅限科研使用，禁止挖矿，使用需提前预约",
        "views": 58
    },
    {
        "name": "《人工智能：一种现代的方法》第4版",
        "type": "图书资料",
        "owner_id": 2,
        "description": "经典AI教材，9成新，适合人工智能入门学习。",
        "prompt": "A stack of artificial intelligence textbooks on a wooden desk, modern AI book cover design, clean academic style, soft natural lighting",
        "size": "portrait_4_3",
        "location": "图书馆三楼",
        "original_price": 128.00,
        "rental_price": 0.00,
        "borrow_rules": "借阅周期不超过30天，到期可申请续借",
        "views": 12
    },
    {
        "name": "便携式投影仪",
        "type": "电子数码",
        "owner_id": 3,
        "description": "小米便携式投影仪，适合小组讨论和项目答辩使用。",
        "prompt": "A portable mini projector on a white table, modern compact digital device, sleek design, product photography with clean background",
        "size": "landscape_4_3",
        "location": "行政楼C102",
        "original_price": 2999.00,
        "rental_price": 20.00,
        "borrow_rules": "借用不超过7天，需押学生证",
        "views": 26
    },
    {
        "name": "小型会议室（可容纳8人）",
        "type": "场地空间",
        "owner_id": 2,
        "description": "配备白板、投影仪、视频会议设备，适合小组讨论。",
        "prompt": "A modern small meeting room for 8 people, with whiteboard, conference table and video equipment, bright interior design, clean office space",
        "size": "landscape_4_3",
        "location": "创新楼D401",
        "original_price": 0.00,
        "rental_price": 0.00,
        "borrow_rules": "需提前2天预约，单次使用不超过4小时",
        "views": 41
    }
]


def build_image_url(prompt: str, size: str) -> str:
    encoded_prompt = urllib.parse.quote(prompt)
    return f"{IMAGE_BASE_URL}?prompt={encoded_prompt}&image_size={size}"


def reset_resources():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    try:
        # 清空表并重置自增 ID
        cursor.execute("TRUNCATE TABLE idle_resource")
        print("已清空 idle_resource 表")

        insert_sql = """
            INSERT INTO idle_resource (
                resource_name, resource_type, owner_id, description, images,
                location, original_price, rental_price, status, borrow_rules, views
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, 'available', %s, %s
            )
        """

        for res in RESOURCES:
            image_url = build_image_url(res["prompt"], res["size"])
            cursor.execute(insert_sql, (
                res["name"], res["type"], res["owner_id"], res["description"],
                image_url, res["location"], res["original_price"],
                res["rental_price"], res["borrow_rules"], res["views"]
            ))
            print(f"已插入资源: {res['name']}")

        conn.commit()
        print(f"\n共插入 {len(RESOURCES)} 条资源记录")
    except Exception as e:
        conn.rollback()
        print(f"操作失败: {e}")
        raise
    finally:
        cursor.close()
        conn.close()


if __name__ == "__main__":
    reset_resources()
