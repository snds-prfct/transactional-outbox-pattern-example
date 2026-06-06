### BUILDER STAGE
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY orders-service/pom.xml orders-service/pom.xml
COPY orders-service/src orders-service/src

RUN ./mvnw clean package -Dmaven.test.skip=true


### LAYERS EXTRACTION STAGE
FROM eclipse-temurin:25-jre-alpine AS layers

WORKDIR /layers

COPY --from=builder /build/orders-service/target/orders-service*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted


### RUNNER STAGE
FROM eclipse-temurin:25-jre-alpine AS runner

WORKDIR /app

RUN addgroup --system apprunner && adduser --system --ingroup apprunner apprunner

COPY --from=layers /layers/extracted/dependencies/ ./
COPY --from=layers /layers/extracted/snapshot-dependencies/ ./
COPY --from=layers /layers/extracted/application/ ./

USER apprunner

ENTRYPOINT ["java", "-jar", "app.jar"]
