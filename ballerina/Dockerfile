# Auto Generated Dockerfile

#FROM java:8-jre
#LABEL maintainer="dev@ballerina.io"

FROM adoptopenjdk/openjdk8-openj9:jdk8u162-b12_openj9-0.8.0

RUN rm -rf /var/lib/apt/lists/* && apt-get clean && apt-get update \
	&& apt-get install -y --no-install-recommends locales \
	&& rm -rf /var/lib/apt/lists/* \
	&& locale-gen en_US.UTF-8

ENV LANG="en_US.UTF-8" \
	LANGUAGE="en_US:en" \
	LC_ALL="en_US.UTF-8" \
	VERSION=8 \
	UPDATE=162 \
	BUILD=12

# COPY ballerina-runtime /home/ballerina

ADD proxy /home/ballerina

RUN cd /home/ballerina \
    && rm -rf .classpath .gitignore .gradle .project .settings Dockerfile build \
    && ./gradlew shadowJar \
    && rm -rf /home/ballerina/src \

EXPOSE  8080

CMD java -Dballerina.home=/home/ballerina/build -jar /home/ballerina/build/libs/ballerina-all.jar --http.port=8080
