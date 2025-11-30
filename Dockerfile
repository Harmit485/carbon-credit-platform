FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy Maven wrapper and pom.xml first for caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the jar (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests -B

# Expose the port (Render will set $PORT at runtime)
EXPOSE 8080

# Run the jar (wildcard matches the generated jar)
ENTRYPOINT ["java","-jar","target/*.jar"]
