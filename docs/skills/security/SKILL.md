---
name: repo-security
description: Use for security work in cpo-case-payment-orders-api involving Spring Security, JWT decoder behavior, OIDC issuer validation, auth regression tests, and environment/config verification.
---

# Repo Security

## Use when

- resuming JWT issuer validation or OIDC-related work
- changing `SecurityConfiguration` or auth filters
- adding focused security regression tests
- verifying whether issuer-related settings come from app defaults, Helm, local docker, or pipeline wiring

## Workflow

1. Start with `git status --short` and inspect existing local diffs before editing.
2. Review `src/main/java/uk/gov/hmcts/reform/cpo/config/SecurityConfiguration.java` together with issuer properties in `src/main/resources/application.yaml`.
3. Search for `issuer`, `issuer-uri`, `JwtDecoder`, `JwtIssuerValidator`, `JwtTimestampValidator`, `IDAM_OIDC_URL`, and `OIDC_ISSUER`.
4. Verify config sources in:
   `charts/cpo-case-payment-orders-api/values.yaml`
   `docker-compose.yml`
   `cpo-docker/compose/cpo.yml`
   `Jenkinsfile_CNP`
   If `OIDC_ISSUER` needs changing, decode a real token from the target environment and copy the exact `iss` claim. Do not infer it from discovery URLs.
5. Prefer layered regression coverage:
   unit validator coverage first
   then lightweight HTTP/security rejection coverage only if it does not destabilize the broader suite
   then integration coverage if the existing WireMock/Testcontainers harness supports it
6. Run the narrowest relevant Gradle targets before widening scope.

## Expected checks

- valid issuer passes validation
- unexpected issuer is rejected
- expired tokens still fail
- HTTP requests with rejected JWTs return `401`
- docs explain discovery config versus enforced issuer config
