FROM gradle:8.6.0-jdk21 AS build

# Install Node JS
RUN apt-get update -y && apt-get install --no-install-recommends -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && node -v

# Better layer caching by installing dependencies first
WORKDIR /app
COPY package.json package-lock.json /app/
COPY settings.gradle.kts build.gradle.kts gradlew gradle.properties /app/
COPY gradle /app/gradle
COPY buildSrc /app/buildSrc

# Needs to:
# 1. Create the jte directory to avoid errors with jte plugin
# 2. Skip gradle task assetsPipeline because it requires the assets files
RUN mkdir -p /app/src/main/jte && \
  npm install && \
  gradle -Dsonar.gradle.skipCompile=true --console plain --no-configuration-cache classes -x assetsPipeline

# Build application
COPY . /app/
RUN gradle -Dsonar.gradle.skipCompile=true --console plain --no-configuration-cache \
      clean shadowJar -x test -x accessibilityTest \
      && mv -vf build/libs/*.jar app.jar

FROM eclipse-temurin:21.0.2_13-jre-alpine

LABEL org.opencontainers.image.description="Monummenta Hygínia Java Application Service"
LABEL org.opencontainers.image.url="https://github.com/Liber-UFPE/hyginia/"
LABEL org.opencontainers.image.documentation="https://github.com/Liber-UFPE/hyginia/"
LABEL org.opencontainers.image.source="https://github.com/Liber-UFPE/hyginia/"
LABEL org.opencontainers.image.vendor="Laboratório Liber / UFPE"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.title="Monummenta Hygínia"

ENV VISAO_HOLANDESA_PORT=8080
ENV MICRONAUT_ENVIRONMENTS=container

COPY --from=build /app/app.jar .
EXPOSE 8080
HEALTHCHECK CMD curl -f "http://localhost:$VISAO_HOLANDESA_PORT/" || exit 1

# Add unprivileged user to run the service:
# https://docs.docker.com/engine/reference/builder/#user
# https://docs.docker.com/develop/develop-images/instructions/#user
# https://manpages.ubuntu.com/manpages/noble/en/man8/addgroup.8.html
# https://manpages.ubuntu.com/manpages/noble/en/man8/adduser.8.html
RUN addgroup --system hyginia && adduser --system -G hyginia hyginia
USER hyginia

ENTRYPOINT [ "java", "-jar", "app.jar" ]