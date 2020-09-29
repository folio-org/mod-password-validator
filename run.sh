#!/bin/sh
#
DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_DATABASE}"
JAVA_OPTS="${JAVA_OPTIONS} \
        -Dspring.datasource.username=${DB_USERNAME} \
        -Dspring.datasource.password=${DB_PASSWORD}"
#        -Dspring.datasource.url=${DB_URL}"
#
echo "JAVA_OPTS=${JAVA_OPTS}"
#
#exec "java ${JAVA_OPTS} org.springframework.boot.loader.JarLauncher"