# the base image
FROM amazoncorretto:17

WORKDIR /app

COPY .gradle/ ./gradle

COPY gradle/ build.gradle settings.gradle gradlew  ./

COPY src ./src

CMD ["./gradlew", "bootRun"]