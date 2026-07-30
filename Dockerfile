# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.9

ARG PLATFORM=""
FROM eclipse-temurin${PLATFORM}:21 AS builder
WORKDIR /builder

ARG JAR_FILE=build/libs/cpo-case-payment-orders-api.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM hmctsprod.azurecr.io/base/java${PLATFORM}:21-distroless
USER hmcts
WORKDIR /opt/app

COPY lib/applicationinsights.json /opt/app/

# The following layer ARGs are only needed to stop Fortify flagging an issue with the COPY instructions
ARG DIR_LAYER_APPLICATION=application/
ARG DIR_LAYER_DEPENDECIES=dependencies/
ARG DIR_LAYER_SPRING_BOOT_LOADER=spring-boot-loader/
ARG DIR_LAYER_SNAPSHOT_DEPENDENCIES=snapshot-dependencies/

COPY --from=builder /builder/extracted/${DIR_LAYER_DEPENDECIES} /opt/app/
# Add 'CMD true or RUN true' if consecutive COPY commands are failing in case (intermittently).
# See https://github.com/moby/moby/issues/37965#issuecomment-771526632
COPY --from=builder /builder/extracted/${DIR_LAYER_SPRING_BOOT_LOADER} /opt/app/
COPY --from=builder /builder/extracted/${DIR_LAYER_SNAPSHOT_DEPENDENCIES} /opt/app/
COPY --from=builder /builder/extracted/${DIR_LAYER_APPLICATION} /opt/app/

EXPOSE 4457
ENTRYPOINT ["/usr/bin/java", "-jar", "application.jar"]
