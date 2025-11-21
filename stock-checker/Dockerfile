FROM eclipse-temurin:17
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests
CMD ["java", "-jar", "target/stock-checker-0.0.1-SNAPSHOT.jar"]