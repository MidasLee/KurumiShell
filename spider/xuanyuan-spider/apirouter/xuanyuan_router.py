from fastapi import APIRouter, HTTPException
from typing import Optional
import re
from fastapi.responses import JSONResponse
from util.image_info_util import fetch_html, parse_html
import httpx
from bs4 import BeautifulSoup

xuanyuan_router = APIRouter()

# 官网已更新页面，该接口当前已经失效
@xuanyuan_router.get("/search")
async def search_images(q: str, filter: Optional[str] = "", page: int = 1):
    # 构建请求URL
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    url = f"https://dockers.xuanyuan.me/search?q={q}&filter={filter}&page={page}"

    try:
        # 发送HTTP请求
        async with httpx.AsyncClient() as client:
            response = await client.get(url, headers=headers)
            response.raise_for_status()

        # 解析HTML
        soup = BeautifulSoup(response.text, 'html.parser')

        # 提取总页码
        pagination = soup.find('nav', {'aria-label': 'pagination'})
        total_pages = page  # 默认为当前页码

        if pagination:
            page_links = pagination.find_all('a')
            page_numbers = []

            for link in page_links:
                if link.text.isdigit():
                    page_numbers.append(int(link.text))

            if page_numbers:
                total_pages = max(page_numbers)
            else:
                # 如果没有找到页码链接，可能是只有一页
                total_pages = 1
        else:
            total_pages = 1

        # 提取镜像信息
        images = []
        image_cards = soup.select('.grid.gap-4 a.block')

        for card in image_cards:
            # 镜像链接
            image_link = card['href'] if 'href' in card.attrs else ""

            # 镜像名称和作者
            name = card.select_one(
                'h3.font-semibold.text-primary').text.strip()
            author = card.select_one(
                'p.text-sm.text-muted-foreground').text.strip()

            # 图标链接
            img_tag = card.select_one('img.rounded-full')
            icon_url = img_tag['src'] if img_tag and 'src' in img_tag.attrs else ""

            # 镜像介绍
            description_tag = card.select_one('p.text-sm.mb-4.line-clamp-2')
            description = description_tag.text.strip() if description_tag else ""

            # star数量
            star_tag = card.select_one('div.flex.items-center.text-sm')
            stars = star_tag.text.strip() if star_tag else "0"
            stars = re.sub(r'[^\d]', '', stars)  # 提取纯数字

            # 拉取数量
            pulls_tag = card.select_one('div.text-sm.text-muted-foreground')
            pulls = pulls_tag.text.strip() if pulls_tag else "0"

            # 镜像标签
            tag = "none"
            tag_divs = card.find_all(
                'div', class_=lambda x: x and 'rounded-md' in x and 'border' in x and 'px-2.5' in x and 'py-0.5' in x)
            if tag_divs:
                tag_div = tag_divs[0]
                if "Official" in tag_div.text:
                    tag = "Official"
                elif "Verified" in tag_div.text:
                    tag = "Verified"
                elif "open_source" in tag_div.text:
                    tag = "open_source"

            images.append({
                "name": name,
                "author": author,
                "image_link": image_link,
                "icon_url": icon_url,
                "description": description,
                "stars": int(stars) if stars else 0,
                "pulls": pulls,
                "tag": tag
            })

        return {
            "current_page": page,
            "total_pages": total_pages,
            "images": images
        }

    except httpx.HTTPStatusError as e:
        raise HTTPException(status_code=e.response.status_code, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 官网已更新页面，该接口当前已经失效
@xuanyuan_router.get("/image_info")
async def get_image_info(image_name: str):
    try:
        html_content = await fetch_html(image_name)
        data = parse_html(html_content)
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@xuanyuan_router.get("/image_tags")
async def get_image_info(image_name: str, tag_name: Optional[str] = "", page: int = 1, page_size: int = 25):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    url = f'https://dockers.xuanyuan.me/api/tags?url=https%3A%2F%2Fhub.docker.com%2Fv2%2Frepositories%2F{image_name}%2Ftags%3Fname%3D{tag_name}%26ordering%3Dlast_updated%26page%3D{page}%26page_size%3D{page_size}'
    try:
        # 发送HTTP请求
        async with httpx.AsyncClient() as client:
            response = await client.get(url, headers=headers)
            response.raise_for_status()
        data = response.json()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@xuanyuan_router.get("/v2/search")
async def v2_search_images(image_name: str, page: int = 1, page_size: int = 25):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    url = f'https://xuanyuan.cloud/api/docker/searchv4?q={image_name}&page={page}&limit={page_size}'
    try:
        # 发送HTTP请求
        async with httpx.AsyncClient() as client:
            response = await client.get(url, headers=headers)
            response.raise_for_status()
        data = response.json()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@xuanyuan_router.get("/v2/image_tags")
async def v2_search_images(namespace: str, name: str, tag: str):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    url = f'https://xuanyuan.cloud/api/docker/filter?namespace={namespace}&name={name}&tag={tag}'
    try:
        # 发送HTTP请求
        async with httpx.AsyncClient() as client:
            response = await client.get(url, headers=headers)
            response.raise_for_status()
        data = response.json()
        return JSONResponse(content=data)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
