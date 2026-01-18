#For building containerized app, relies on a DB though
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY crud/target/*.jar ./app.jar

EXPOSE 9797

ENTRYPOINT ["java", "-jar", "app.jar"]