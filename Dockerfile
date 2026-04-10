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
COPY --from=build /app/build/libs/conversionservice.war /usr/local/tomcat/webapps/conversionservice.war
RUN mkdir -p /tmp/fop-temp

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar

ENV OTEL_SERVICE_NAME="fop-converter"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
ENV OTEL_LOGS_EXPORTER="otlp"
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_TRACES_EXPORTER="otlp"
# OTEL_EXPORTER_OTLP_ENDPOINT must be set at deploy time, e.g.:
#   K8s internal:  http://alloy.telemetry.svc.cluster.local:4318
#   Dev remote:    https://otel.dev.amphora.ee
#   Prod remote:   https://otel.svc.amphora.ee
# Remote endpoints require OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer <token>"
ENV CATALINA_OPTS="-Xms256m -Xmx512m -javaagent:/opt/opentelemetry-javaagent.jar"
EXPOSE 8080
