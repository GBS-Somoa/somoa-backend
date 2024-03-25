FROM amazoncorretto:17
ARG JAR_FILE=build/libs/*.jar

RUN mkdir app
WORKDIR /app
COPY . .

RUN chmod +x gradlew && ./gradlew clean bootjar
RUN mv ${JAR_FILE} ./app.jar

ENTRYPOINT ["java", "-XX:+AllowRedefinitionToAddDeleteMethods", "-jar", "app.jar"]