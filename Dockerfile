FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S springdocker && adduser -S springdocker -G springdocker
USER springdocker:springdocker
ARG JAR_FILE=app/build/libs/router-usage-statistics-java.jar
COPY ${JAR_FILE} router-usage-statistics-java.jar
ENTRYPOINT ["java","-jar", \
#"-DPORT=7001", \
#"-DPROFILE=docker", \
#"-DTIME_ZONE=America/Denver", \
#"-DDBNAME=some_name", \
#"-DDBUSR=some_user", \
#"-DDBPWD=some_password", \
#"-DJSUSR=another_user", \
#"-DJSPWD=another_password", \
#"-DAPI_KEY_PUB=some_api_key", \
#"-DAPI_KEY_PRV=another_api_key", \
#"-DEMAIL=some_email", \
#"-DNAME=another_name", \
"/router-usage-statistics-java.jar"]
