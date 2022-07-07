FROM openjdk:14
ENV TZ=Europe/Berlin
ENV STAGE=COMPOSE

RUN groupadd -g 9999 planxuser && \
    useradd -r -u 9999 -g planxuser planxuser
RUN mkdir /data
RUN chown planxuser:planxuser /data
USER planxuser

# paste the jar to the container
COPY ./build/libs/converting-service-0.0.1.jar /

## for REST
#EXPOSE 8090

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${STAGE}", "converting-service-0.0.1.jar"]
