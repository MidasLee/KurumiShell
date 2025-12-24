from bs4 import BeautifulSoup


def extract_image_info(html_content):
    soup = BeautifulSoup(html_content, 'html.parser')
    image_cards = soup.find_all('div', class_='card')

    image_info_list = []

    for card in image_cards:
        icon = card.find('img', class_='rounded-circle svg-container')
        icon_path = icon['src'] if icon else None

        name_tag = card.find('a', target='_blank')
        image_name = name_tag.text if name_tag else None

        badges = card.find_all('span', class_='badge')
        platform = None
        source = None
        size = None
        date = None

        for badge in badges:
            badge_text = badge.text.strip()
            if 'linux/' in badge_text:
                platform = badge_text
            elif any(x in badge_text for x in ['docker.io', 'docker.elastic.co', 'ghcr.io', 'quay.io', 'gcr.io', 'k8s.gcr.io', 'registry.k8s.io']):
                source = badge_text
            elif 'MB' in badge_text or 'GB' in badge_text:
                size = badge_text
            elif '-' in badge_text and ':' in badge_text:
                date = badge_text

        if all([icon_path, image_name, platform, source, size, date]):
            image_info = {
                'icon_path': 'https://docker.aityp.com/'+icon_path,
                'image_name': image_name,
                'platform': platform,
                'source': source,
                'size': size,
                'collection_date': date
            }
            image_info_list.append(image_info)

    return image_info_list


def safe_find(soup, method, *args, **kwargs):
    """安全查找元素，避免抛出异常"""
    try:
        result = getattr(soup, method)(*args, **kwargs)
        return result if result else None
    except Exception as e:
        logger.warning(f"Error finding element: {e}")
        return None
