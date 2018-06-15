# Auto Generated Dockerfile

FROM java:8-jre
LABEL maintainer="dev@ballerina.io"

COPY ballerina-runtime /home/ballerina

EXPOSE  8080

CMD java -Dballerina.home=/home/ballerina -jar /home/ballerina/ballerina-runtime-0.0.1-SNAPSHOT.jar --http.port=8080
