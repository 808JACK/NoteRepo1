# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml from NoteIt directory
COPY NoteIt/mvnw .
COPY NoteIt/mvnw.cmd .
COPY NoteIt/.mvn .mvn
COPY NoteIt/pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Copy source code from NoteIt directory
COPY NoteIt/src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port (Railway provides PORT env var)
EXPOSE $PORT

# Run the application with dynamic port
CMD ["sh", "-c", "java -jar target/*.jar --server.port=${PORT:-8080}"]