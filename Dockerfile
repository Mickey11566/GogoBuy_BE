# 第一階段：編譯階段 (使用 JDK 17 為例，請根據您專案版本調整)
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 複製專案原始碼
COPY --chown=gradle:gradle . .

# 執行編譯 (跳過測試以加速建置，若要測試請移除 -x test)
RUN ./gradlew clean bootJar -x test

# 第二階段：執行階段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 從第一階段複製編譯好的 jar 檔
# Spring Boot 預設產出的檔案會在 build/libs/ 下
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# 啟動指令
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]