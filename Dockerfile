FROM eclipse-temurin:17-jdk-jammy
LABEL name="greenback-tg-bot"
EXPOSE 5005

WORKDIR /app

COPY *.jar ./app.jar

CMD ["java", "-jar", "app.jar" ]
