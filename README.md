# Case Payment Orders API

This micro-service provides a set of APIs to manage case payment orders.

[![API Docs](https://img.shields.io/badge/API%20Docs-site-e140ad.svg)](https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/cpo-case-payment-orders-api.json)

[![Build Status](https://travis-ci.org/hmcts/cpo-case-payment-orders-api.svg?branch=master)](https://travis-ci.org/hmcts/cpo-case-payment-orders-api)

## Getting Started

### Prerequisites
- [JDK 21](https://java.com)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/cpo-case-payment-orders-api` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4457` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4457/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Developing

### Unit tests
To run all unit tests execute the following command:
```bash
./gradlew test
```

### Integration tests
To run all integration tests execute the following command:
```bash
./gradlew integration
```

### Functional tests
The tests are written using befta-fw library. To find out more about BEFTA Framework, see the
 [BEFTA-FW repository and its README](https://github.com/hmcts/befta-fw).

These tests can be run using:
```bash
export TEST_URL=http://localhost:4457
./gradlew functional
```

> Note: These are the tests run against an environment.
> Please see [cpo-docker/README.md](./cpo-docker/README.md) for local environment testing.
>
> If you would like to test against AAT dependencies then run `docker-compose up`.
> Also set the required environment variables that can be found by reviewing the contents of this project's
> [Jenkinsfile_CNP](./Jenkinsfile_CNP) script (particularly the `secrets` mappings, and the variables set by
> the `setBeftaEnvVariables` routine).
>

### Code quality checks
We use [checkstyle](http://checkstyle.sourceforge.net/) and [PMD](https://pmd.github.io/).

To run all checks execute the following command:

```bash
./gradlew clean checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdIntegrationTest
```

To run all checks alongside the unit tests execute the following command:

```bash
./gradlew checks
```

or to run all checks, all tests and generate a code coverage report execute the following command:

```bash
./gradlew check integration functional jacocoTestReport
```

## Error handling and support

JSON error responses include stable string codes to help support identify the error category.
A code describes the HTTP status; it does not identify an individual incident or establish its cause.

| HTTP status | Code |
| --- | --- |
| 400, 422 | `INVALID_REQUEST` |
| 401 | `AUTHENTICATION_REQUIRED` |
| 403 | `ACCESS_DENIED` |
| 404 | `RESOURCE_NOT_FOUND` |
| 405 | `METHOD_NOT_ALLOWED` |
| 413 | `REQUEST_TOO_LARGE` |
| 415 | `UNSUPPORTED_MEDIA_TYPE` |
| 429 | `TOO_MANY_REQUESTS` |
| 5xx | `INTERNAL_ERROR` |
| Other errors | `REQUEST_FAILED` |

Authentication and service-authentication filters return empty 401/403 bodies and record the category
in handler logs. Controller errors can return JSON bodies. Fallback JSON and HTML error pages may
omit the code. Normal WARN/ERROR failure logs retain exception details for administrator support; public responses remain generic.

### Investigating a failure

Record the service/environment, UTC time, HTTP status, error code where available and an existing
telemetry operation ID. Use the operation ID and time to find related requests and logs in App Insights.
No additional incident reference is generated. Do not request bearer tokens or credentials from users.

For 400/422, check the request format and validation messages. For 401, check sign-in; for 403, check
permissions. Escalate unexpected 5xx failures with the recorded context. Detailed logs should be
accessible only through the environment's restricted administrator/support access.

### Optional additional DEBUG diagnostics

Original failure exceptions are already retained at normal WARN/ERROR levels. For additional tracing, enable DEBUG only for the logger needed. For request or authentication
failures, use the relevant entry below in the deployment's logging configuration:

```yaml
logging:
  level:
    uk.gov.hmcts.reform.cpo.exception.RestExceptionHandler: DEBUG
    uk.gov.hmcts.reform.cpo.security.SafeAuthenticationErrors: DEBUG
```

Other diagnostic loggers are:

- `uk.gov.hmcts.reform.cpo.security.SafeServiceAuthFilter` for service authentication.
- `uk.gov.hmcts.reform.cpo.logging.SafeHealthDiagnosticsFilter` for health checks and dependency diagnostics.

- `uk.gov.hmcts.reform.cpo.auditlog.AuditInterceptor` and `uk.gov.hmcts.reform.cpo.auditlog.aop.AuditAspect` for audit failures.

If using the Application Insights Java agent, its logging collection threshold must also allow DEBUG:

```text
APPLICATIONINSIGHTS_INSTRUMENTATION_LOGGING_LEVEL=DEBUG
```

Apply startup configuration through the normal deployment process and restart/roll out as required.
Reproduce the failure and check related trace/exception telemetry using the operation ID. Additional DEBUG events are available only while DEBUG is enabled; normal failure diagnostics remain available subject to collection and retention.
Restore both logger and collection thresholds afterwards. Server diagnostics can contain sensitive
information and must remain subject to restricted log access.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
