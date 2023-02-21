FROM openjdk:17

WORKDIR /checkit-server-build

ARG JAR_FILES="build/libs/*.jar"
ENV JAR=checkit-server.jar
COPY ${JAR_FILES} $JAR

RUN useradd -r -U spring
RUN chown spring:spring -R .
RUN chmod ug+rw -R .
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","/checkit-server-build/checkit-server.jar"]