#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
读取5个飞书Wiki文档（Day1-Day5）的原始内容，保存JSON并提取图片token。
"""

import json
import os
import re
import subprocess

# 飞书Wiki文档URL配置
DOCS = {
    "day1": "https://my.feishu.cn/wiki/U470wwuJMiYC1VkaVj5cfRZfnAp",
    "day2": "https://my.feishu.cn/wiki/G0cFwodjmizTV6kQQZCcxQl2nLc",
    "day3": "https://my.feishu.cn/wiki/RI8cwflWDiy2RXksTHMc9hM7nue",
    "day4": "https://my.feishu.cn/wiki/In5Gw2hS3ix3KNkXxQ0cdAXXnko",
    "day5": "https://my.feishu.cn/wiki/XqstwgeKkivjIsky8hdc1rSAnde",
}

OUTPUT_DIR = r"C:\Users\luofeng\Desktop\实训"


def fetch_doc(url: str) -> dict:
    """
    调用 lark-cli docs +fetch 获取文档JSON内容。
    """
    cmd = [
        "lark-cli", "docs", "+fetch",
        "--api-version", "v2",
        "--doc", url,
        "--doc-format", "markdown",
        "--scope", "full",
    ]
    result = subprocess.run(" ".join(cmd), capture_output=True, text=True, encoding="utf-8", shell=True)
    if result.returncode != 0:
        print(f"获取文档失败: {url}\n{result.stderr}")
        return {}
    try:
        return json.loads(result.stdout)
    except json.JSONDecodeError as e:
        print(f"JSON解析失败: {e}\n输出前500字符: {result.stdout[:500]}")
        return {}


def extract_image_tokens(content: str) -> list:
    """
    从文档内容中提取图片token。
    """
    # 匹配 <image token="..."/> 或 <image token="..." ...>
    pattern = r'<image\s+token="([^"]+)"'
    return re.findall(pattern, content)


def main():
    for day, url in DOCS.items():
        print(f"\n正在读取 {day} 文档: {url}")
        data = fetch_doc(url)
        if not data or not data.get("ok"):
            print(f"{day} 读取失败")
            continue

        document = data.get("data", {}).get("document", {})
        content = document.get("content", "")
        doc_id = document.get("document_id", "")
        rev_id = document.get("revision_id", "")

        # 保存原始JSON
        json_path = os.path.join(OUTPUT_DIR, f"{day}_doc.json")
        with open(json_path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  JSON已保存: {json_path}")

        # 保存纯内容文本
        content_path = os.path.join(OUTPUT_DIR, f"{day}_content.md")
        with open(content_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"  内容已保存: {content_path}")

        # 提取图片token
        image_tokens = extract_image_tokens(content)
        print(f"  文档ID: {doc_id}, 版本: {rev_id}")
        print(f"  内容长度: {len(content)} 字符")
        print(f"  图片数量: {len(image_tokens)}")
        for token in image_tokens:
            print(f"    - image token: {token}")


if __name__ == "__main__":
    main()
