FROM eclipse-temurin:17

WORKDIR /app

COPY . .

# Give execute permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/stock-checker-0.0.1-SNAPSHOT.jar"]
