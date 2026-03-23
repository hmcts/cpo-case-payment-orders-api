---
name: repo-security
description: Use for security work in cpo-case-payment-orders-api involving Spring Security, auth filters, S2S or user-auth regressions, and broader environment/config verification outside the dedicated JWT issuer workflow.
---

# Repo Security

## Use when

- changing `SecurityConfiguration` or auth filters
- adding focused security regression tests
- verifying Spring Security, IDAM, or S2S-related settings across app, local docker, Helm, or pipeline wiring
- checking why auth tests now return `401` instead of `403`
- working on broader security behavior beyond just JWT issuer validation

For JWT issuer-specific work, prefer `docs/skills/security-jwt-issuer/SKILL.md`.

## Workflow

1. Start with `git status --short` and inspect existing local diffs before editing.
2. Review `src/main/java/uk/gov/hmcts/reform/cpo/config/SecurityConfiguration.java` together with `src/main/resources/application.yaml`.
3. Search for `SecurityFilterChain`, `ServiceAuthFilter`, `JwtGrantedAuthoritiesConverter`, `IDAM`, `S2S`, and related auth wiring.
4. Verify relevant config sources in:
   `src/main/resources/application.yaml`
   `charts/cpo-case-payment-orders-api/values.yaml`
   `docker-compose.yml`
   `cpo-docker/compose/cpo.yml`
   `Jenkinsfile_CNP`
   `Jenkinsfile_nightly`
5. Prefer layered regression coverage:
   unit coverage for the changed auth behavior first
   then lightweight HTTP/security rejection coverage only if it does not destabilize the broader suite
   then integration coverage if the existing WireMock/Testcontainers harness supports it
6. Run the narrowest relevant Gradle targets before widening scope:
   choose the smallest unit or integration target covering the changed auth path
   widen to `smoke` or `functional` only after focused checks are green.

## Expected checks

- the changed auth path behaves as intended under unit and request-level tests
- HTTP requests still resolve to the expected `401` or `403` path
- S2S and user-auth failures still resolve to the expected `401` or `403` path
