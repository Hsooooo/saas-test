FROM openjdk:17-slim

EXPOSE 10350

WORKDIR /app

# jar 파일 복사
COPY ./build/libs/em-saas-rest-api.jar /app/

ENTRYPOINT ["java", "-jar", "em-saas-rest-api.jar", "-Djava.net.preferIPv4Stack=true", "-Djava.net.preferIPv4Addresses=true"]