#--------------------------------------------------------
# 1. image with extracted application layers
#--------------------------------------------------------
FROM adoptopenjdk/openjdk11:alpine-jre as builder
# should be a single jar file
ARG JAR_FILE=mod-password-validator-server/target/*.jar

COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

#--------------------------------------------------------
# 2. target image
#--------------------------------------------------------
FROM adoptopenjdk/openjdk11:alpine-jre

COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./

# Expose this port locally in the container.
EXPOSE 8081

ENV JAVA_OPTS ${JAVA_OPTIONS} \
        "--spring.datasource.username=${DB_USERNAME}" \
        "--spring.datasource.password=${DB_PASSWORD}" \
        "--spring.datasource.url=${DB_URL}"

ENTRYPOINT ["java", "${JAVA_OPTS}", "org.springframework.boot.loader.JarLauncher"]