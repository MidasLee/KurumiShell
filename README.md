# KurumiShell

<div align=center>
    <img src="./web/src/assets/images/kurumi-shell.png" width=16%>
</div>

> KurumiShell是一个集成了多种实用工具的平台，包括SSH连接管理、Docker镜像搜索和Markdown笔记管理等功能，旨在为用户提供便捷的开发和运维工具集。

<div align=center>
    <a href="./README.md">中文文档</a> | <a href="./README_EN.md">英文文档</a>
</div>

## 功能特点

- 🖥️ **SSH连接管理**：提供SSH连接分组管理、终端访问、文件管理和资源监控功能
- 🐳 **Docker工具**：集成Docker镜像搜索（渡渡鸟和轩辕镜像仓库）
- 📝 **Markdown笔记管理**：支持文件夹分类管理，实时预览和编辑Markdown文档
- 📊 **数据统计**：直观展示笔记文件夹和SSH连接分组的统计信息
- 🎨 **现代化UI**：基于Vue 3 + Naive UI构建的响应式界面
- 🔌 **实时通信**：使用WebSocket实现SSH终端和资源监控的实时通信

## 项目结构

KurumiShell项目采用前后端分离的架构设计，主要包含以下几大部分：

```
KurumiShell/
├── src/                # 后端源代码（Kotlin）
│   ├── main/kotlin/per/midas/kurumishell/  # 后端主代码
│   │   ├── entity/     # 实体类定义
│   │   ├── repository/ # 数据访问层
│   │   ├── service/    # 业务逻辑层
│   │   ├── controller/ # API控制器
│   │   ├── config/     # 配置类
│   │   └── websocket/  # WebSocket相关实现
│   └── test/           # 测试代码
├── web/                # 前端项目（Vue 3）
│   ├── src/            # 前端源代码
│   │   ├── components/ # 通用组件
│   │   ├── pages/      # 页面组件
│   │   ├── services/   # API服务
│   │   ├── App.vue     # 根组件
│   │   └── main.ts     # 入口文件
│   ├── package.json    # 前端依赖
│   └── vite.config.ts  # Vite配置
├── spider/             # 爬虫项目
│   ├── dudubird-spider/ # 渡渡鸟镜像仓库爬虫
│   └── xuanyuan-spider/ # 轩辕镜像仓库爬虫
├── build.gradle.kts    # Gradle构建配置
├── settings.gradle.kts # Gradle设置
└── README.md           # 项目说明文档
```

## 技术栈

### 后端
- **语言**：Kotlin
- **框架**：Spring Boot
- **数据库**：JPA (Hibernate)
- **WebSocket**：Spring WebSocket
- **构建工具**：Gradle

### 前端
- **框架**：Vue 3
- **UI组件库**：Naive UI
- **构建工具**：Vite
- **语言**：TypeScript
- **HTTP客户端**：Axios
- **图表库**：ECharts

### 爬虫
- **语言**：Python
- **框架**：FastAPI

## 环境要求

### 后端
- JDK 17+
- Gradle 7+

### 前端
- Node.js 18+
- npm 8+ 或 yarn 1.22+

### 爬虫
- Python 3.8+
- FastAPI

## 快速开始

### 后端启动

1. 克隆代码：

```shell
git clone https://github.com/MidasLee/KurumiShell.git
cd KurumiShell
```

2. 启动后端服务：

```shell
./gradlew bootRun
```

### 前端启动

1. 进入前端目录：

```shell
cd web
```

2. 安装依赖：

```shell
npm install
# 或
yarn install
```

3. 启动开发服务器：

```shell
npm run dev
# 或
yarn dev
```

4. 访问前端应用：

打开浏览器访问 `http://localhost:5666`

### 爬虫启动

1. 进入爬虫目录：

```shell
cd spider/dudubird-spider
# 或
cd spider/xuanyuan-spider
```

2. 安装依赖：

```shell
pip install -r requirements.txt
```

3. 启动爬虫服务：

```shell
python main.py
```

## 核心功能模块

### SSH连接管理

- **分组管理**：创建、编辑、删除SSH连接分组
- **连接管理**：创建、编辑、删除SSH连接配置
- **终端访问**：基于WebSocket的SSH终端
- **文件管理**：远程文件上传、下载和管理
- **资源监控**：实时监控服务器CPU、内存和磁盘使用情况

### Docker工具

- **渡渡鸟镜像搜索**：搜索渡渡鸟镜像仓库的Docker镜像
- **轩辕镜像搜索**：搜索轩辕镜像仓库的Docker镜像
- **拉取命令**：提供镜像拉取和标签命令

### Markdown笔记管理

- **文件夹管理**：创建、编辑、删除笔记文件夹
- **笔记管理**：创建、编辑、删除Markdown笔记
- **实时预览**：编辑时实时预览Markdown渲染效果
- **数据统计**：统计各文件夹下的笔记数量

## 配置说明

### 前端环境配置

前端配置文件位于 `web/.env.development`（开发环境）和 `web/.env.production`（生产环境）中，主要配置项包括：

```
# 应用标题
VITE_APP_TITLE = "KurumiShell"

# 前端服务端口
VITE_APP_PORT = 5666

# API基础URL
VITE_APP_BASE_URL = "http://localhost:8888"

# 渡渡鸟镜像仓库API地址
VITE_APP_DUDUBIRD_URL = "http://localhost:8166"

# 轩辕镜像仓库API地址
VITE_APP_XUANYUAN_URL = "http://localhost:8188"
```

### 后端配置

后端配置文件位于 `src/main/resources/application.yml` 中，主要配置项包括：

```yaml
# 服务器配置
server:
  port: 8888  # 后端服务端口
  servlet:
    context-path: /
    encoding:
      force: true
      charset: UTF-8
      enabled: true

# Spring应用配置
spring:
  application:
    name: KurumiShell
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: -1  # 不限制单个文件大小
      max-request-size: -1  # 不限制请求总大小
  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/kurumi-shell?useSSL=false&serverTimezone=UTC
    username: root
    password: Midas888
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update  # 自动更新表结构
    show-sql: true  # 显示SQL语句
    properties:
      hibernate:
        format_sql: true  # 格式化SQL语句

# 应用自定义配置
app:
  # 管理员用户配置
  admin:
    id: admin
    username: admin
    password: 666666
    email: admin@admin.com
  # JWT配置
  jwtSecret: XimKNNjYZkYmfw2th28zdj6ByeP3bwPa
  jwtExpirationMs: 86400000  # JWT过期时间（毫秒），默认24小时
  # CORS配置
  cors:
    allowed-origins:
      - "http://localhost:5666"
      - "http://127.0.0.1:5666"
```

## 开发指南

### 后端开发

1. 创建新实体类：在 `src/main/kotlin/per/midas/kurumishell/entity/` 目录下创建
2. 创建数据访问层：在 `src/main/kotlin/per/midas/kurumishell/repository/` 目录下创建接口
3. 创建业务逻辑层：在 `src/main/kotlin/per/midas/kurumishell/service/` 目录下创建服务类
4. 创建API控制器：在 `src/main/kotlin/per/midas/kurumishell/controller/` 目录下创建控制器

### 前端开发

1. 创建新页面：在 `web/src/pages/` 目录下创建Vue组件
2. 创建API服务：在 `web/src/pages/{module}/service/` 目录下创建API服务
3. 创建组件：在 `web/src/pages/{module}/component/` 目录下创建组件
4. 配置路由：在 `web/src/router/index.ts` 中配置路由

## 浏览器兼容性

- Chrome (最新版)
- Firefox (最新版)
- Safari (最新版)
- Edge (最新版)

## 注意事项

1. **安全提示**：生产环境中请确保修改默认密码，并配置适当的安全策略
2. **资源消耗**：SSH终端和资源监控功能会消耗一定的服务器资源，请根据实际情况调整配置
3. **网络要求**：WebSocket功能需要确保网络环境支持
4. **数据库**：首次启动时会自动创建数据库表结构

## 爬虫项目免责声明

### 数据来源说明

本项目中的爬虫模块（渡渡鸟爬虫和轩辕爬虫）用于从第三方网站获取Docker镜像相关数据，具体包括：

- **渡渡鸟爬虫**：从 `docker.aityp.com` 获取镜像数据
- **轩辕爬虫**：从 `dockers.xuanyuan.me` 和 `xuanyuan.cloud` 获取镜像数据

### 使用限制

1. **数据用途**：爬虫获取的数据仅供个人学习和研究使用，不得用于商业目的
2. **使用条款**：使用爬虫功能时，请遵守各第三方网站的使用条款、服务协议和隐私政策
3. **访问频率**：请勿通过本项目的爬虫功能对第三方网站进行高频访问，以免给对方服务器造成不必要的负担

### 法律责任

1. **数据所有权**：本项目不拥有、控制或保证任何第三方网站数据的准确性、完整性、及时性或可用性
2. **知识产权**：所有镜像数据的知识产权归各自的所有者所有，使用时请遵守相关的版权和知识产权法规
3. **免责声明**：本项目及其开发者不对因使用爬虫功能或第三方网站数据而产生的任何直接、间接、偶然、特殊或后果性的损害负责
4. **合规性**：用户有责任确保其使用本项目爬虫功能的行为符合所在国家和地区的法律法规

### 风险提示

1. **接口变更**：第三方网站的接口或页面结构可能随时变更，导致爬虫功能失效
2. **数据准确性**：本项目不对爬虫获取的数据的准确性和完整性做任何保证
3. **使用风险**：请谨慎使用爬虫获取的数据，对于基于这些数据做出的决策或行动，本项目不承担任何责任

## 依赖库授权协议

### 前端依赖

| 依赖名称 | 版本 | 授权协议 | 用途 |
|---------|------|---------|------|
| Vue | ^3.3.4 | MIT License | 前端框架 |
| Naive UI | ^2.42.0 | MIT License | UI组件库 |
| Axios | ^1.10.0 | MIT License | HTTP客户端 |
| ECharts | ^5.6.0 | Apache License 2.0 | 图表库 |
| @xterm/xterm | ^5.5.0 | MIT License | 终端模拟器 |
| xterm-addon-fit | ^0.8.0 | MIT License | Xterm终端适配插件 |
| @kangc/v-md-editor | ^2.3.18 | MIT License | Markdown编辑器 |
| Pinia | ^3.0.3 | MIT License | 状态管理 |
| pinia-plugin-persistedstate | ^4.4.1 | MIT License | Pinia持久化插件 |
| Vue Router | 4 | MIT License | 路由管理 |
| countup.js | ^2.9.0 | MIT License | 数字动画效果 |
| prismjs | ^1.30.0 | MIT License | 代码高亮 |
| element-resize-detector | ^1.2.4 | MIT License | DOM元素尺寸变化检测 |
| vue-hooks-plus | ^2.4.0 | MIT License | Vue Hooks工具库 |
| @vue/compiler-sfc | ^3.5.17 | MIT License | Vue单文件组件编译器 |
| vite | ^7.0.4 | MIT License | 前端构建工具 |
| typescript | ~5.8.3 | Apache License 2.0 | 类型检查语言 |
| @types/node | ^24.0.14 | MIT License | Node.js类型定义 |
| @types/prismjs | ^1.26.5 | MIT License | Prismjs类型定义 |
| @vicons/* | ^0.13.0 | MIT License | 图标库 |
| sass | ^1.89.2 | MIT License | CSS预处理器 |
| vue-tsc | ^2.2.12 | MIT License | Vue TypeScript检查工具 |

### 后端依赖

| 依赖名称 | 版本 | 授权协议 | 用途 |
|---------|------|---------|------|
| Spring Boot | 3.5.3 | Apache License 2.0 | 后端框架 |
| Spring Boot Starter Data JPA | 3.5.3 | Apache License 2.0 | JPA数据访问支持 |
| Spring Boot Starter Web | 3.5.3 | Apache License 2.0 | Web应用支持 |
| Spring Boot Starter WebSocket | 3.5.3 | Apache License 2.0 | WebSocket支持 |
| Spring Boot Starter Security | 3.5.3 | Apache License 2.0 | 安全框架支持 |
| Kotlin | 1.9.25 | Apache License 2.0 | 编程语言 |
| Jackson Module Kotlin | - | Apache License 2.0 | Jackson与Kotlin集成 |
| JPA (Hibernate) | - | GNU Lesser General Public License v2.1 | ORM框架 |
| JJWT API | 0.12.6 | Apache License 2.0 | JWT认证API |
| JJWT Implementation | 0.12.6 | Apache License 2.0 | JWT认证实现 |
| JJWT Jackson | 0.12.6 | Apache License 2.0 | JWT Jackson支持 |
| MySQL Connector/J | - | GNU General Public License v2.0 with Classpath Exception | MySQL数据库驱动 |
| Jakarta Bean Validation API | 3.1.1 | Apache License 2.0 | Bean验证API |
| JSch | 2.27.2 | BSD 3-Clause License | SSH连接库 |
| Kotlin Test JUnit5 | - | Apache License 2.0 | Kotlin JUnit5测试支持 |
| Spring Boot Test | 3.5.3 | Apache License 2.0 | Spring Boot测试支持 |

### 爬虫依赖

| 依赖名称 | 版本 | 授权协议 | 用途 |
|---------|------|---------|------|
| annotated-types | ^0.7.0 | MIT License | 类型注解支持 |
| anyio | ^4.9.0 | MIT License | 异步I/O支持 |
| attrs | ^25.3.0 | MIT License | 类定义简化库 |
| beautifulsoup4 | ^4.13.4 | MIT License | HTML解析库 |
| certifi | ^2025.6.15 | Mozilla Public License 2.0 | SSL证书验证 |
| charset-normalizer | ^3.4.2 | MIT License | 字符集检测 |
| click | ^8.2.1 | BSD 3-Clause License | 命令行接口框架 |
| fastapi | ^0.115.14 | MIT License | API框架 |
| h11 | ^0.16.0 | MIT License | HTTP/1.1协议实现 |
| httpcore | ^1.0.9 | BSD 3-Clause License | HTTP客户端核心 |
| httpx | ^0.28.1 | BSD 3-Clause License | HTTP客户端 |
| idna | ^3.10 | BSD 3-Clause License | 国际化域名支持 |
| outcome | ^1.3.0 | MIT License | 异步操作结果处理 |
| packaging | ^25.0 | Apache License 2.0 | 包管理工具 |
| pydantic | ^2.11.7 | MIT License | 数据验证库 |
| pydantic_core | ^2.33.2 | MIT License | Pydantic核心实现 |
| PySocks | ^1.7.1 | BSD 3-Clause License | SOCKS代理支持 |
| python-dotenv | ^1.1.1 | BSD 3-Clause License | 环境变量加载 |
| requests | ^2.32.4 | Apache License 2.0 | HTTP客户端 |
| selenium | ^4.34.1 | Apache License 2.0 | 网页自动化测试 |
| setuptools | ^78.1.1 | MIT License | Python包安装工具 |
| sniffio | ^1.3.1 | MIT License | 异步I/O库检测 |
| sortedcontainers | ^2.4.0 | Apache License 2.0 | 高效有序容器 |
| soupsieve | ^2.7 | MIT License | CSS选择器库 |
| starlette | ^0.46.2 | BSD 3-Clause License | ASGI框架 |
| trio | ^0.30.0 | MIT License | 异步编程库 |
| trio-websocket | ^0.12.2 | MIT License | WebSocket支持 |
| typing-inspection | ^0.4.1 | MIT License | 类型检查工具 |
| typing_extensions | ^4.14.1 | MIT License | 类型扩展支持 |
| urllib3 | ^2.5.0 | MIT License | HTTP客户端库 |
| uvicorn | ^0.35.0 | BSD 3-Clause License | ASGI服务器 |
| webdriver-manager | ^4.0.2 | Apache License 2.0 | WebDriver管理工具 |
| websocket-client | ^1.8.0 | Apache License 2.0 | WebSocket客户端 |
| wheel | ^0.45.1 | MIT License | Python包打包工具 |
| wsproto | ^1.2.0 | MIT License | WebSocket协议实现 |

## 第三方库使用声明

本项目使用了多种开源依赖库，这些库遵循不同的开源许可证。以下是对主要依赖库的许可证说明和使用声明：

1. **GNU Lesser General Public License v2.1 (LGPL v2.1) 依赖**
   - **Hibernate (JPA)**：本项目使用Hibernate作为JPA实现，采用动态链接方式使用，未修改其源代码。根据LGPL v2.1条款，用户可以自由使用、分发本项目，但如果修改了Hibernate本身的代码，则需要按LGPL v2.1条款发布修改后的代码。

2. **GNU General Public License v2.0 with Classpath Exception (GPL v2 with Classpath Exception) 依赖**
   - **MySQL Connector/J**：本项目使用MySQL Connector/J作为数据库驱动。根据Classpath Exception条款，允许在任何许可证下的应用程序中使用该驱动，只要满足以下条件：
     - 该驱动仅通过标准Java API与应用程序交互
     - 应用程序代码不直接依赖于驱动的内部实现

3. **Apache License 2.0 依赖**
   - 本项目使用的Spring Boot、Kotlin、JJWT、Jakarta Bean Validation API、TypeScript等核心依赖遵循Apache License 2.0
   - 使用要求：
     - 保留原始版权声明和许可证文件
     - 标明修改情况（如适用）
     - 不得使用原作者的名称进行产品推广

4. **MIT License 依赖**
   - 本项目前端的Vue、Naive UI、Axios、ECharts、Xterm等主要依赖遵循MIT License
   - 爬虫项目中的annotated-types、anyio、attrs、beautifulsoup4、charset-normalizer、fastapi、h11、outcome、pydantic、pydantic_core、setuptools、sniffio、soupsieve、trio、typing-inspection、typing_extensions、urllib3、wheel、wsproto等依赖遵循MIT License
   - 使用要求：
     - 保留原始版权声明和许可证文件
     - 允许自由使用、修改、分发和销售
     - 不提供任何担保

5. **BSD 3-Clause License 依赖**
   - **JSch**：本项目使用JSch库实现SSH连接功能，遵循BSD 3-Clause License
   - 爬虫项目中的click、httpcore、httpx、idna、starlette、uvicorn、PySocks、python-dotenv等依赖遵循BSD 3-Clause License
   - 使用要求：
     - 保留原始版权声明和许可证文件
     - 不得使用原作者的名称进行产品推广
     - 提供许可证文件副本

6. **Mozilla Public License 2.0 依赖**
   - **certifi**：爬虫项目使用certifi库进行SSL证书验证，遵循Mozilla Public License 2.0
   - 使用要求：
     - 保留原始版权声明和许可证文件
     - 衍生作品需在相同许可证下发布
     - 修改的源代码需公开

本项目在发布时已包含所有直接依赖库的许可证文件副本，位于各自的依赖包目录中。用户在使用、修改或分发本项目时，请确保遵守所有依赖库的许可证条款。

## 许可证

本项目采用 <a href="./LICENSE">MIT 许可证</a>。

### MIT许可证的优势
1. **高度自由**：允许任何人自由地使用、复制、修改、合并、发布、分发、转授权及销售软件副本
2. **商业友好**：无限制地支持商业用途，非常适合用于商业项目
3. **简洁明确**：协议文本简短易懂，降低了合规风险
4. **促进传播**：宽松的条款有助于项目的广泛传播和采用
5. **保留归属**：仅要求保留原作者的版权声明和许可声明，维护了作者的基本权益
