FROM openjdk:latest
COPY . /opt/app/sapphire/
WORKDIR /opt/app/sapphire
RUN ./gradlew clean
CMD ["./gradlew", "bootRun"]