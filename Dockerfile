FROM bellsoft/liberica-openjre-alpine:21
COPY ./target/message-api-1.0.jar /app/
ENTRYPOINT ["java", "-jar", "/app/message-api-1.0.jar"]
