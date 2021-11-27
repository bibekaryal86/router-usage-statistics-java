FROM adoptopenjdk/openjdk11:alpine
RUN addgroup -S springdocker && adduser -S springdocker -G springdocker
USER springdocker:springdocker
ARG JAR_FILE=app/build/libs/router-usage-statistics-java.jar
COPY ${JAR_FILE} router-usage-statistics-java.jar
ENTRYPOINT ["java","-jar", "router-usage-statistics-java.jar"]


