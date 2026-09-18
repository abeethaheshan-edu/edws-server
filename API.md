# EDWS Server — User & Auth API

Base URL: `http://localhost:8080/api/v1`

The `/api/v1` prefix comes from `server.servlet.context-path` in `application.yaml`.
Controllers map `/users` and `/auth`, **not** `/api/v1/users`.

---

## 1. Routes

Declared in one place as constants: `com.edws.gov.security.SecurityRoutes`.
`PUBLIC_ROUTES` and `PROTECTED_ROUTES` are both wired directly into the filter chain.

Patterns there are written **without** `/api/v1` — the servlet container strips the
context path before Spring Security matches. This trips people up constantly.

### Public (no token)

| Method | Path            | Purpose |
|--------|-----------------|---------|
| POST   | `/auth/login`   | issues the token pair |
| POST   | `/auth/refresh` | authenticates via the refresh token itself |
| GET    | `/actuator/health` | probe |
| GET    | `/actuator/info`   | probe |
| —      | `/error`        | container error dispatch |

### Protected (valid access token required)

| Method | Path | Guard |
|--------|------|-------|
| GET    | `/auth/me` | token only |
| GET    | `/users/me` | token only |
| POST   | `/users/citizens` | `@GnOfficerOnly` |
| GET    | `/users/citizens` | `@GnOfficerOnly` |
| GET    | `/users/citizens/{citizenId}` | `@GnOfficerOnly` |
| PATCH  | `/users/citizens/{citizenId}` | `@GnOfficerOnly` |
| DELETE | `/users/citizens/{citizenId}` | `@GnOfficerOnly` |
| GET    | `/users/citizens/gn-division/{gnDivisionId}` | `@HasRole(...)` |

The chain ends with `anyRequest().authenticated()`, so a new endpoint that nobody
remembered to register **fails closed**, not open.

---

## 2. Authentication

Tokens are returned in **response headers**, never in the body.

```
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "gn.officer@gov.lk", "password": "Secret123!" }
```

```
200 OK
X-Access-Token             : eyJhbGciOiJIUzI1NiJ9...
X-Refresh-Token            : eyJhbGciOiJIUzI1NiJ9...
X-Token-Type               : Bearer
X-Access-Token-Expires-In  : 3600
X-Refresh-Token-Expires-In : 604800
Cache-Control              : no-store

{
  "success": true,
  "message": "Signed in successfully",
  "data": {
    "id": "68f0...",
    "email": "gn.officer@gov.lk",
    "fullName": "K. Perera",
    "role": "GN_OFFICER",
    "status": "ACTIVE",
    "administrativeScope": "GN_DIVISION",
    "permissions": ["MANAGE_USERS"],
    "gnDivisionId": "68a1..."
  },
  "timestamp": "2026-08-02T10:15:30Z"
}
```

Subsequent calls:

```
Authorization: Bearer <X-Access-Token>
```

Renewal — the refresh token goes in its own header, because by this point the access
token is already gone:

```
POST /api/v1/auth/refresh
X-Refresh-Token: eyJhbGciOiJIUzI1NiJ9...
```

A **new refresh token is also returned** (rotation), so a stolen one has a bounded life.

> **Browser clients:** every one of those headers must be in the CORS `exposedHeaders`
> list or JavaScript silently reads `null`. It already is — see `SecurityConfig`.

### Not implemented (deliberate)

Refresh tokens are stateless, so there is **no server-side revocation**. A stolen refresh
token stays valid until it expires. If you need logout-everywhere or immediate revocation,
you need a token store (Redis or a Mongo collection) keyed by the JWT `jti` claim, which
`JwtService` already sets.

---

## 3. Response contracts

### Success — `ApiResponse<T>`

```json
{
  "success": true,
  "message": "Citizens retrieved successfully",
  "data": { },
  "timestamp": "2026-08-02T10:15:30Z"
}
```

Paged endpoints put a `PageResponse<T>` in `data`:

```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 143,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

Query params: `?page=0&size=20&sort=createdAt,desc`

### Error — `ErrorResponse`

Identical shape for **every** failure, including ones raised inside the security filter
chain before any controller exists.

```json
{
  "errorType": "RESOURCE_NOT_FOUND",
  "message": "User not found",
  "code": "EDWS-USER-4040",
  "path": "/api/v1/users/citizens/68f0c1",
  "traceId": "a91f4c7d",
  "timestamp": "2026-08-02T10:15:30Z",
  "fieldErrors": []
}
```

Validation failures populate `fieldErrors`:

```json
{
  "errorType": "VALIDATION_ERROR",
  "message": "One or more fields are invalid",
  "code": "EDWS-REQ-4001",
  "fieldErrors": [
    { "field": "nic", "rejectedValue": "12345", "message": "NIC must be 9 digits followed by V/X, or 12 digits" }
  ]
}
```

**`code` is the support tool.** It goes to the client *and* into the log line, so:

```bash
grep "EDWS-USER-4040" application.log
```

**`traceId` narrows it to one request.** A user can read it off the screen; it appears on
the matching log entry.

---

## 4. Error codes

| Code | HTTP | errorType |
|------|------|-----------|
| `EDWS-REQ-4001` | 400 | VALIDATION_ERROR |
| `EDWS-REQ-4002` | 400 | BAD_REQUEST (unparsable body) |
| `EDWS-REQ-4003` | 400 | BAD_REQUEST (bad param type) |
| `EDWS-REQ-4004` | 400 | BAD_REQUEST (missing param) |
| `EDWS-AUTH-4010` | 401 | AUTHENTICATION_ERROR (bad credentials) |
| `EDWS-AUTH-4011` | 401 | AUTHENTICATION_ERROR (not signed in) |
| `EDWS-AUTH-4012` | 401 | AUTHENTICATION_ERROR (no token) |
| `EDWS-AUTH-4013` | 401 | AUTHENTICATION_ERROR (**expired** — refresh) |
| `EDWS-AUTH-4014` | 401 | AUTHENTICATION_ERROR (**invalid** — sign in again) |
| `EDWS-AUTH-4015` | 401 | AUTHENTICATION_ERROR (wrong token type) |
| `EDWS-AUTH-4016` | 403 | AUTHENTICATION_ERROR (account not active) |
| `EDWS-AUTHZ-4030` | 403 | AUTHORIZATION_ERROR |
| `EDWS-AUTHZ-4031` | 403 | AUTHORIZATION_ERROR (outside your area) |
| `EDWS-AUTHZ-4032` | 403 | AUTHORIZATION_ERROR (no GN division assigned) |
| `EDWS-USER-4040` | 404 | RESOURCE_NOT_FOUND |
| `EDWS-GND-4041` | 404 | RESOURCE_NOT_FOUND (GN division) |
| `EDWS-SYS-4044` | 404 | PATH_NOT_FOUND |
| `EDWS-SYS-4045` | 405 | METHOD_NOT_ALLOWED |
| `EDWS-SYS-4046` | 415 | UNSUPPORTED_MEDIA_TYPE |
| `EDWS-USER-4090` | 409 | CONFLICT (email exists) |
| `EDWS-USER-4091` | 409 | CONFLICT (NIC exists) |
| `EDWS-SYS-4092` | 409 | CONFLICT (duplicate key) |
| `EDWS-SYS-5000` | 500 | INTERNAL_ERROR |
| `EDWS-SYS-5001` | 500 | INTERNAL_ERROR (database) |

Codes are permanent — add new ones, never renumber.

`4013` vs `4014` matters to the client: expired means *silently refresh*, invalid means
*force sign-in*. An opaque 401 for both means a refresh loop or a needless logout.

---

## 5. Access control

Everything security-related lives in **`com.edws.gov.security`**.

### The model

**`SecurityRoutes` is the access-control contract.** A path is either public or protected,
and that is declared in one file. Nothing else participates in that decision.

```java
public static final String[] PUBLIC_ROUTES = {      // no token
        Auth.ABS_LOGIN,
        Auth.ABS_REFRESH,
        Infra.HEALTH,
        ...
};

public static final String[] PROTECTED_ROUTES = {   // valid access token required
        Auth.ABS_ME,
        Users.ABS_ALL,
        Infra.ACTUATOR_ALL
};
```

Both lists are wired straight into the filter chain:

```java
.requestMatchers(SecurityRoutes.PUBLIC_ROUTES).permitAll()
.requestMatchers(SecurityRoutes.PROTECTED_ROUTES).authenticated()
.anyRequest().authenticated()          // fail closed
```

**Adding an endpoint — the normal flow:**

1. Add the path constant to the relevant nested class in `SecurityRoutes`.
2. Add it to `PUBLIC_ROUTES` or `PROTECTED_ROUTES`.
3. Use the constant in the controller mapping.

That's it. No annotation. A protected route requires an access token and is open to any
authenticated user.

Forget step 2 entirely and `anyRequest().authenticated()` still catches it — the mistake
locks people out rather than letting them in.

Declare the same route in both lists and the application **refuses to start**
(`SecurityRoutes.validate()`). Spring Security applies the first matching rule, so a
duplicate silently resolves to whichever list is registered first while the developer reads
the other one and believes the opposite.

### Annotations — special cases only

Use one **only when an endpoint needs more than "must be signed in"**.

| Annotation | Rule |
|------------|------|
| `@HasRole(Roles.ADMIN)` | one or more roles — parameterised |
| `@HasPermission("MANAGE_USERS")` | one or more `Permission` values |
| `@GnOfficerOnly` | `GN_OFFICER` **and** scope `GN_DIVISION` **and** a division assigned |
| `@AdminOnly` | `SUPER_ADMIN` or `ADMIN` |
| `@CurrentUser` | parameter annotation, injects the `User` |

```java
// Common case — protected route, any signed-in user. No annotation.
@GetMapping(SecurityRoutes.Users.ME)
public ... getMyProfile() { }

// Special case — needs a specific role.
@GnOfficerOnly
@PostMapping(SecurityRoutes.Users.CITIZENS)
public ... createCitizen(...) { }

@HasRole({Roles.GN_OFFICER, Roles.ADMIN, Roles.SUPER_ADMIN})
@GetMapping(SecurityRoutes.Users.CITIZENS_BY_GN_DIVISION)
public ... getCitizensByGnDivision(...) { }
```

There is no `@PublicApi` and no `@Authenticated`. Both only restated what `SecurityRoutes`
already says, and a marker annotation that looks like it grants access but doesn't is worse
than no annotation at all.

**Always use the `Roles` constants, never raw strings.** A typo in a literal produces an
annotation that matches nobody and nothing tells you — the endpoint just starts returning
403 for everyone. `Roles` also verifies at startup that every constant still maps to a real
`Role` enum value.

### Two things that will bite you

**1. `@HasRole` needs the template bean.** It is a *templated* meta-annotation — Spring
Security substitutes `{value}`, turning `@HasRole({Roles.ADMIN, Roles.GN_OFFICER})` into
`hasAnyRole('ADMIN','GN_OFFICER')`. That only happens because `SecurityConfig` publishes:

```java
@Bean
static AnnotationTemplateExpressionDefaults annotationTemplateExpressionDefaults() {
    return new AnnotationTemplateExpressionDefaults();
}
```

It **must be `static`**. Spring has to publish it before it initialises method security; a
non-static `@Bean` is created too late, the template never applies, and `@HasRole` then
fails at runtime trying to parse the literal text `{value}` as SpEL.

**2. One authorization annotation per method.** Spring Security does not support two
`@PreAuthorize`-derived annotations on the same method — `@HasRole` together with
`@GnOfficerOnly` throws at startup. Pick one, or add a combining method to `AccessControl`.

### Endpoint access vs data scope

Annotations decide **who may call**. They cannot decide **which records come back** —
whether citizen *X* is inside the officer's division is only knowable *after* loading X.

So `UserService` enforces the second layer. Every method derives the division from the
officer's own record via `SessionContext`; it is never read from the request, so no request
can reach outside the officer's area.

`GET /users/citizens/gn-division/{id}` shows all three layers: the route is PROTECTED,
`@HasRole` admits GN officers and the admin tiers, then the service pins the officer to
their own division while letting `canViewAnyGnDivision()` roles read across.

Cross-division reads return **404, not 403** — a 403 would confirm the id is real, which is
enough to map the database by probing ids.

## 6. `session.getCurrentUser()`

```java
@Service
public class SomeService {
    private final SessionContext session;

    public void doWork() {
        User officer = session.getCurrentUser();
        GnDivision division = session.getCurrentGnDivision();
        String id = session.getCurrentUserId();
    }
}
```

`JwtAuthenticationFilter` already loaded and validated the user into the SecurityContext,
so this is a lookup, not another database round trip. Controllers therefore never need a
`userId` path variable to identify the caller — which removes a whole class of "pass
someone else's id" bugs.

Role and status are re-read from Mongo on every request rather than trusted from the token,
so suspending a user or changing their role takes effect **immediately** instead of at
token expiry.

---

## 7. Example: register a citizen

```
POST /api/v1/users/citizens
Authorization: Bearer <access token>
Content-Type: application/json

{
  "email": "nimal@example.lk",
  "password": "Secret123!",
  "fullName": "Nimal Silva",
  "nic": "199012345678",
  "phone": "0771234567",
  "address": {
    "houseNo": "45/2",
    "streetAddress1": "Temple Road",
    "city": "Panadura",
    "zipCode": "12500",
    "latitude": 6.7133,
    "longitude": 79.9026
  }
}
```

```
201 Created
Location: /users/citizens/68f0c1a2...
```

Note what the payload **cannot** set: role, status, GN division, province, district. Those
are assigned server-side from the officer's own record. Accepting them from the client
would let an officer register a citizen into someone else's division, or hand out an
`ADMIN` role. Mass assignment is the standard way this endpoint gets abused.

`DELETE` is a **soft delete** — it flips status to `INACTIVE`. Citizen records are
referenced by disaster documents, properties and family links, and are part of a government
audit trail; removing the document would leave those pointing at nothing.

---

## 8. Configuration

```yaml
server:
  servlet:
    context-path: /api/v1

edws:
  security:
    jwt:
      secret: ${JWT_SECRET}          # >= 32 bytes or the app refuses to start
      expiration-ms: 3600000         # access  — 1 hour
      refresh-expiration-ms: 604800000  # refresh — 7 days
      issuer: edws-server
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS:*}
```

Set `CORS_ALLOWED_ORIGINS` to the real front-end origins before production. `*` is a
development convenience.
