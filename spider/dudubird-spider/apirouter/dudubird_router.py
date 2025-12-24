from fastapi import APIRouter, HTTPException
from bs4 import BeautifulSoup
from util.image_info_util import extract_image_info, safe_find
from model.image_search_response import ImageSearchResponse
from model.image_info import ImageInfo
import json
import requests
import logging

logging.basicConfig(
    level=logging.INFO,  # 设置日志级别为 INFO
    format="%(levelname)s - %(asctime)s - %(message)s",  # 设置日志格式
    handlers=[logging.StreamHandler()]  # 输出到控制台
)
logger = logging.getLogger(__name__)


dudubird_router = APIRouter()


@dudubird_router.get("/search_images", response_model=ImageSearchResponse)
async def search_images(
    search: str,
    site: str = "All",
    platform: str = "All",
    sort: str = "名称排序"
):
    """
    搜索Docker镜像信息

    参数:
    - search: 搜索关键词 (如: elasticsearch:7.17)
    - site: 镜像源 (默认: All)
    - site可选项：
    - All
    - gcr.io
    - ghcr.io
    - quay.io
    - k8s.gcr.io
    - docker.io
    - registry.k8s.io
    - docker.elastic.co
    - skywalking.docker.scarf.sh
    - mcr.microsoft.com
    - platform: 架构 (默认: All)
    - platform可选项：
    - All
    - linux/386
    - linux/amd64
    - linux/arm64
    - linux/arm
    - linux/ppc64le
    - linux/s390x
    - linux/mips64le
    - linux/riscv64
    - linux/loong64
    - sort: 排序方式 (默认: 名称排序)
    - sort可选项：
    - 名称排序
    - 镜像大小
    - 浏览量
    - 最近同步
    - 最早同步
    """
    try:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }
        url = f"https://docker.aityp.com/i/search?site={
            site}&platform={platform}&sort={sort}&search={search}"
        logger.info(f"Fetching URL: {url}")
        response = requests.get(url, headers=headers)
        response.raise_for_status()

        image_infos = extract_image_info(response.text)

        return {
            "count": len(image_infos),
            "results": image_infos
        }
    except requests.RequestException as e:
        raise HTTPException(status_code=400, detail=f"请求目标网站失败: {str(e)}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"服务器内部错误: {str(e)}")


@dudubird_router.get("/image_info")
async def image_info(image_name: str):
    try:
        # 模拟浏览器访问
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }

        url = f"https://docker.aityp.com/image/{image_name}"
        logger.info(f"Fetching URL: {url}")

        response = requests.get(url, headers=headers)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, 'html.parser')

        # 改进的表格数据提取方法
        def get_table_value(label):
            try:
                # 查找包含指定文本的td元素
                for td in soup.find_all('td'):
                    if td.text.strip() and label in td.text.strip():
                        next_td = td.find_next_sibling('td')
                        if next_td:
                            # 对于有badge的情况
                            badge = next_td.find('span', class_='badge')
                            if badge:
                                return badge.get_text(strip=True)
                            # 对于链接的情况
                            link = next_td.find('a')
                            if link:
                                return link.get_text(strip=True)
                            return next_td.get_text(strip=True)
                return "N/A"
            except Exception as e:
                logger.warning(
                    f"Error extracting table value for {label}: {e}")
                return "N/A"

        # 提取镜像详情
        image_details = {"error": "Details not found"}
        details_code = safe_find(soup, 'find', 'code', id='codeBlock4')
        if details_code:
            try:
                image_details = json.loads(details_code.get_text(strip=True))
            except json.JSONDecodeError as e:
                logger.error(f"Failed to parse image details: {e}")
                image_details = {"error": f"Invalid JSON: {str(e)}"}

        # 提取卡片内容
        def get_card_content(title):
            card_header = safe_find(
                soup, 'find', 'div', class_='card-header', string=title)
            if card_header:
                card_body = card_header.find_next('div', class_='card-body')
                if card_body:
                    return [badge.get_text(strip=True) for badge in card_body.find_all('span', class_='badge')]
            return []

        # 构建响应数据
        info = ImageInfo(
            source_image=get_table_value("源镜像"),
            domestic_image=get_table_value("国内镜像"),
            image_id=get_table_value("镜像ID"),
            image_tag=get_table_value("镜像TAG"),
            size=get_table_value("大小"),
            image_source=get_table_value("镜像源"),
            project_info=get_table_value("项目信息"),
            cmd=get_table_value("CMD"),
            entrypoint=get_table_value("启动入口"),
            working_dir=get_table_value("工作目录"),
            os_platform=get_table_value("OS/平台"),
            views=get_table_value("浏览量"),
            created_at=get_table_value("镜像创建"),
            sync_time=get_table_value("同步时间"),
            updated_at=get_table_value("更新时间"),
            open_ports=get_card_content("开放端口"),
            env_vars=get_card_content("环境变量"),
            image_labels=get_card_content("镜像标签"),
            docker_pull_cmd=safe_find(soup, 'find', 'code', id='codeBlock1').get_text(
                strip=True) if safe_find(soup, 'find', 'code', id='codeBlock1') else "N/A",
            containerd_pull_cmd=safe_find(soup, 'find', 'code', id='codeBlock2').get_text(
                strip=True) if safe_find(soup, 'find', 'code', id='codeBlock2') else "N/A",
            shell_replace_cmd=safe_find(soup, 'find', 'code', id='codeBlock3').get_text(
                strip=True) if safe_find(soup, 'find', 'code', id='codeBlock3') else "N/A",
            ansible_docker_cmd=safe_find(soup, 'find', 'code', id='codeBlockAnsibleDocker').get_text(
                strip=True) if safe_find(soup, 'find', 'code', id='codeBlockAnsibleDocker') else "N/A",
            ansible_containerd_cmd=safe_find(soup, 'find', 'code', id='codeBlockAnsibleContainerd').get_text(
                strip=True) if safe_find(soup, 'find', 'code', id='codeBlockAnsibleContainerd') else "N/A",
            build_history=safe_find(soup, 'find', 'code', id='codeBlock6').get_text(
                strip=True) if safe_find(soup, 'find', 'code', id='codeBlock6') else "N/A",
            image_details=image_details
        )

        return info.model_dump()

    except requests.RequestException as e:
        logger.error(f"Request error: {e}")
        raise HTTPException(
            status_code=500, detail=f"Request failed: {str(e)}")
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        raise HTTPException(
            status_code=500, detail=f"Error processing request: {str(e)}")
