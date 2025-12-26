# KurumiShell

<div align=center>
    <img src="./web/src/assets/images/kurumi-shell.png" width=16%>
</div>

> KurumiShell is a platform that integrates various practical tools, including SSH connection management, Docker image search, and Markdown note management, aiming to provide users with convenient development and operation tools.

<div align=center>
    <a href="./README.md">Chinese Documentation</a> | <a href="./README_EN.md">English Documentation</a>
</div>

## Features

- 🖥️ **SSH Connection Management**: Provides SSH connection grouping management, terminal access, file management, and resource monitoring functions
- 🐳 **Docker Tools**: Integrates Docker image search (Dudubird and Xuanyuan image repositories)
- 📝 **Markdown Note Management**: Supports folder classification management, real-time preview, and editing of Markdown documents
- 📊 **Data Statistics**: Intuitively displays statistical information about note folders and SSH connection groups
- 🎨 **Modern UI**: Responsive interface built based on Vue 3 + Naive UI
- 🔌 **Real-time Communication**: Uses WebSocket to implement real-time communication for SSH terminals and resource monitoring

## Project Structure

The KurumiShell project adopts a front-end and back-end separation architecture design, mainly consisting of following parts:

```
KurumiShell/
├── src/                # Back-end source code (Kotlin)
│   ├── main/kotlin/per/midas/kurumishell/  # Back-end main code
│   │   ├── entity/     # Entity class definitions
│   │   ├── repository/ # Data access layer
│   │   ├── service/    # Business logic layer
│   │   ├── controller/ # API controllers
│   │   ├── config/     # Configuration classes
│   │   └── websocket/  # WebSocket related implementations
│   └── test/           # Test code
├── web/                # Front-end project (Vue 3)
│   ├── src/            # Front-end source code
│   │   ├── components/ # Common components
│   │   ├── pages/      # Page components
│   │   ├── services/   # API services
│   │   ├── App.vue     # Root component
│   │   └── main.ts     # Entry file
│   ├── package.json    # Front-end dependencies
│   └── vite.config.ts  # Vite configuration
├── spider/             # Spider projects
│   ├── dudubird-spider/ # Dudubird image repository spider
│   └── xuanyuan-spider/ # Xuanyuan image repository spider
├── build.gradle.kts    # Gradle build configuration
├── settings.gradle.kts # Gradle settings
└── README.md           # Project documentation
```

## Technology Stack

### Back-end
- **Language**: Kotlin
- **Framework**: Spring Boot
- **Database**: JPA (Hibernate)
- **WebSocket**: Spring WebSocket
- **Build Tool**: Gradle

### Front-end
- **Framework**: Vue 3
- **UI Component Library**: Naive UI
- **Build Tool**: Vite
- **Language**: TypeScript
- **HTTP Client**: Axios
- **Chart Library**: ECharts

### Spider
- **Language**: Python
- **Framework**: FastAPI

## Environment Requirements

### Back-end
- JDK 17+
- Gradle 7+

### Front-end
- Node.js 18+
- npm 8+ or yarn 1.22+

### Spider
- Python 3.8+
- FastAPI

## Quick Start

### Back-end Startup

1. Clone the code:

```shell
git clone https://github.com/MidasLee/KurumiShell.git
cd KurumiShell
```

2. Start the back-end service:

```shell
./gradlew bootRun
```

### Front-end Startup

1. Enter the front-end directory:

```shell
cd web
```

2. Install dependencies:

```shell
npm install
# or
yarn install
```

3. Start the development server:

```shell
npm run dev
# or
yarn dev
```

4. Access the front-end application:

Open your browser and visit `http://localhost:5666`

### Spider Startup

1. Enter the spider directory:

```shell
cd spider/dudubird-spider
# or
cd spider/xuanyuan-spider
```

2. Install dependencies:

```shell
pip install -r requirements.txt
```

3. Start the spider service:

```shell
python main.py
```

## Core Function Modules

### SSH Connection Management

- **Group Management**: Create, edit, and delete SSH connection groups
- **Connection Management**: Create, edit, and delete SSH connection configurations
- **Terminal Access**: WebSocket-based SSH terminal
- **File Management**: Remote file upload, download, and management
- **Resource Monitoring**: Real-time monitoring of server CPU, memory, and disk usage

### Docker Tools

- **Dudubird Image Search**: Search for Docker images from Dudubird image repository
- **Xuanyuan Image Search**: Search for Docker images from Xuanyuan image repository
- **Pull Commands**: Provide image pull and tag commands

### Markdown Note Management

- **Folder Management**: Create, edit, and delete note folders
- **Note Management**: Create, edit, and delete Markdown notes
- **Real-time Preview**: Real-time preview of Markdown rendering effects during editing
- **Data Statistics**: Count the number of notes in each folder

## Configuration Instructions

### Front-end Environment Configuration

Front-end configuration files are located in `web/.env.development` (development environment) and `web/.env.production` (production environment), with the main configuration items including:

```
# Application title
VITE_APP_TITLE = "KurumiShell"

# Front-end service port
VITE_APP_PORT = 5666

# API base URL
VITE_APP_BASE_URL = "http://localhost:8888"

# Dudubird image repository API URL
VITE_APP_DUDUBIRD_URL = "http://localhost:8166"

# Xuanyuan image repository API URL
VITE_APP_XUANYUAN_URL = "http://localhost:8188"
```

### Back-end Configuration

Back-end configuration files are located in `src/main/resources/application.yml`, with the main configuration items including:

```yaml
server:
  port: 8888
  servlet:
    context-path: /
    encoding:
      force: true
      charset: UTF-8
      enabled: true
spring:
  application:
    name: KurumiShell
  servlet:
    multipart:
      max-file-size: -1
      max-request-size: -1
  profiles:
    active: mysql  # Must specify: mysql or sqlite

# Global application configuration (profile-independent)
app:
  admin:
    id: admin
    username: admin
    password: 666666
    email: admin@admin.com
  jwtSecret: XimKNNjYZkYmfw2th28zdj6ByeP3bwPa
  jwtExpirationMs: 86400000  # 24 hours
  cors:
    allowed-origins:
      - "http://localhost:5666"
      - "http://127.0.0.1:5666"

---
# MySQL Profile
spring:
  config:
    activate:
      on-profile: mysql
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/kurumi-shell?useSSL=false&serverTimezone=UTC
    username: root
    password: Midas888
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

---
# SQLite Profile  
spring:
  config:
    activate:
      on-profile: sqlite
  datasource:
    driver-class-name: org.sqlite.JDBC
    url: jdbc:sqlite:./kurumi-shell.db
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.community.dialect.SQLiteDialect
```

## Database Configuration

This project supports both MySQL and SQLite databases, allowing flexible switching based on environment needs.

### Quick Switching Methods

#### 1. Using MySQL Database

Start the application using the mysql profile:
```bash
# Method 1: Command line arguments
./gradlew bootRun --args='--spring.profiles.active=mysql'

# Method 2: Environment variables
export SPRING_PROFILES_ACTIVE=mysql
./gradlew bootRun
```

#### 2. Using SQLite Database

Start the application using the sqlite profile:
```bash
# Method 1: Command line arguments
./gradlew bootRun --args='--spring.profiles.active=sqlite'

# Method 2: Environment variables
export SPRING_PROFILES_ACTIVE=sqlite
./gradlew bootRun
```

#### 3. Modify Configuration File

Directly modify the default profile in `src/main/resources/application.yml`:
```yaml
spring:
  profiles:
    active: sqlite  # Change to sqlite or mysql
```

### Database Configuration Details

#### MySQL Configuration
```yaml
spring:
  config:
    activate:
      on-profile: mysql
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://10.26.24.214:3306/kurumi-shell?useSSL=false&serverTimezone=UTC
    username: root
    password: Midas888
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
```

#### SQLite Configuration
```yaml
spring:
  config:
    activate:
      on-profile: sqlite
  datasource:
    driver-class-name: org.sqlite.JDBC
    url: jdbc:sqlite:./kurumi-shell.db
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.community.dialect.SQLiteDialect
```

### Technical Implementation Details

#### DatabaseConfig Configuration Class
The project dynamically configures database-related parameters through the `DatabaseConfig` class:

```kotlin
@Configuration
class DatabaseConfig {
    @Bean
    @Profile("sqlite")
    fun hibernateProperties(): Properties {
        return Properties().apply {
            put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect")
            put("hibernate.hbm2ddl.auto", "update")
            put("hibernate.show_sql", "false")
        }
    }
}
```

#### Dependency Configuration
SQLite support has been added in `build.gradle.kts`:
```kotlin
// SQLite database support
runtimeOnly("org.xerial:sqlite-jdbc")
runtimeOnly("org.hibernate.orm:hibernate-community-dialects")
```

#### Entity Class Compatibility
All entity classes have been optimized to ensure SQLite compatibility:
- Removed MySQL-specific COMMENT syntax
- Used Hibernate automatic type mapping
- Maintained full compatibility with MySQL

### Deployment Recommendations

#### Production Environment (Recommend MySQL)
- Use MySQL profile
- Ensure database server is accessible
- Configure appropriate connection pool parameters

#### Development/Testing Environment (Optional SQLite)
- Use SQLite profile
- Database files will be automatically created in the project root directory
- No additional database server required
- Suitable for quick deployment and testing

### Troubleshooting

If you encounter database connection issues:

1. Check the currently active profile:
   ```bash
   ps aux | grep java | grep profiles
   ```

2. View application logs to confirm database type:
   ```
   The following 1 profile is active: "sqlite"
   ```

3. Check database file permissions (SQLite) or network connection (MySQL)

4. Ensure relevant dependencies have been correctly added (build.gradle.kts)

5. **Common Problem Solutions**:
   - **COMMENT Syntax Error**: If you encounter `near "COMMENT": syntax error`, check if entity classes still have unremoved COMMENT syntax
   - **Dialect Issues**: Ensure correct Hibernate dialect configuration is used
   - **Missing Dependencies**: Confirm `sqlite-jdbc` and `hibernate-community-dialects` dependencies have been added
   - **Table Creation Failure**: Check if entity class annotations are correct and SQLite-unsupported features have been removed

6. **Database File Location**:
   - SQLite database files are created by default in the project root: `./kurumi-shell.db`
   - Ensure the application has write permission to this directory

### Important Notes

1. **Data Migration**: MySQL and SQLite data are not interoperable, reinitialize data when switching databases
2. **JPA Dialect**: Corresponding Hibernate dialects have been automatically configured
3. **DDL Settings**: Default uses `update` mode, recommend using `validate` in production
4. **SQLite Limitations**:
   - SQLite does not support concurrent writes, not suitable for high-concurrency scenarios
   - Does not support some SQL features (such as certain ALTER TABLE operations)
   - Does not support COMMENT syntax, solved through entity class optimization
5. **Entity Class Compatibility**: All entity classes have removed database-specific syntax to ensure cross-database compatibility

## Development Guide

### Back-end Development

1. Create new entity classes: Create them in the `src/main/kotlin/per/midas/kurumishell/entity/` directory
2. Create data access layer: Create interfaces in the `src/main/kotlin/per/midas/kurumishell/repository/` directory
3. Create business logic layer: Create service classes in the `src/main/kotlin/per/midas/kurumishell/service/` directory
4. Create API controllers: Create controllers in the `src/main/kotlin/per/midas/kurumishell/controller/` directory

### Front-end Development

1. Create new pages: Create Vue components in the `web/src/pages/` directory
2. Create API services: Create API services in the `web/src/pages/{module}/service/` directory
3. Create components: Create components in the `web/src/pages/{module}/component/` directory
4. Configure routes: Configure routes in `web/src/router/index.ts`

## Browser Compatibility

- Chrome (latest version)
- Firefox (latest version)
- Safari (latest version)
- Edge (latest version)

## Important Notes

1. **Security Tips**: In production environment, please ensure to modify the default password and configure appropriate security policies
2. **Resource Consumption**: SSH terminal and resource monitoring functions will consume a certain amount of server resources; please adjust configuration according to actual conditions
3. **Network Requirements**: WebSocket functionality requires network environment support
4. **Database**: Database table structure will be automatically created when starting for the first time

## Spider Project Disclaimer

### Data Source Description

The spider modules (Dudubird spider and Xuanyuan spider) in this project are used to obtain Docker image-related data from third-party websites, specifically including:

- **Dudubird Spider**: Obtains image data from `docker.aityp.com`
- **Xuanyuan Spider**: Obtains image data from `dockers.xuanyuan.me` and `xuanyuan.cloud`

### Usage Restrictions

1. **Data Usage**: The data obtained by spiders is for personal learning and research purposes only and shall not be used for commercial purposes
2. **Terms of Use**: When using the spider function, please comply with the terms of use, service agreements, and privacy policies of each third-party website
3. **Access Frequency**: Do not use the spider function of this project to access third-party websites at high frequency, so as not to cause unnecessary burden on their servers

### Legal Liability

1. **Data Ownership**: This project does not own, control, or guarantee the accuracy, completeness, timeliness, or availability of any third-party website data
2. **Intellectual Property Rights**: The intellectual property rights of all image data belong to their respective owners; please comply with relevant copyright and intellectual property laws and regulations when using
3. **Disclaimer**: This project and its developers shall not be responsible for any direct, indirect, incidental, special, or consequential damages arising from the use of the spider function or third-party website data
4. **Compliance**: Users are responsible for ensuring that their use of the spider function of this project complies with the laws and regulations of their country and region

### Risk Warnings

1. **Interface Changes**: The interfaces or page structures of third-party websites may change at any time, resulting in the failure of the spider function
2. **Data Accuracy**: This project does not guarantee the accuracy and completeness of data obtained by spiders
3. **Usage Risks**: Please use the data obtained by spiders with caution; this project shall not be responsible for any decisions or actions based on these data

## Dependencies License Agreements

### Front-end Dependencies

| Dependency Name | Version | License | Purpose |
|----------------|---------|---------|---------|
| Vue | ^3.3.4 | MIT License | Front-end framework |
| Naive UI | ^2.42.0 | MIT License | UI component library |
| Axios | ^1.10.0 | MIT License | HTTP client |
| ECharts | ^5.6.0 | Apache License 2.0 | Chart library |
| @xterm/xterm | ^5.5.0 | MIT License | Terminal emulator |
| xterm-addon-fit | ^0.8.0 | MIT License | Xterm terminal adaptation plugin |
| @kangc/v-md-editor | ^2.3.18 | MIT License | Markdown editor |
| Pinia | ^3.0.3 | MIT License | State management |
| pinia-plugin-persistedstate | ^4.4.1 | MIT License | Pinia persistence plugin |
| Vue Router | 4 | MIT License | Routing management |
| countup.js | ^2.9.0 | MIT License | Digital animation effects |
| prismjs | ^1.30.0 | MIT License | Code highlighting |
| element-resize-detector | ^1.2.4 | MIT License | DOM element size change detection |
| vue-hooks-plus | ^2.4.0 | MIT License | Vue Hooks toolkit |
| @vue/compiler-sfc | ^3.5.17 | MIT License | Vue single file component compiler |
| vite | ^7.0.4 | MIT License | Front-end build tool |
| typescript | ~5.8.3 | Apache License 2.0 | Type checking language |
| @types/node | ^24.0.14 | MIT License | Node.js type definitions |
| @types/prismjs | ^1.26.5 | MIT License | Prismjs type definitions |
| @vicons/* | ^0.13.0 | MIT License | Icon library |
| sass | ^1.89.2 | MIT License | CSS preprocessor |
| vue-tsc | ^2.2.12 | MIT License | Vue TypeScript checking tool |

### Back-end Dependencies

| Dependency Name | Version | License | Purpose |
|----------------|---------|---------|---------|
| Spring Boot | 3.5.3 | Apache License 2.0 | Back-end framework |
| Spring Boot Starter Data JPA | 3.5.3 | Apache License 2.0 | JPA data access support |
| Spring Boot Starter Web | 3.5.3 | Apache License 2.0 | Web application support |
| Spring Boot Starter WebSocket | 3.5.3 | Apache License 2.0 | WebSocket support |
| Spring Boot Starter Security | 3.5.3 | Apache License 2.0 | Security framework support |
| Kotlin | 1.9.25 | Apache License 2.0 | Programming language |
| Jackson Module Kotlin | - | Apache License 2.0 | Jackson Kotlin integration |
| JPA (Hibernate) | - | GNU Lesser General Public License v2.1 | ORM framework |
| JJWT API | 0.12.6 | Apache License 2.0 | JWT authentication API |
| JJWT Implementation | 0.12.6 | Apache License 2.0 | JWT authentication implementation |
| JJWT Jackson | 0.12.6 | Apache License 2.0 | JWT Jackson support |
| MySQL Connector/J | - | GNU General Public License v2.0 with Classpath Exception | MySQL database driver |
| Jakarta Bean Validation API | 3.1.1 | Apache License 2.0 | Bean validation API |
| JSch | 2.27.2 | BSD 3-Clause License | SSH connection library |
| Kotlin Test JUnit5 | - | Apache License 2.0 | Kotlin JUnit5 test support |
| Spring Boot Test | 3.5.3 | Apache License 2.0 | Spring Boot test support |

### Spider Dependencies

| Dependency Name | Version | License | Purpose |
|----------------|---------|---------|---------|
| annotated-types | ^0.7.0 | MIT License | Type annotation support |
| anyio | ^4.9.0 | MIT License | Asynchronous I/O support |
| attrs | ^25.3.0 | MIT License | Class definition simplification library |
| beautifulsoup4 | ^4.13.4 | MIT License | HTML parsing library |
| certifi | ^2025.6.15 | Mozilla Public License 2.0 | SSL certificate verification |
| charset-normalizer | ^3.4.2 | MIT License | Character set detection |
| click | ^8.2.1 | BSD 3-Clause License | Command line interface framework |
| fastapi | ^0.115.14 | MIT License | API framework |
| h11 | ^0.16.0 | MIT License | HTTP/1.1 protocol implementation |
| httpcore | ^1.0.9 | BSD 3-Clause License | HTTP client core |
| httpx | ^0.28.1 | BSD 3-Clause License | HTTP client |
| idna | ^3.10 | BSD 3-Clause License | Internationalized domain name support |
| outcome | ^1.3.0 | MIT License | Asynchronous operation result processing |
| packaging | ^25.0 | Apache License 2.0 | Package management tool |
| pydantic | ^2.11.7 | MIT License | Data validation library |
| pydantic_core | ^2.33.2 | MIT License | Pydantic core implementation |
| PySocks | ^1.7.1 | BSD 3-Clause License | SOCKS proxy support |
| python-dotenv | ^1.1.1 | BSD 3-Clause License | Environment variable loading |
| requests | ^2.32.4 | Apache License 2.0 | HTTP client |
| selenium | ^4.34.1 | Apache License 2.0 | Web automation testing |
| setuptools | ^78.1.1 | MIT License | Python package installation tool |
| sniffio | ^1.3.1 | MIT License | Asynchronous I/O library detection |
| sortedcontainers | ^2.4.0 | Apache License 2.0 | Efficient ordered containers |
| soupsieve | ^2.7 | MIT License | CSS selector library |
| starlette | ^0.46.2 | BSD 3-Clause License | ASGI framework |
| trio | ^0.30.0 | MIT License | Asynchronous programming library |
| trio-websocket | ^0.12.2 | MIT License | WebSocket support |
| typing-inspection | ^0.4.1 | MIT License | Type checking tool |
| typing_extensions | ^4.14.1 | MIT License | Type extension support |
| urllib3 | ^2.5.0 | MIT License | HTTP client library |
| uvicorn | ^0.35.0 | BSD 3-Clause License | ASGI server |
| webdriver-manager | ^4.0.2 | Apache License 2.0 | WebDriver management tool |
| websocket-client | ^1.8.0 | Apache License 2.0 | WebSocket client |
| wheel | ^0.45.1 | MIT License | Python package packaging tool |
| wsproto | ^1.2.0 | MIT License | WebSocket protocol implementation |

## Third-party Library Usage Statement

This project uses various open-source dependency libraries, which follow different open-source licenses. The following are the license descriptions and usage statements for the main dependency libraries:

1. **GNU Lesser General Public License v2.1 (LGPL v2.1) Dependencies**
   - **Hibernate (JPA)**: This project uses Hibernate as the JPA implementation, using dynamic linking, and has not modified its source code. According to the terms of LGPL v2.1, users can freely use and distribute this project, but if they modify the code of Hibernate itself, they need to publish the modified code according to the terms of LGPL v2.1.

2. **GNU General Public License v2.0 with Classpath Exception (GPL v2 with Classpath Exception) Dependencies**
   - **MySQL Connector/J**: This project uses MySQL Connector/J as the database driver. According to the Classpath Exception terms, it is allowed to use the driver in applications under any license, as long as the following conditions are met:
     - The driver interacts with the application only through the standard Java API
     - The application code does not directly depend on the internal implementation of the driver

3. **Apache License 2.0 Dependencies**
   - Core dependencies used in this project, such as Spring Boot, Kotlin, JJWT, Jakarta Bean Validation API, TypeScript, etc., follow Apache License 2.0
   - Usage requirements:
     - Keep the original copyright notice and license files
     - Indicate modifications (if applicable)
     - Do not use the original author's name for product promotion

4. **MIT License Dependencies**
   - Main front-end dependencies of this project, such as Vue, Naive UI, Axios, ECharts, Xterm, etc., follow MIT License
   - Dependencies in the spider project, such as annotated-types, anyio, attrs, beautifulsoup4, charset-normalizer, fastapi, h11, outcome, pydantic, pydantic_core, setuptools, sniffio, soupsieve, trio, typing-inspection, typing_extensions, urllib3, wheel, wsproto, etc., follow MIT License
   - Usage requirements:
     - Keep the original copyright notice and license files
     - Allow free use, modification, distribution, and sale
     - Do not provide any warranty

5. **BSD 3-Clause License Dependencies**
   - **JSch**: This project uses the JSch library to implement SSH connection functions, following BSD 3-Clause License
   - Dependencies in the spider project, such as click, httpcore, httpx, idna, starlette, uvicorn, PySocks, python-dotenv, etc., follow BSD 3-Clause License
   - Usage requirements:
     - Keep the original copyright notice and license files
     - Do not use the original author's name for product promotion
     - Provide a copy of the license file

6. **Mozilla Public License 2.0 Dependencies**
   - **certifi**: The spider project uses the certifi library for SSL certificate verification, following Mozilla Public License 2.0
   - Usage requirements:
     - Keep the original copyright notice and license files
     - Derivative works need to be published under the same license
     - Modified source code needs to be made public

This project includes copies of license files for all direct dependency libraries when released, located in their respective dependency package directories. When using, modifying, or distributing this project, please ensure compliance with the license terms of all dependency libraries.

## License

This project is licensed under the <a href="./LICENSE">MIT License</a>.

### Advantages of MIT License
1. **Highly Free**: Allows anyone to freely use, copy, modify, merge, publish, distribute, sublicense, and sell copies of the software
2. **Business-friendly**: Unlimited support for commercial use, very suitable for commercial projects
3. **Concise and Clear**: The agreement text is short and easy to understand, reducing compliance risks
4. **Promotes Spread**: Loose terms help the project spread and be widely adopted
5. **Retains Attribution**: Only requires retention of the original author's copyright notice and license statement, maintaining the author's basic rights