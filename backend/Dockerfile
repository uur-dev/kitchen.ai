# ── STAGE 1: BUILD ──────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
ARG MODULE_NAME

# shared-library poora copy karo (parent pom + all modules)
COPY shared-library/ shared-library/

# parent pom se sare modules ek saath install honge
# order automatic — Maven dependency graph se handle karta hai
RUN cd shared-library && mvn clean install -DskipTests -q

# service copy aur build karo
COPY ${MODULE_NAME}/ ${MODULE_NAME}/
RUN cd ${MODULE_NAME} && mvn clean package -DskipTests -q

# ── STAGE 2: RUN ────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
ARG MODULE_NAME
COPY --from=build /app/${MODULE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]