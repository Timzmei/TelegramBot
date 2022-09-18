FROM openjdk:17
ADD /target/TelegramBot-1.0-SNAPSHOT-shaded.jar telegramBot.jar
ENTRYPOINT ["java", "-jar", "telegramBot.jar"]