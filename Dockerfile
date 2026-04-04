FROM eclipse-temurin:11-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && \
    sed -i "/org.gretty/d" build.gradle && \
    sed -i "/gretty/,/}/d" build.gradle && \
    sed -i "/jcenter/d" build.gradle && \
    ./gradlew war --no-daemon

FROM tomcat:9-jre11-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/build/libs/FopConverterJava-1.0.0.war /usr/local/tomcat/webapps/conversionservice.war
RUN mkdir -p /tmp/fop-temp
ENV CATALINA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
