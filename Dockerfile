FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S janus && adduser -S janus -G janus

COPY --from=builder /app/target/*.jar app.jar

RUN chown janus:janus app.jar

USER janus

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]