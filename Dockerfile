# 1. ビルド用の環境
FROM eclipse-temurin:17-jdk-jammy AS build
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# 2. 実行用の軽量環境
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]