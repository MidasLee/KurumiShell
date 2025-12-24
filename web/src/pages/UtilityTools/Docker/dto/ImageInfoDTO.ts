export type ImageInfoDTO = {
  source_image: string
  domestic_image: string
  image_id: string
  image_tag: string
  size: string
  image_source: string
  project_info: string
  cmd: string
  entrypoint: string
  working_dir: string
  os_platform: string
  views: string
  created_at: string
  sync_time: string
  updated_at: string
  open_ports: string[]
  env_vars: string[]
  image_labels: string[]
  docker_pull_cmd: string
  containerd_pull_cmd: string
  shell_replace_cmd: string
  ansible_docker_cmd: string
  ansible_containerd_cmd: string
  build_history: string
  image_details: ImageDetailsDTO
}

export type ImageDetailsDTO = {
  Id: string
  RepoTags: string[]
  RepoDigests: string[]
  Parent: string
  Comment: string
  Created: string
  Container: string
  ContainerConfig: null | any
  DockerVersion: string
  Author: string
  Config: {
    Hostname: string
    Domainname: string
    User: string
    AttachStdin: boolean
    AttachStdout: boolean
    AttachStderr: boolean
    ExposedPorts: Record<string, any>
    Tty: boolean
    OpenStdin: boolean
    StdinOnce: boolean
    Env: string[]
    Cmd: string[]
    ArgsEscaped: boolean
    Image: string
    Volumes: null | any
    WorkingDir: string
    Entrypoint: null | string[]
    OnBuild: string[]
    Labels: Record<string, any>
  }
  Architecture: string
  Os: string
  Size: number
  GraphDriver: {
    Data: {
      LowerDir: string
      MergedDir: string
      UpperDir: string
      WorkDir: string
    }
    Name: string
  }
  RootFS: {
    Type: string
    Layers: string[]
  }
  Metadata: {
    LastTagTime: string
  }
}
