from pydantic import BaseModel
from typing import List
from model.image_item import ImageItem


class ImageSearchResponse(BaseModel):
    count: int
    results: List[ImageItem]
