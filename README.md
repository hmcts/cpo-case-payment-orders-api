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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
