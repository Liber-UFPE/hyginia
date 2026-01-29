FROM eclipse-temurin:21.0.9_10-jdk-jammy@sha256:41c8a28993a03b5dd042ce1afc7973c9a98bfbbbd6336eb1e32e673b1718d99c AS build

# Install Node JS
RUN apt-get update -y && apt-get install --no-install-recommends -y curl git \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && node -v

# Better layer caching by installing dependencies first
WORKDIR /app
COPY package.json yarn.lock /app/
COPY settings.gradle.kts build.gradle.kts gradlew gradle.properties /app/
COPY gradle /app/gradle
COPY buildSrc /app/buildSrc

# Creates the jte directory to avoid errors with jte plugin
COPY src/main/jte/.jteroot /app/src/main/jte/.jteroot

RUN corepack enable && \
    yarn install && \
    ./gradlew -Dsonar.gradle.skipCompile=true --console plain --no-configuration-cache classes -x assetsPipeline

# Build application
COPY . /app/
RUN ./gradlew -Dsonar.gradle.skipCompile=true --console plain --no-configuration-cache \
      clean shadowJar -x test -x accessibilityTest \
      && mv -vf build/libs/*.jar app.jar

# https://github.com/GoogleContainerTools/distroless/tree/main/java
FROM gcr.io/distroless/java21-debian12:nonroot@sha256:93eecaa3e3bee98da6795700edbcdc93d6bdcb7038c44be3ab5940e24e7b620f

LABEL org.opencontainers.image.description="Monummenta Hygínia Java Application Service"
LABEL org.opencontainers.image.url="https://github.com/Liber-UFPE/hyginia/"
LABEL org.opencontainers.image.documentation="https://github.com/Liber-UFPE/hyginia/"
LABEL org.opencontainers.image.source="https://github.com/Liber-UFPE/hyginia/"
LABEL org.opencontainers.image.vendor="Laboratório Liber / UFPE"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.title="Monummenta Hygínia"

ENV HYGINIA_PORT=8080
ENV MICRONAUT_ENVIRONMENTS=container

COPY --from=build /app/app.jar .
EXPOSE 8080
HEALTHCHECK CMD curl -f "http://localhost:$HYGINIA_PORT/" || exit 1

# The entrypoint for distroless images is set to the equivalent of "java -jar"
# so it only expects a path to a JAR file in the CMD.
CMD [ "app.jar" ]
