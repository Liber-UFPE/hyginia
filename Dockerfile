FROM eclipse-temurin:25.0.1_8-jdk-jammy@sha256:ad2891d6c3141ef4519ea60391d84bd598ccd3c355e85c99a100576b10ccd75d AS build

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

FROM eclipse-temurin:25.0.1_8-jre-alpine@sha256:9c65fe190cb22ba92f50b8d29a749d0f1cfb2437e09fe5fbf9ff927c45fc6e99

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

CMD [ "java", "-jar", "app.jar" ]
