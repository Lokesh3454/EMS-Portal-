# ==============================================================================
# Stage 1: Build Angular Frontend
# ==============================================================================
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci || npm install

COPY frontend/ ./
RUN npm run build -- --configuration production

# ==============================================================================
# Stage 2: Build Spring Boot Backend with Frontend Static Assets
# ==============================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-build
WORKDIR /app/backend

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend/src ./src

# Copy Angular production bundle into Spring Boot's static resources directory
COPY --from=frontend-build /app/frontend/dist/frontend/ ./src/main/resources/static/

RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 3: Slim Production Runtime
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S ems && adduser -S ems -G ems
USER ems

COPY --from=backend-build /app/backend/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
