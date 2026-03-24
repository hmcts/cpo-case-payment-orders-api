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
s = sys.argv[1]
s += '=' * (-len(s) % 4)
print(json.loads(base64.urlsafe_b64decode(s))["iss"])
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
