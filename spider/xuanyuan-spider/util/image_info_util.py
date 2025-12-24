from fastapi import HTTPException
import httpx
from bs4 import BeautifulSoup

def extract_with_selenium(url):
    options = webdriver.ChromeOptions()
    options.add_argument('--headless')  # 无头模式
    driver = webdriver.Chrome(options=options)

    try:
        driver.get(url)
        # 等待页面加载完成
        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located(
                (By.XPATH, "//button[contains(text(),'详细说明')]"))
        )
        # 点击"详细说明"Tab
        driver.find_element(
            By.XPATH, "//button[contains(text(),'详细说明')]").click()
        # 等待内容加载
        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CLASS_NAME, "prose"))
        )
        # 获取渲染后的HTML
        return driver.page_source
    finally:
        driver.quit()


async def fetch_html(image_name: str):
    url = f"https://dockers.xuanyuan.me/image/{image_name}"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, headers=headers)
            response.raise_for_status()
            return response.text
        except httpx.HTTPStatusError as e:
            raise HTTPException(
                status_code=404, detail=f"Image not found: {e}")


def parse_html(html_content: str):
    soup = BeautifulSoup(html_content, 'html.parser')

    # result = {
    #     "image_src": None,
    #     "title": None,
    #     "image_type": None,
    #     "stats": [],
    #     "description": None,
    #     "tags": [],
    #     "documentation": None
    # }

    result = {
        "image_src": None,
        "title": None,
        "image_type": None,
        "stats": [],
        "description": None,
        "tags": []
    }

    # 提取顶部卡片信息
    card = soup.find(
        'div', class_=lambda x: x and 'bg-card' in x and 'shadow' in x)

    if card:
        # 提取图标URL
        img_tag = card.find('img')
        if img_tag:
            result["image_src"] = img_tag.get('src', None)

        # 提取标题和类型
        title_tag = card.find('h1', class_='text-2xl font-bold text-primary')
        if not title_tag:
            title_tag = card.find('h1')
        if title_tag:
            result["title"] = title_tag.get_text(strip=True)

        type_tag = card.find('p', class_='text-muted-foreground')
        if not type_tag:
            type_tag = card.find('p', class_=lambda x: x and 'text-muted' in x)
        if type_tag:
            result["image_type"] = type_tag.get_text(strip=True)

        # 提取统计信息
        stat_divs = card.find_all('div', class_='flex items-center gap-2')
        for div in stat_divs:
            result["stats"].append(div.get_text(" ", strip=True))

        # 提取描述
        desc_tag = card.find('p', class_='text-lg mb-4')
        if not desc_tag:
            desc_tag = card.find('p', class_=lambda x: x and 'text-lg' in x)
        if desc_tag:
            result["description"] = desc_tag.get_text(strip=True)

        # 提取标签
        tags = card.find_all(
            'div', class_='inline-flex items-center rounded-md border px-2.5 py-0.5 text-xs font-semibold')
        if not tags:
            tags = card.find_all(
                'div', class_=lambda x: x and 'inline-flex' in x and 'rounded-md' in x)
        result["tags"] = [tag.get_text(strip=True) for tag in tags]

        # doc_panel = soup.find('div', {'data-state': 'active'})
        # doc_content = ""
        # if doc_panel:
        #     doc_content = doc_panel.get_text(strip=True)
        # result["documentation"] = doc_content

    return result