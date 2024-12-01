FROM sbtscala/scala-sbt:eclipse-temurin-17.0.13_11_1.10.6_3.5.2 AS build-env

# FROM ghcr.io/graalvm/native-image-community:23-muslib AS build-env
# RUN curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > /bin/cs && chmod +x /bin/cs
# RUN /bin/cs setup --apps bloop,sbt --dir /bin -y

FROM build-env AS builder
WORKDIR /build
COPY build.sbt .
COPY project project
RUN sbt update
COPY . .
RUN sbt assembly

# FROM scratch AS runner
# WORKDIR /app
# COPY --from=builder /build/target/native-image/sclaus sclaus
# ENTRYPOINT ["/sclaus"]
# CMD []

FROM openjdk:11-jre-slim-buster AS runner
WORKDIR /app
COPY --from=builder /build/target/assembly/sclaus.jar sclaus.jar
CMD ["java", "-jar", "sclaus.jar"]
