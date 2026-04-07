# ─── Build stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /build

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw

# Resolve dependencies in a separate layer (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ─── Runtime stage ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid appgroup --no-create-home appuser

WORKDIR /app

COPY --from=build /build/target/kanban-api-*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

# KANBOARD_API_TOKEN must be injected at runtime — empty default is intentional
ENV KANBOARD_URL="https://marcushowarth.com/jsonrpc.php" \
    KANBOARD_API_TOKEN="" \
    KANBOARD_USERNAME="claude"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -sf http://localhost:8080/sse || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
