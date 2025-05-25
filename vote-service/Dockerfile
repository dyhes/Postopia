# 选择轻量级基础镜像（推荐）
FROM eclipse-temurin:17.0.15_6-jre-ubi9-minimal

# 设置工作目录
WORKDIR /app

# 复制JAR文件到容器
COPY build/libs/vote-service.jar app.jar

# 暴露服务端口（与实际端口一致）
EXPOSE 8086

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]