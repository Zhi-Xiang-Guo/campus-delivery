FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# 先复制顶层 pom 和模块 pom，加快缓存
COPY pom.xml ./pom.xml
COPY sky-common/pom.xml ./sky-common/pom.xml
COPY sky-pojo/pom.xml ./sky-pojo/pom.xml
COPY sky-server/pom.xml ./sky-server/pom.xml

# 再复制源码
COPY sky-common ./sky-common
COPY sky-pojo ./sky-pojo
COPY sky-server ./sky-server

WORKDIR /app
RUN mvn -q -DskipTests -pl sky-server -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/sky-server/target/sky-server-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

