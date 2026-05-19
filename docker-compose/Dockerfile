# ================================================================
# GMP系统 - 多模块通用Dockerfile
# 使用方法: docker build --build-arg MODULE=gmp-auth -t gmp-auth .
#
# 构建阶段1: Maven编译 (使用Eclipse Temurin JDK 21)
# ================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /build

# 先复制POM文件，利用Docker缓存加速构建
COPY pom.xml .
COPY gmp-common/pom.xml gmp-common/
COPY gmp-framework/pom.xml gmp-framework/
COPY gmp-auth/pom.xml gmp-auth/
COPY gmp-gateway/pom.xml gmp-gateway/
COPY gmp-system-server/pom.xml gmp-system-server/
COPY gmp-form-server/pom.xml gmp-form-server/
COPY gmp-workflow-server/pom.xml gmp-workflow-server/

# 下载依赖（此层可被缓存）
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -q

# 复制源码并编译指定模块
COPY gmp-common/src gmp-common/src
COPY gmp-framework/src gmp-framework/src
COPY gmp-auth/src gmp-auth/src
COPY gmp-gateway/src gmp-gateway/src
COPY gmp-system-server/src gmp-system-server/src
COPY gmp-form-server/src gmp-form-server/src
COPY gmp-workflow-server/src gmp-workflow-server/src

ARG MODULE
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -pl ${MODULE} -am -DskipTests -B -q

# ================================================================
# 运行阶段: 使用精简JRE镜像
# ================================================================
FROM eclipse-temurin:21-jre-alpine

# 设置时区
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

ARG MODULE
# 从构建阶段复制编译好的JAR包
COPY --from=builder /build/${MODULE}/target/*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT:-8000}/actuator/health || exit 1

# JVM参数（可通过环境变量JAVA_OPTS覆盖）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SERVER_PORT=8080

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar --server.port=${SERVER_PORT}"]
