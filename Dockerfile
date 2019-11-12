FROM gradle:5.4-jdk11-slim AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:11-jre-slim
RUN mkdir -p /opt/app
COPY --from=build /home/gradle/src/build/libs/jared-the-manager-0.0.1-SNAPSHOT-all.jar /opt/app/jared-the-manager-0.0.1-SNAPSHOT-all.jar

RUN useradd --create-home --user-group tavernik && \
    chown -R tavernik /opt

USER tavernik
WORKDIR /opt/app

EXPOSE 8080

ENV JDBC_URL=jdbc:postgresql://localhost:15432/expenses
ENV TG_BOT_TOKEN=notoken
ENV JDBC_USER=nouser
ENV JDBC_PASSWORD=nopassword
ENV ALLOWED_USERS=anyone

CMD ["java", "-jar", "jared-the-manager-0.0.1-SNAPSHOT-all.jar"]