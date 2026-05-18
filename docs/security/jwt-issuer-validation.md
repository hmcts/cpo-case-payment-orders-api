# JWT issuer validation

## Summary

- JWT issuer validation is enabled in the active `JwtDecoder`.
- OIDC discovery and issuer enforcement are configured separately on purpose.
- `OIDC_ISSUER` / `oidc.issuer` remains the primary enforced issuer.
- The decoder rejects tokens whose `iss` claim does not exactly match `oidc.issuer` or a confirmed value in `oidc.allowed-issuers`, even if signature validation and timestamps succeed.
- The enforced issuer must come from a real access token `iss` claim, not from discovery metadata or deployment naming.
- `OIDC_ALLOWED_ISSUERS` is optional and should stay unset unless real tokens prove that more than one issuer is valid for the same runtime.
- See [HMCTS Guidance](#hmcts-guidance) for the central policy reference.

## Current behavior

`SecurityConfiguration.jwtDecoder()` discovers OIDC metadata and JWKS from `spring.security.oauth2.client.provider.oidc.issuer-uri`, then applies a validator chain that enforces:

- token timestamps
- exact issuer claim equality with the accepted issuer set built from `oidc.issuer` first, plus optional `oidc.allowed-issuers`

This keeps key discovery separate from the trust boundary for issuer claims.

## Code path

- Decoder wiring: `src/main/java/uk/gov/hmcts/reform/cpo/config/SecurityConfiguration.java`
- Validator coverage: `src/test/java/uk/gov/hmcts/reform/cpo/config/SecurityConfigurationTest.java`
- Integration coverage: `src/integrationTest/java/uk/gov/hmcts/reform/cpo/security/JwtIssuerValidationIT.java`
- Integration token generation: `src/integrationTest/java/uk/gov/hmcts/reform/cpo/BaseTest.java`

## Configuration contract

| Setting | Used for | Must match or point to | Notes |
| --- | --- | --- | --- |
| `IDAM_OIDC_URL` | OIDC metadata discovery and JWKS retrieval | the target IDAM base URL | feeds `spring.security.oauth2.client.provider.oidc.issuer-uri` as `${IDAM_OIDC_URL}/o` |
| `OIDC_ISSUER` | primary enforced JWT issuer validation | the exact `iss` claim in real bearer tokens | do not infer it from discovery metadata, deployment naming, or guesswork |
| `OIDC_ALLOWED_ISSUERS` | optional additive issuer allow-list | comma-separated exact `iss` claim values confirmed from real bearer tokens | leave unset unless multiple token issuers are confirmed; no wildcards, prefixes, or discovery-derived values |

Do not rely on the fallback in `application.yaml` for any real runtime. Compose, Helm, and CI should all provide `OIDC_ISSUER` explicitly.
Leave `OIDC_ALLOWED_ISSUERS` unset unless a target runtime has confirmed multiple valid token issuers.
See [HMCTS Guidance](#hmcts-guidance) for the policy reference when deciding or reviewing service-level issuer configuration.

## Runtime activation

By default, runtime issuer validation is single-issuer:

- `OIDC_ISSUER` must be present and is always the primary issuer.
- `OIDC_ALLOWED_ISSUERS` is optional. If it is absent from the container environment, Spring injects an empty `oidc.allowed-issuers` value.
- An empty `oidc.allowed-issuers` value means the accepted issuer set contains only `OIDC_ISSUER`.
- To activate multi-issuer validation, add `OIDC_ALLOWED_ISSUERS` explicitly to that deployment environment with confirmed additional `iss` values.

The same rule applies to the build-integrated functional/smoke issuer verifier. It reads `OIDC_ALLOWED_ISSUERS` directly from the process environment. If the verifier must accept the same additional issuer as the deployed runtime, Jenkins must export `OIDC_ALLOWED_ISSUERS` for that run as well.

## How to derive `OIDC_ISSUER`

- Do not guess the issuer from the public discovery URL alone.
- Decode only the JWT payload from a real access token for the target environment and inspect the `iss` claim.
- Do not store or document full bearer tokens. Record only the derived issuer value.
- If multiple issuers are confirmed for one runtime, set `OIDC_ISSUER` to the primary issuer and `OIDC_ALLOWED_ISSUERS` to the additional exact `iss` values.

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
- Preview Helm template: `charts/cpo-case-payment-orders-api/values.preview.template.yaml`
- Local Docker runtime: `docker-compose.yml`
- Local CPO docker stack: `cpo-docker/compose/cpo.yml`
- Integration test profile: `src/integrationTest/resources/application-itest.yaml`
- Build verification tasks: `build.gradle`

`Jenkinsfile_CNP` sets `IDAM_API_URL_BASE` and `S2S_URL_BASE` for BEFTA-style tests and also exports `OIDC_ISSUER` for the build-integrated issuer verifier. `Jenkinsfile_nightly` exports the same issuer-verifier environment for nightly runs. Runtime issuer settings still come from Helm values or explicit environment overrides.

At the time of this patch, Helm contains an `OIDC_ISSUER` value but this repo does not contain a captured real token proving that value. Treat that environment value as pending verification until a real token is decoded. `OIDC_ALLOWED_ISSUERS` is intentionally unset in Helm, Jenkins, and docker config because this repo does not contain confirmed evidence of multiple valid issuers for those runtimes.

## Test coverage

- Unit validator checks confirm:
  - primary configured issuer is accepted
  - configured allowed issuers are accepted
  - issuer matching is exact, not prefix-based
  - unexpected issuer is rejected
  - missing issuer is rejected
  - expired tokens are rejected even when issuer matches
- Decoder coverage confirms tokens still require a valid signature.
- Integration coverage aligns the test profile issuer with WireMock OIDC discovery, accepts a confirmed secondary issuer configured through `oidc.allowed-issuers`, and rejects a token signed with the test key but carrying an unexpected issuer

## Remaining operational step

Decode a real IDAM bearer token from the target environment and compare its `iss` claim with the deployment `OIDC_ISSUER` value before treating that environment configuration as verified.

## Local verification

For local running, `OIDC_ISSUER` must match the `iss` claim in the real access token your local setup is using.
Do not assume it is the same as the integration-test issuer.

The functional/smoke issuer verifier is:

- mandatory in CI/pipeline when `VERIFY_OIDC_ISSUER=true` is set
- disabled by default for local runs
- opt-in locally by setting `VERIFY_OIDC_ISSUER=true`

The build also enforces a separate static policy check with `verifyOidcIssuerPolicy`, which fails if enforced issuer config is derived from discovery configuration.

Examples:

- integration-test profile uses `http://localhost:${wiremock.server.port}/o`
- a local docker/session may produce tokens with a different issuer such as `http://localhost:5556`

If local verification fails with a message like:

```text
OIDC issuer mismatch: expected one of `http://localhost:5000/o` but token iss was `http://localhost:5556`
```

then set `OIDC_ISSUER` to the actual primary token issuer and rerun the verification. If `http://localhost:5556` is a confirmed additional issuer for that same runtime, keep `OIDC_ISSUER` as the primary issuer and add `http://localhost:5556` to `OIDC_ALLOWED_ISSUERS`.

## Operational note

If OIDC discovery succeeds but the token `iss` claim matches neither `OIDC_ISSUER` nor a configured `OIDC_ALLOWED_ISSUERS` value, authentication fails with `401 Unauthorized`.

## Acceptance Checklist

Before merging JWT issuer-validation changes, confirm all of the following:

- The active `JwtDecoder` is built from `spring.security.oauth2.client.provider.oidc.issuer-uri`.
- The active validator chain includes both `JwtTimestampValidator` and exact `iss` validation against `oidc.issuer` plus optional `oidc.allowed-issuers`.
- There is no disabled, commented-out, or alternate runtime path that leaves issuer validation off.
- `issuer-uri` is used for discovery and JWKS lookup only.
- `oidc.issuer` / `OIDC_ISSUER` is used as the primary enforced token `iss` value only.
- `oidc.allowed-issuers` / `OIDC_ALLOWED_ISSUERS` is unset unless multiple token issuers are confirmed from real tokens.
- Any allowed issuer value is matched exactly, with no wildcard, regex, prefix, or suffix behavior.
- `OIDC_ISSUER` is explicitly configured and not guessed from the discovery URL.
- The accepted issuer set for the target environment is aligned across runtime config and any CI/verifier configuration used for that same environment.
- If `OIDC_ISSUER` or `OIDC_ALLOWED_ISSUERS` changed, the values were verified against real tokens for the target environment.
- There is a test that accepts a token with the expected issuer.
- There is a test that accepts a token with a configured allowed issuer.
- There is a test that rejects a token with an unexpected issuer.
- There is a test that rejects a token with no `iss` claim.
- There is a test that rejects an expired token.
- There is a test that rejects a token without a valid signature.
- There is decoder-level coverage using a signed token, not only validator-only coverage.
- At least one failure assertion clearly proves issuer rejection, for example by checking for `iss`.
- CI or build verification checks that a real token issuer matches the accepted issuer set, or the repo documents why that does not apply.
- Comments and docs do not describe the old insecure behavior.
- Any repo-specific exception to the standard issuer-validation pattern is intentional and documented.

Do not merge if any of the following are true:

- issuer validation is constructed but not applied
- only timestamp validation is active
- `OIDC_ISSUER` was inferred rather than verified
- `OIDC_ALLOWED_ISSUERS` was populated without confirmed real-token `iss` values
- Helm and CI/Jenkins issuer values disagree without explanation
- only happy-path tests exist

## Configuration Policy

- `spring.security.oauth2.client.provider.oidc.issuer-uri` is used for OIDC discovery and JWKS lookup only.
- `oidc.issuer` / `OIDC_ISSUER` is the primary enforced JWT issuer and must match the token `iss` claim exactly.
- `oidc.allowed-issuers` / `OIDC_ALLOWED_ISSUERS` is an optional additive comma-separated list for confirmed secondary token issuers only.
- Do not derive `OIDC_ISSUER` from `IDAM_OIDC_URL` or the discovery URL.
- Do not derive `OIDC_ALLOWED_ISSUERS` from `IDAM_OIDC_URL`, `OIDC_ISSUER`, deployment naming, or discovery metadata.
- Production-like environments must provide `OIDC_ISSUER` explicitly.
- Requiring explicit `OIDC_ISSUER` with no static fallback in main runtime config is the preferred pattern, but it is not yet mandatory across all services.
- Local or test-only fallbacks are acceptable only when they are static, intentional, and clearly scoped to non-production use.
- The build enforces this policy with `verifyOidcIssuerPolicy`, which fails if enforced issuer config is derived from discovery config.

## HMCTS Guidance

- [JWT iss Claim Validation guidance](https://tools.hmcts.net/confluence/spaces/SISM/pages/1958056812/JWT+iss+Claim+Validation+for+OIDC+and+OAuth+2+Tokens#JWTissClaimValidationforOIDCandOAuth2Tokens-Configurationrecommendation)
- Use that guidance as the reference point for service-level issuer decisions and configuration recommendations.
