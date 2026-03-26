# JWT issuer validation

## Summary

Issuer validation is active in `cpo-case-payment-orders-api`.
The decoder now rejects tokens whose `iss` claim does not match `oidc.issuer`, even if signature validation and timestamps succeed.

## Current behavior

`SecurityConfiguration.jwtDecoder()` discovers OIDC metadata and JWKS from `spring.security.oauth2.client.provider.oidc.issuer-uri`, then applies a validator chain that enforces:

- token timestamps
- issuer claim equality with `oidc.issuer`

This keeps key discovery separate from the trust boundary for issuer claims.

## Code path

- Decoder wiring: `src/main/java/uk/gov/hmcts/reform/cpo/config/SecurityConfiguration.java`
- Validator coverage: `src/test/java/uk/gov/hmcts/reform/cpo/config/SecurityConfigurationTest.java`
- Integration coverage: `src/integrationTest/java/uk/gov/hmcts/reform/cpo/security/JwtIssuerValidationIT.java`
- Integration token generation: `src/integrationTest/java/uk/gov/hmcts/reform/cpo/BaseTest.java`

## Configuration contract

- `IDAM_OIDC_URL` feeds `spring.security.oauth2.client.provider.oidc.issuer-uri` as `${IDAM_OIDC_URL}/o`
- `OIDC_ISSUER` feeds `oidc.issuer`

Use `IDAM_OIDC_URL` for metadata discovery and JWKS retrieval.
Use `OIDC_ISSUER` for the exact `iss` claim expected in bearer tokens.
Do not derive `OIDC_ISSUER` by guesswork. Decode a real bearer token from the target environment and copy the `iss` claim exactly.
Do not rely on the fallback in `application.yaml` for any real runtime. Compose, Helm, and CI should all provide `OIDC_ISSUER` explicitly.

## How to derive `OIDC_ISSUER`

- Do not guess the issuer from the public discovery URL alone.
- Decode only the JWT payload from a real access token for the target environment and inspect the `iss` claim.
- Do not store or document full bearer tokens. Record only the derived issuer value.

Example:

```bash
TOKEN='eyJ...'
PAYLOAD=$(printf '%s' "$TOKEN" | cut -d '.' -f2)
python3 - <<'PY' "$PAYLOAD"
import base64, json, sys
payload = sys.argv[1]
payload += '=' * (-len(payload) % 4)
print(json.loads(base64.urlsafe_b64decode(payload))["iss"])
PY
```

- JWTs are `header.payload.signature`.
- The second segment is base64url-encoded JSON.
- This decodes the payload only. It does not verify the signature.

## Verified config sources

- Application defaults: `src/main/resources/application.yaml`
- Helm deployment values: `charts/cpo-case-payment-orders-api/values.yaml`
- Local Docker runtime: `docker-compose.yml`
- Local CPO docker stack: `cpo-docker/compose/cpo.yml`
- Integration test profile: `src/integrationTest/resources/application-itest.yaml`

`Jenkinsfile_CNP` sets `IDAM_API_URL_BASE` and `S2S_URL_BASE` for BEFTA-style tests and also exports `OIDC_ISSUER` for the build-integrated issuer verifier. Runtime issuer settings still come from Helm values or explicit environment overrides.

At the time of this patch, Helm contains an `OIDC_ISSUER` value but this repo does not contain a captured real token proving that value. Treat that environment value as pending verification until a real token is decoded.

## Test coverage

- Unit validator checks confirm:
  - configured issuer is accepted
  - unexpected issuer is rejected
  - expired tokens are rejected even when issuer matches
- Integration coverage aligns the test profile issuer with WireMock OIDC discovery and rejects a token signed with the test key but carrying an unexpected issuer

## Remaining operational step

Decode a real IDAM bearer token from the target environment and compare its `iss` claim with the deployment `OIDC_ISSUER` value before treating that environment configuration as verified.

## Local verification

For local running, `OIDC_ISSUER` must match the `iss` claim in the real access token your local setup is using.
Do not assume it is the same as the integration-test issuer.

The functional/smoke issuer verifier is:

- mandatory in CI/pipeline when `VERIFY_OIDC_ISSUER=true` is set
- disabled by default for local runs
- opt-in locally by setting `VERIFY_OIDC_ISSUER=true`

Examples:

- integration-test profile uses `http://localhost:${wiremock.server.port}/o`
- a local docker/session may produce tokens with a different issuer such as `http://localhost:5556`

If local verification fails with a message like:

`OIDC_ISSUER mismatch: expected http://localhost:5000/o but token iss was http://localhost:5556`

then set `OIDC_ISSUER` to the actual token issuer and rerun the verification.

## Operational note

If OIDC discovery succeeds but `OIDC_ISSUER` does not match the token `iss` claim, authentication fails with `401 Unauthorized`.

## Acceptance Checklist

Before merging JWT issuer-validation changes, confirm all of the following:

- The active `JwtDecoder` is built from `spring.security.oauth2.client.provider.oidc.issuer-uri`.
- The active validator chain includes both `JwtTimestampValidator` and `JwtIssuerValidator(oidc.issuer)`.
- There is no disabled, commented-out, or alternate runtime path that leaves issuer validation off.
- `issuer-uri` is used for discovery and JWKS lookup only.
- `oidc.issuer` / `OIDC_ISSUER` is used as the enforced token `iss` value only.
- `OIDC_ISSUER` is explicitly configured and not guessed from the discovery URL.
- App config, Helm values, preview values, and CI/Jenkins values are aligned for the target environment.
- If `OIDC_ISSUER` changed, it was verified against a real token for the target environment.
- There is a test that accepts a token with the expected issuer.
- There is a test that rejects a token with an unexpected issuer.
- There is a test that rejects an expired token.
- There is decoder-level coverage using a signed token, not only validator-only coverage.
- At least one failure assertion clearly proves issuer rejection, for example by checking for `iss`.
- CI or build verification checks that a real token issuer matches `OIDC_ISSUER`, or the repo documents why that does not apply.
- Comments and docs do not describe the old insecure behavior.
- Any repo-specific difference from peer services is intentional and documented.

Do not merge if any of the following are true:

- issuer validation is constructed but not applied
- only timestamp validation is active
- `OIDC_ISSUER` was inferred rather than verified
- Helm and CI/Jenkins issuer values disagree without explanation
- only happy-path tests exist

## Configuration Policy

- `spring.security.oauth2.client.provider.oidc.issuer-uri` is used for OIDC discovery and JWKS lookup only.
- `oidc.issuer` / `OIDC_ISSUER` is the enforced JWT issuer and must match the token `iss` claim exactly.
- Do not derive `OIDC_ISSUER` from `IDAM_OIDC_URL` or the discovery URL.
- Production-like environments must provide `OIDC_ISSUER` explicitly.
- Requiring explicit `OIDC_ISSUER` with no static fallback in main runtime config is the preferred pattern, but it is not yet mandatory across all services.
- Local or test-only fallbacks are acceptable only when they are static, intentional, and clearly scoped to non-production use.
- The build enforces this policy with `verifyOidcIssuerPolicy`, which fails if `oidc.issuer` is derived from discovery config.
