---
name: repo-security-jwt-issuer
description: Use for JWT issuer validation work in cpo-case-payment-orders-api involving JwtDecoder issuer checks, OIDC_ISSUER config, issuer regression tests, and the smoke/functional issuer verifier.
---

# Repo Security JWT Issuer

## Use when

- resuming JWT issuer validation or OIDC issuer-related work
- changing `JwtDecoder` validation behavior
- updating `IDAM_OIDC_URL`, `OIDC_ISSUER`, or issuer-related Helm and pipeline config
- checking why authenticated requests now return `401 invalid_token`
- reviewing the build-integrated issuer verifier used by smoke/functional runs

## Workflow

1. Start with `git status --short` and inspect existing local diffs before editing.
2. Review `src/main/java/uk/gov/hmcts/reform/cpo/config/SecurityConfiguration.java` together with issuer properties in `src/main/resources/application.yaml`.
3. Search for `issuer`, `issuer-uri`, `JwtDecoder`, `JwtIssuerValidator`, `JwtTimestampValidator`, `IDAM_OIDC_URL`, `OIDC_ISSUER`, and `VERIFY_OIDC_ISSUER`.
4. Verify config sources in:
   `charts/cpo-case-payment-orders-api/values.yaml`
   `charts/cpo-case-payment-orders-api/values.preview.template.yaml`
   `docker-compose.yml`
   `cpo-docker/compose/cpo.yml`
   `Jenkinsfile_CNP`
   `Jenkinsfile_nightly`
   `cpo-docker/bin/env_variables_all.txt`
   If `OIDC_ISSUER` needs changing, decode a real token from the target environment and copy the exact `iss` claim. Do not infer it from discovery URLs.
5. Prefer layered regression coverage:
   unit validator coverage first
   then lightweight HTTP/security rejection coverage only if it does not destabilize the broader suite
   then integration coverage if the existing WireMock/Testcontainers harness supports it
6. Review the build-integrated issuer verifier in:
   `src/functionalTest/java/uk/gov/hmcts/reform/cpo/befta/JwtIssuerVerificationApp.java`
   `src/functionalTest/java/uk/gov/hmcts/reform/cpo/befta/CasePaymentOrdersBeftaRunner.java`
   `build.gradle`
   Confirm the intended contract:
   CI/pipeline enables it with `VERIFY_OIDC_ISSUER=true`
   local runs keep it off by default unless explicitly enabled
   Jenkins must export `OIDC_ISSUER` because Helm env alone is not visible to the verifier process.
7. Run the narrowest relevant Gradle targets before widening scope:
   `./gradlew test --tests uk.gov.hmcts.reform.cpo.config.SecurityConfigurationTest`
   `./gradlew integration --tests uk.gov.hmcts.reform.cpo.security.JwtIssuerValidationIT`
   `./gradlew verifyFunctionalTestJwtIssuer`
   widen to `smoke` or `functional` only after focused checks are green.

## Expected checks

- valid issuer passes validation
- unexpected issuer is rejected
- expired tokens still fail
- HTTP requests with rejected JWTs return `401`
- docs explain discovery config versus enforced issuer config
- preview and CI issuer values are explicit and should be checked against a real token, not derived by guesswork
- `VERIFY_OIDC_ISSUER=true` makes smoke/functional fail fast when `iss != OIDC_ISSUER`
