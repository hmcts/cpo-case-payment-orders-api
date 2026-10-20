# CCD-7882: controlled error responses

API responses no longer include exception class names or raw exception messages. Standard HTTP status descriptions replace diagnostic messages. Existing fixed validation messages remain where already defined. Payment validation details contain user-facing validation messages, never exception causes. Advice error bodies include a stable category `code` derived only from the HTTP status, for example `INVALID_REQUEST` or `INTERNAL_ERROR`. Advice/security-handler error/warn logs contain the same code without exception messages or stack traces. Original exceptions are logged only at DEBUG, which can be enabled for the advice/security handler package when needed; log access must stay restricted through the existing platform controls. No existing non-identifying reference was found in these handlers, so no reference is added. Security-filter responses remain empty. Advice and Spring Boot fallback JSON omit request paths.

Spring Boot fallback responses explicitly exclude exception, message, binding errors and stack traces, including when a caller requests them with query parameters. HTML error responses use a fixed generic page. Invalid bearer credentials return an empty 401; OAuth invalid-request errors retain 400. Both use a generic `WWW-Authenticate: Bearer` challenge; denied access returns 403 without diagnostic text. Token validation, endpoint permissions and document access checks are unchanged.

## Verification and release handoff

Local regression tests cover diagnostic redaction, malformed JSON, invalid UUID conversion, unsupported media/method status codes, and authentication error handlers. Existing relevant tests are also run. These checks do not establish deployment or end-to-end acceptance.

At merge/deployment planning, assign the delivery owner, QA contact, target date and retest environment. After deployment, record the actual revision, environment, UTC timestamp and sanitised response evidence in the tracking ticket. Do not record credentials, document contents or the OFFICIAL-SENSITIVE report. Obtain detailed reproduction material from SecOps through the approved channel.

## Local verification — 9 October 2026

Branch: `CCD-7882_Verbose_Error_Messages`, based on master `a614615`. Results apply to the local working changes; they are not deployment evidence.

```sh
./gradlew test --offline --tests 'uk.gov.hmcts.reform.cpo.exception.*' --tests 'uk.gov.hmcts.reform.cpo.security.*' --tests 'uk.gov.hmcts.reform.cpo.config.*' checkstyleMain checkstyleTest --console=plain
```

Result: **45 tests passed**, no failures or skips; `checkstyleMain` and `checkstyleTest` passed. Test reports are generated under `build/reports/tests/test/` and JUnit XML under `build/test-results/test/`.

## Support use of error codes

HTTP status remains unchanged. Advice JSON includes `code`; security filters continue to return empty
401/403 bodies with the code in handler logs. Spring Boot fallback responses omit reflected paths.
These backend changes do not make a frontend display the code automatically.

| Status | Code | Support action |
| --- | --- | --- |
| 400 / 422 | INVALID_REQUEST | Check request format and safe validation messages; correct input. |
| 401 | AUTHENTICATION_REQUIRED | Check sign-in/session; never collect bearer tokens. |
| 403 | ACCESS_DENIED | Check the user's intended permissions; escalate unexpected refusals. |
| 5xx | INTERNAL_ERROR | Escalate with service, time, status and available telemetry correlation. |

Other existing statuses retain distinct codes: RESOURCE_NOT_FOUND (404), METHOD_NOT_ALLOWED (405),
REQUEST_TOO_LARGE (413), UNSUPPORTED_MEDIA_TYPE (415), TOO_MANY_REQUESTS (429).
Other statuses use REQUEST_FAILED. Codes describe categories, not a confirmed root cause. They are defined as `ErrorCode` enum constants
in Java and serialized as the same strings in JSON; HTTP status remains numeric.

Local log-capture regression tests verify these examples at INFO (synthetic requests):

```text
ERROR Request failed: code=INVALID_REQUEST status=400
WARN  Authentication failed: code=AUTHENTICATION_REQUIRED status=401
WARN  Access denied: code=ACCESS_DENIED status=403
ERROR Request failed: code=INTERNAL_ERROR status=500
```

At DEBUG, an additional event contains the same category/status and original exception with stack trace.
Normal handler events contain no exception message or throwable. Collect service/environment, UTC time,
status, code and an existing telemetry operation ID if available. A category is shared by many requests
and cannot identify one incident. No new incident reference is added.

Enable DEBUG temporarily for the advice and security handler loggers only, then restore the previous level.
Diagnostic logs can contain sensitive details; retain existing restricted access. Local tests establish
handler output, not App Insights ingestion, severity filters, correlation or independent agent exception
collection. Validate those in the target environment before relying on the examples in App Insights.

## Local verification — 15 October 2026

Meaningful codes and INFO/DEBUG log capture: **48 focused tests passed**, no failures or skips.
The focused suites and Checkstyle commands above were rerun. This is local evidence, not deployment
or App Insights evidence. Changes remain uncommitted.

## Temporarily enabling diagnostics in App Insights

The original exception message and stack trace are passed to `log.debug(..., exception)`.
Normal ERROR/WARN events contain only the fixed category and HTTP status. Diagnostics are
available only for failures occurring while DEBUG is enabled; earlier details cannot be recovered.

Two thresholds must allow DEBUG:

1. Override Spring logging for these two handler classes in the target environment:

   ```yaml
   logging:
     level:
       uk.gov.hmcts.reform.cpo.exception.RestExceptionHandler: DEBUG
       uk.gov.hmcts.reform.cpo.security.SafeAuthenticationErrors: DEBUG
   ```

   Merge these entries with the existing `logging.level` configuration. Keep other application
   loggers at their existing levels; do not enable root DEBUG.

2. Allow the Application Insights Java 3.x agent to collect DEBUG events:

   ```text
   APPLICATIONINSIGHTS_INSTRUMENTATION_LOGGING_LEVEL=DEBUG
   ```

   The agent's default collection threshold is INFO. Changing Spring logging alone is insufficient.
   This agent threshold applies to all emitted logs, so review other loggers already set to DEBUG.
   It is not the agent's `selfDiagnostics` setting.

Apply these temporary overrides through the environment's normal deployment process; a restart/rollout
is needed when supplying startup configuration. Confirm the agent is attached and the connection-string
secret referenced by `lib/applicationinsights.json` is mounted. Existing repository configuration does
not establish that a deployed environment has these settings.

Reproduce a controlled failure. With the Java 3.x agent, normal events appear in `traces`, while events
carrying an exception object appear in `exceptions`. In the Application Insights Logs view, for example:

```kusto
union traces, (exceptions | extend message = outerMessage)
| where timestamp > ago(30m)
| where cloud_RoleName == "cpo-case-payment-orders-api"
| where message contains "code="
| project timestamp, operation_Id, severityLevel, message, itemType
| order by timestamp desc
```

Use `operation_Id` to inspect related request telemetry when correlation is available. In a
workspace table view, use the equivalent `AppTraces`/`AppExceptions` schema. Check ingestion,
filters and sampling if expected events are missing; local log-capture tests do not validate these.

Restore both thresholds to their previous values after diagnosis and confirm DEBUG events stop.
Original diagnostics may contain sensitive details; use existing restricted log access.
No deployed logging settings were changed by this documentation update.

Reference: [Microsoft Java agent logging configuration](https://learn.microsoft.com/en-us/azure/azure-monitor/app/java-standalone-config#autocollect-logging).

## Senior review follow-up

- Preserve OAuth bearer error statuses: an invalid bearer request remains 400 with INVALID_REQUEST
  in handler logs; authentication failures remain 401. Bearer challenges and bodies contain no diagnostics.
- Move original diagnostics from the owned IDAM/document-client/filter failure paths to DEBUG.
  Keep generic operational events at ERROR/WARN. This does not change HTTP status or authentication checks.
- Update functional response fixtures to assert safe codes/messages instead of exception classes,
  internal messages or identifiers. Keep existing status expectations and fixed user-facing validation details.
- The shared ServiceAuthFilter is replaced with an owned filter using the same validator/allowlist
  and 401/403 semantics. Audit failure diagnostics and operational identifiers are available only at DEBUG.

Functional tests require the services and dependencies used by BEFTA. Fixture updates and local unit
checks are not a deployed functional-test pass; run the functional suites in the target test environment.

For those additional owned paths, temporarily add the relevant logger entries alongside the earlier
DEBUG configuration, with the same App Insights threshold, then restore previous values:

```yaml
logging:
  level:
    uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter: DEBUG
    uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl: DEBUG
```

Review-fix validation: **73 tests passed**, no failures or skips; Checkstyle and functional-test
Java compilation/resource processing passed. **13 functional JSON fixtures** were updated and
parsed successfully. Functional suites were not executed against deployed services.

## Complete request-path fixes — 16 October 2026

Public health endpoints retain their availability and status codes but use `show-details: never`,
so failed indicators cannot publish exception text through health JSON. Detailed audit exceptions,
service-authentication exceptions and operational user/case/document/service identifiers are moved
from ERROR/WARN into DEBUG; normal events use generic summaries or fixed codes.

The shared S2S auto-configuration bean (`serviceAuthFiler`) is replaced after initialization by
`SafeServiceAuthFilter`, retaining the same ServiceAuthTokenValidator implementation and configured service allowlist,
Bearer normalization, empty 401/403 responses and downstream exception handling semantics.
Filter overrides with distinct bean names remain intact. This wiring depends on the shared bean name and
has a regression test; review it when upgrading the service-auth library.

Request parsing failures remain 400/INVALID_REQUEST. Response serialization failures in translation
and CDAM now return 500/INTERNAL_ERROR before commitment, correcting the earlier broad conversion
exception mapping. This is a deliberate status correction; existing client-validation status
expectations stay unchanged. Once a streaming response is committed, its HTTP status/body cannot
be replaced; deployment checks must include aborted downloads and telemetry behaviour.

For temporary diagnosis, enable DEBUG only for the relevant owned security, audit, controller or
service logger, together with the App Insights collection threshold described above. Restore both
thresholds after diagnosis. Full deployed BEFTA execution and App Insights ingestion remain
separate release checks. These changes do not promise that every third-party/framework logger
will suppress diagnostics; those still require environment verification.

Additional logger overrides for the complete-path fixes (use only those needed):

```yaml
logging:
  level:
    uk.gov.hmcts.reform.cpo.security.SafeServiceAuthFilter: DEBUG
    uk.gov.hmcts.reform.cpo.auditlog.AuditInterceptor: DEBUG
    uk.gov.hmcts.reform.cpo.auditlog.aop.AuditAspect: DEBUG
```

### Verified local results — 16 October 2026

**148 affected tests passed**, zero failures/errors/skips. `checkstyleMain`, `checkstyleTest`,
`compileIntegrationTestJava` and `compileFunctionalTestJava` passed. Regression coverage includes
public health UP/DOWN response redaction (200/503), S2S 401/403/allowlist/Bearer handling and actual
shared-bean replacement, INFO/DEBUG diagnostic separation, response serialization as 500, and
preserved 405 Allow / 415 Accept headers. Translation/CPO also cover whole security-chain logging
and committed error handling; CPO/CDAM cover audit interceptor and SpEL failures with log capture.

Health functional fixtures now expect status without component diagnostics. Existing error fixtures
retain their HTTP statuses except the documented serialization correction. Integration/functional
sources were compiled; deployed BEFTA execution, real interrupted downloads, secret mounts,
upstream gateways and App Insights capture/sampling still need environment verification.
Changes are uncommitted; no deployment configuration was applied to a running environment.

## Framework failure-path follow-up — 16 October 2026

Health indicators can log their own exceptions at WARN even when public health details are hidden.
`SafeHealthLoggingConfiguration` registers an owned Logback filter for health-indicator logger classes.
It replaces their WARN/ERROR events with a fixed summary, preserving severity, and routes original
messages/throwables to `logging.SafeHealthDiagnosticsFilter` at DEBUG. The filter is removed when
its Spring context closes. Health results and HTTP statuses are unchanged. This application uses
Logback; custom contributors that log through unrelated logger names need separate verification.

Bearer authentication now has an explicit failure handler. AuthenticationServiceException (including
IDAM outages) retains the existing 401 contract, with an empty body and a generic Bearer challenge. The original exception is
logged at DEBUG only, preventing Spring's default rethrow from reaching container ERROR logs.
Normal invalid-token/invalid-request handling continues through the existing safe entry point.

For temporary diagnosis, add the following alongside the previously documented App Insights
collection threshold, then restore the prior level:

```yaml
logging:
  level:
    uk.gov.hmcts.reform.cpo.logging.SafeHealthDiagnosticsFilter: DEBUG
```

IDAM failure diagnostics use the existing `uk.gov.hmcts.reform.cpo.security.SafeAuthenticationErrors` DEBUG setting.

Framework-path regression results: **150 affected tests passed**, no failures/errors/skips.
Tests now use real AbstractHealthIndicator implementations (including nested composite contributors)
and capture propagated INFO/DEBUG logs. Translation/CPO also simulate IDAM failures through the
full bearer chain, verifying empty 401 responses, a generic WWW-Authenticate header, no normal-level
throwables, and DEBUG-only causes. Public health status/body and existing bearer checks remain green.
Checkstyle and integration/functional compilation are checked separately; deployed functional tests
and App Insights ingestion are still outstanding.

## SQL diagnostics and authentication compatibility

IDAM outages retain the existing 401/empty-body/`WWW-Authenticate: Bearer` contract.
The authentication boundary owns the normal-level failure event; converter and service layers
preserve causes for the boundary's DEBUG diagnostic without duplicate ERROR events.

Hibernate's `org.hibernate.engine.jdbc.spi.SqlExceptionHelper` WARN/ERROR messages are redirected to
owned DEBUG logging because they can include constraint names, case IDs and order references.
Request failures produce the advice's single safe code/status event. Original SQL exceptions, including
translated 409 conflicts, remain attached as causes and are available through the advice's DEBUG logger.
A real PostgreSQL duplicate-key integration test captures root logs and checks the safe event.


## Ticket-description confirmation — 16 October 2026

The source review identified reflected paths, inherited 405 warnings, dependency/health logging,
logging reloads, access-denied challenges and duplicate IDAM DEBUG events. These source gaps are
addressed in the follow-up below; deployed compliance still requires environment verification.

All three checked-out branches are `CCD-7882_Verbose_Error_Messages`. Their ErrorCode enum bodies
and mappings are identical to the proposed table. Generic advice/HTML responses, disabled Boot
exception/message/trace/binding output and DEBUG diagnostics implement the main remediation.
This is a source/documentation review, not evidence of deployment, administrator-only log access,
retesting the reported ITHC URLs, or stakeholder approval of the shared code names. See the README
for the concise contract and unresolved branch-specific gaps. No implementation changes or test
reruns were made for this documentation confirmation.

## Access-denied challenges and complete diagnostic paths

Authenticated access denials retain `WWW-Authenticate: Bearer error="insufficient_scope"`
without diagnostic descriptions or URIs, including method-security denials handled by the advice.
Error JSON omits request paths in both advice and Spring Boot fallback responses. Functional
fixtures use local error-response bases without inherited path expectations; request status codes remain unchanged.
Spring 405 handling preserves Allow headers while avoiding the superclass's rejected-method WARN.

The context-managed Logback filter also controls dependency WARN/ERROR events, including connection
pools, servlet/framework errors and slow-health contributor warnings. Normal events keep severity
and fixed categories; logger names, messages and causes are available only at the owned
`uk.gov.hmcts.reform.cpo.logging.SafeHealthDiagnosticsFilter` DEBUG logger. Events below a source
logger's configured threshold remain disabled. This protection applies after filter installation;
pre-context startup output, container access logs and agent collection require deployment checks.

IDAM converter and service layers preserve exception causes without logging duplicates. The security
handler or advice logs the original cause once at DEBUG. Deployed functional verification and App
Insights checks remain outstanding.

The diagnostic filter is reset-resistant: logging reloads reinstall it without duplicate registration,
and closing the owning Spring context removes it and its listener. Regression tests cover disabled
source levels, logging resets, dependency causes, and a real slow healthy indicator (HTTP 200).


## Final branch requirement review — 16 October 2026

Reviewed controller/security/fallback paths, unchanged callers, dependency diagnostics and the supplied
error-code table. All normal owned failure logs use safe summaries; original causes remain available
through the documented DEBUG loggers. Production acceptance requires deployed ITHC/BEFTA retesting,
upstream error-page checks, administrator-only log access and App Insights/agent verification.
The README remains support/maintenance guidance without ticket or branch history.

An isolated executable probe of pre-change advice (9b8a457^) confirmed inherited response-writing 500,
method 405, media 415 and upload-size 413 mappings; these are preserved. IDAM outages remain empty 401.
Bearer challenges now retain allowlisted protocol error codes without descriptions/URIs/scopes.
Annotation reason text is excluded from generic JSON. Query failure wrappers retain the original cause
for boundary DEBUG logging while retaining 400. Intermediate JDBC/transaction/MVC diagnostics defer
normal failure summaries to the terminal boundary. SqlExceptionHelper no longer needs OFF: its original
messages are routed to owned DEBUG without duplicate WARN/ERROR events.


Final local validation: **224 unit tests and 109 integration tests passed**, no failures,
errors or skips. Main/unit/integration/functional Checkstyle and functional Java compilation passed;
`git diff --check` passed. Integration tests include the configured authentication filters and actual
request paths. All 97 functional feature JSON files parsed successfully.

```sh
./gradlew test integration --offline checkstyleMain checkstyleTest checkstyleIntegrationTest checkstyleFunctionalTest compileFunctionalTestJava --console=plain
```

Deployed functional/ITHC retesting, upstream/container responses, administrator-only log access and
App Insights/independent agent instrumentation remain environment acceptance checks. Shared code
names are suitable categories; stakeholder approval is not established by the local tests.
The implementation and local validation above are included in commit `33e9c72`.

## Functional fixture fixes

- Local `CPO_400_Response`, `CPO_404_Response` and `CPO_409_Response` bases avoid inheriting
  request-path expectations from BEFTA's shared error bodies.
- `CPO_403_Response` expects safe `ACCESS_DENIED` JSON and the required
  `WWW-Authenticate: Bearer error="insufficient_scope"` challenge, without diagnostic descriptions.
- Error scenarios expect safe messages and codes rather than exception names or reflected identifiers.
  HTTP status expectations remain unchanged.

Fixture JSON validation and Java compilation passed locally; these do not establish a deployed
functional-test pass. Rerun BEFTA in Jenkins to verify the deployed responses against the fixtures.

## Real local HTTP verification — 19 October 2026

`UnauthenticatedHttpIT` adds seven tests using an embedded server, loopback HTTP and a raw TCP request.
Existing MockMvc/direct-controller tests did not exercise these container dispatches. The fixture
imports production security/error/logging configuration and the real public root/health/info endpoints;
IDAM/S2S clients are mocked and verified unused. No credentials, database or remote services are needed.

Coverage includes public health UP/DOWN (200/503), anonymous protected requests, safe access-denied
challenges, malformed JSON and request targets, 405 Allow headers, missing public resources, and
JSON/HTML servlet/filter failures with diagnostic query parameters. WARN/ERROR logs are captured.
Test-only probes are excluded from application component scanning.

These tests exposed internal ERROR dispatches replacing a servlet 500 with an authentication 401.
Security now permits container ERROR dispatches to render the safe error page with the original status.
Direct anonymous requests to `/error` and protected application endpoints still return 401.

```sh
./gradlew integration --tests '*UnauthenticatedHttpIT' --console=plain
./gradlew check integration --console=plain
```

Validation: **224 unit tests and 115 integration tests passed**, no failures/errors/skips; all
Checkstyle checks passed and functional JSON parsed successfully. Deployed BEFTA/ITHC retesting,
real IDAM/S2S integration, gateway/native container responses, pre-context startup logs, administrator
log access and App Insights/independent agent behaviour remain deployment checks.

XML-only fallback negotiation was also verified over loopback HTTP: a servlet 500 retains status 500
with an empty body and no Content-Type when XML cannot be produced. HTML fallback still returns
the fixed generic error page. This is content negotiation, not a guarantee of a JSON body for every Accept header.
