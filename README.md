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

JWT/OIDC configuration is split across two settings:

- `IDAM_OIDC_URL` is used for OIDC discovery and JWKS lookup. The app derives `spring.security.oauth2.client.provider.oidc.issuer-uri` as `${IDAM_OIDC_URL}/o`.
- `OIDC_ISSUER` is the issuer claim the active `JwtDecoder` enforces on incoming bearer tokens.

Those values can differ in HMCTS environments. Discovery may use the public IDAM base URL, while issuer validation must match the issuer claim emitted in the token. Do not infer `OIDC_ISSUER` from discovery config alone. Set it only after decoding a real IDAM access token and copying the exact `iss` claim. Helm already supplies an environment value in [charts/cpo-case-payment-orders-api/values.yaml](./charts/cpo-case-payment-orders-api/values.yaml), but it still needs to be treated as a value to verify against a real token.

`Jenkinsfile_CNP` configures `IDAM_API_URL_BASE` and `S2S_URL_BASE` for BEFTA usage, but it does not override `IDAM_OIDC_URL` or `OIDC_ISSUER`; deployment-time issuer settings come from Helm/environment values.

Smoke and functional runs enforce JWT issuer verification in CI, while local runs keep it disabled by default unless `VERIFY_OIDC_ISSUER=true` is set.

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

The integration profile (`src/integrationTest/resources/application-itest.yaml`) keeps issuer discovery and enforced issuer aligned to the WireMock OIDC issuer so JWT issuer validation remains active during the suite.

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
