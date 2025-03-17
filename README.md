# Postopia

## 简介

论坛系统的后端服务

# 运行环境

* Java 17
* Spring Boot 3.3.1
* Gradle 8.8
* Docker 20.10.20

# 初始化

## .env配置

复制.env_example,修改其中配置项，其中：

* JWT_SECRET：生成JWT Token时使用的SecretKey
* MAIL_USERNAME,MAIL_PASSWORD:邮件服务的用户名和密码
* CLOUDINARY_URL：CLOUDINARY对象存储的URL（从CLOUDINARY获取）

# 运行项目

新设备需首先用方法1运行

## 方法1

命令行运行

**Mac / Linux:**

```shell
./gradlew bootRun
```

**Windows:**

```shell
.\gradlew.bat bootRun
```

## 方法2

Idea 运行
