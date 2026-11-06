FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon bootJar \
    && mkdir -p /out \
    && jar_file="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)" \
    && test -n "$jar_file" \
    && cp "$jar_file" /out/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /out/app.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
