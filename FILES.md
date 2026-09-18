# File manifest

If a folder looks empty after extracting, compare against this list. Every file below is
in the archive with a non-zero size.

## What changed in this build

**Removed `.idea/`** — the archive previously carried your original IntelliJ config, which
was generated before the `security/` and `service/` packages existed. Opening the project
with that stale config is the usual reason new packages appear empty or unindexed. It is
also listed in your own `.gitignore`, so it should never have been shipped.

**After extracting: open the folder in IntelliJ and let it import the Gradle project.**
IntelliJ regenerates `.idea/` from `build.gradle`. If it was already open, use
File -> Close Project, then reopen the extracted folder.

Also removed: `build/` and `.gradle/` (stale compiled output, including an `Approver.class`
with no matching source).

## Sources (71 .java files)

```
com/edws/gov/
├── EdwsServerApplication.java
├── common/
│   ├── ApiResponse.java                     success envelope
│   └── PageResponse.java                    stable pagination contract
├── config/
│   └── MongoConfig.java                     @EnableMongoAuditing
├── controller/
│   ├── AuthController.java                  /auth  login, refresh, me
│   └── UserController.java                  /users citizen management
├── dto/
│   ├── AdminProfileDto.java                 (yours, untouched)
│   ├── CitizenRequestDto.java               (yours, untouched)
│   ├── auth/
│   │   ├── AuthenticatedUserResponse.java
│   │   └── LoginRequest.java
│   └── user/
│       ├── AddressDto.java
│       ├── CreateCitizenRequest.java
│       ├── UpdateCitizenRequest.java
│       └── UserResponse.java
├── entity/                                  (yours, untouched — 13 files)
├── enums/                                   (yours, untouched — 8 files)
├── exception/
│   ├── ApiException.java                    the one exception app code throws
│   ├── ErrorCode.java                       catalogue: EDWS-AREA-NNNN
│   ├── ErrorResponse.java                   {errorType, message, code, ...}
│   ├── ErrorResponseWriter.java             same shape from inside the filter chain
│   ├── ErrorType.java
│   ├── FieldError.java
│   ├── GlobalExceptionHandler.java          @RestControllerAdvice, catches everything
│   └── TraceId.java
├── mapper/
│   └── UserMapper.java
├── repo/
│   ├── ApproverRepository.java              DISABLED — see note below
│   ├── UserRepository.java                  rewritten with real queries
│   └── ... (yours, untouched)
├── security/                                ← everything security-related, one package
│   ├── AccessControl.java                   bean "@access" — the authorization rules
│   ├── AuthHeaders.java                     X-Access-Token, X-Refresh-Token, ...
│   ├── JwtAccessDeniedHandler.java          403 in the standard error shape
│   ├── JwtAuthenticationEntryPoint.java     401 in the standard error shape
│   ├── JwtAuthenticationFilter.java         Bearer token -> SecurityContext
│   ├── JwtProperties.java                   binds edws.security.jwt.*
│   ├── JwtService.java                      issue / verify access + refresh
│   ├── Roles.java                           role name constants + startup drift check
│   ├── SecurityConfig.java                  the filter chain
│   ├── SecurityRoutes.java                  PUBLIC_ROUTES / PROTECTED_ROUTES — the contract
│   ├── SessionContext.java                  session.getCurrentUser()
│   ├── TokenType.java                       ACCESS | REFRESH
│   └── annotation/                       SPECIAL CASES ONLY
│       ├── AdminOnly.java
│       ├── CurrentUser.java                 parameter annotation
│       ├── GnOfficerOnly.java
│       ├── HasPermission.java               @HasPermission("MANAGE_USERS")
│       └── HasRole.java                     @HasRole(Roles.ADMIN)
└── service/
    ├── AuthService.java                     login, refresh
    └── UserService.java                     citizen CRUD + GN division scope rules
```

Docs: `API.md` at the project root.

## Known issue you need to decide on

`repo/ApproverRepository.java` is commented out. It referenced an `Approver` entity that
has no source file in your project — only a stale `Approver.class` under `build/`. That
means the module could not compile at all before this change. Either recreate the entity or
delete the repository; `DocumentApprover` is an embedded value object inside
`DisasterDocument`, not a `@Document` collection, so it cannot back a `MongoRepository`.
