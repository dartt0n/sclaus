FROM sbtscala/scala-sbt:eclipse-temurin-17.0.13_11_1.10.6_3.5.2 AS build-env

FROM build-env AS builder
WORKDIR /build
COPY . .
RUN sbt assembly

FROM openjdk:11-jre-slim-buster AS runner
WORKDIR /app
COPY --from=builder /build/target/assembly/sclaus.jar sclaus.jar
CMD ["java", "-jar", "sclaus.jar"]
