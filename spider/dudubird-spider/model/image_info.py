from pydantic import BaseModel
from typing import List, Dict


class ImageInfo(BaseModel):
    source_image: str
    domestic_image: str
    image_id: str
    image_tag: str
    size: str
    image_source: str
    project_info: str
    cmd: str
    entrypoint: str
    working_dir: str
    os_platform: str
    views: str
    created_at: str
    sync_time: str
    updated_at: str
    open_ports: List[str]
    env_vars: List[str]
    image_labels: List[str]
    docker_pull_cmd: str
    containerd_pull_cmd: str
    shell_replace_cmd: str
    ansible_docker_cmd: str
    ansible_containerd_cmd: str
    build_history: str
    image_details: Dict
