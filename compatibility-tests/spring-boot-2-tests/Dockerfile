# 第一阶段：编译打包
FROM maven:3.8.4-openjdk-17 AS build

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 执行 Maven 构建
RUN mvn clean package -DskipTests

# 第二阶段：运行环境
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:17-slim

# 设置工作目录
WORKDIR /app

# 从第一阶段复制构建好的 jar 包
COPY --from=build /build/target/*.jar app.jar

# 暴露应用程序运行所需的端口（默认为8080）
EXPOSE 8080

# 启动时运行的应用程序命令
ENTRYPOINT ["java", "-jar", "app.jar"]
