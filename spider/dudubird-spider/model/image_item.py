from pydantic import BaseModel


class ImageItem(BaseModel):
    icon_path: str
    image_name: str
    platform: str
    source: str
    size: str
    collection_date: str
