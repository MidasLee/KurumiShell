plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "per.midas"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
    // 阿里云镜像（优先级最高）
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
        isAllowInsecureProtocol = true  // 如果报HTTPS错误则添加
    }
    maven { url = uri("https://maven.aliyun.com/repository/spring") }
    // 腾讯云镜像（备用）
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    // 注释掉 mavenCentral()
}

dependencies {
    // Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    // Database
	runtimeOnly("com.mysql:mysql-connector-j")
    // Jakarta Bean Validation API (Spring Boot 3.x 使用)
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    // https://mvnrepository.com/artifact/com.github.mwiede/jsch
    implementation("com.github.mwiede:jsch:2.27.2")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
