# Security & Service — File Reference

This document describes the actual files in `src/main/java/com/example/demo/security` and `src/main/java/com/example/demo/service` and the `AuthController`. It includes purpose, important methods, observed issues, and short recommendations.

## Files covered
- security/
  - JwtUtil.java
  - SecurityConfig.java
- service/
  - AdminInterceptor.java
  - UserService.java
  - WebClientConfig.java
  - WebConfig.java
- controller/
  - AuthController.java

---

## security/JwtUtil.java
Purpose
- Create and parse JWT tokens used by the app.

Key details
- Uses io.jsonwebtoken (JJWT).
- Generates an HS256 key in-memory: `private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);`
- Methods:
  - `generateToken(String email)` — subject=email, 24h expiry.
  - `extractEmail(String token)` — returns subject.

Issues / recommendations
- Current implementation creates a new random signing key at startup. Tokens become invalid after restart.
  - Recommendation: load a fixed secret/key from application properties or environment (not hard-coded in repo).
- Consider adding validation methods (expiration check, signature verification wrapped with clear exceptions).
- Use appropriate claims (roles/authorities) if needed.

---

## security/SecurityConfig.java
Purpose
- Spring Security configuration: defines `SecurityFilterChain` and a `PasswordEncoder` bean.

Key details
- CSRF disabled.
- Permits access to `/api/auth/**`, root and static resources and currently calls `.anyRequest().permitAll()` for development.
- Declares `BCryptPasswordEncoder` as `PasswordEncoder` bean.

Issues / recommendations
- `.anyRequest().permitAll()` is insecure for production — wrap with a `dev` profile or change to `.anyRequest().authenticated()` when enabling real auth.
- If JWT auth is used later, register a JWT authentication filter here and remove form/basic defaults.
- Keep CSRF enabled for browser sessions if using cookies/sessions.

---

## service/AdminInterceptor.java
Purpose
- MVC interceptor that restricts access to admin routes (`WebConfig` wires it to `/admin/**` and `/users/**`).

Key details
- Checks `HttpSession` attributes:
  - `currentUser` presence,
  - `isAdmin` boolean (casts attribute to `Boolean`) — if true allows request.
- On failure redirects to `/error/403`.

Issues / recommendations
- Logic is fragile and relies on session attributes being correctly set elsewhere.
- Casting attributes repeatedly is error-prone; prefer:
  - store a strongly typed object in session (e.g., User DTO) or
  - centralize auth checks using Spring Security (authorities) rather than manual session flags.
- Redirects inside API endpoints will confuse API clients; use proper HTTP status (403) for APIs.

---

## service/UserService.java
Purpose
- Service for user data operations; uses `UserRepository`.

Key details
- Methods:
  - `getAllUsers()`, `saveUser(User)`, `deleteUser(Integer)`, `getUserByEmail(String)`, `getUserById(Integer)`.
- Declares a `BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();` field.

Issues / recommendations
- `saveUser` currently does not encode/hash the password before saving — serious security issue.
  - Recommendation: call `passwordEncoder.encode(user.getPassword())` in `saveUser` (or accept already-hashed password from controller only after validation).
- Prefer injecting `PasswordEncoder` bean (from SecurityConfig) instead of creating a new encoder instance.

---

## service/WebClientConfig.java
Purpose
- Provides a `WebClient.Builder` bean for reactive HTTP calls.

Key details
- Simple config, returns `WebClient.builder()`.

Notes
- Good for external API calls; no issues observed.

---

## service/WebConfig.java
Purpose
- MVC configuration: registers `AdminInterceptor` for route protection.

Key details
- Adds `adminInterceptor` to:
  - `/admin/**` (excludes `/error/**`, `/login`, `/register`)
  - `/users/**` (excludes several login/register/error routes)

Issues / recommendations
- Interceptor-based protection duplicates security concerns already handled by Spring Security. Consider moving authorization logic to Spring Security (roles/authorities) and use `@PreAuthorize` or filter chains.
- Current exclusion lists should be reviewed for completeness.

---

## controller/AuthController.java
Purpose
- REST endpoints for authentication: register and login.

Endpoints
- POST `/api/auth/register`
  - Accepts `User` JSON, calls `userService.saveUser(user)`, returns 200 with plain text message.
- POST `/api/auth/login`
  - Accepts `User` JSON (email/password).
  - Looks up user by email and checks password using a freshly constructed `BCryptPasswordEncoder().matches(...)`.
  - On success generates a JWT via `JwtUtil.generateToken(email)` and returns plain text "JWT Token: <token>".
  - On failure returns 401 with "Invalid credentials".

Issues / recommendations
- Registration: must ensure password is hashed by `UserService.saveUser` before persisting. Currently that is missing.
- Login: uses a new local BCryptPasswordEncoder instead of the shared `PasswordEncoder` bean — inject the bean to avoid inconsistencies.
- Return structured JSON responses instead of plain text (e.g. `{ "token": "<jwt>" }`).
- Do not return or log raw passwords anywhere.
- Consider validating the incoming payload (non-null email/password) and return consistent error responses.

---

## General security notes / next steps
- Make password hashing consistent:
  - Hash in service layer on registration.
  - Use same `PasswordEncoder` bean for comparison on login.
- Externalize secrets:
  - Move JWT signing key to `application.properties` or an environment variable and load it in `JwtUtil`.
- Prefer Spring Security for auth/authorization (use authorities/roles) instead of session attributes + interceptors.
- Ensure seeded users in `data.sql` use BCrypt-hashed passwords if `login` expects hashed values.
- For dev vs prod behavior:
  - Use Spring profiles (e.g., `application-dev.properties`) to enable `permitAll()` only in dev.

---
# Security & Service — File Reference

This document describes the actual files in `src/main/java/com/example/demo/security` and `src/main/java/com/example/demo/service` and the `AuthController`. It includes purpose, important methods, observed issues, and short recommendations.

## Files covered
- security/
  - JwtUtil.java
  - SecurityConfig.java
- service/
  - AdminInterceptor.java
  - UserService.java
  - WebClientConfig.java
  - WebConfig.java
- controller/
  - AuthController.java

---

## security/JwtUtil.java
Purpose
- Create and parse JWT tokens used by the app.

Key details
- Uses io.jsonwebtoken (JJWT).
- Generates an HS256 key in-memory: `private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);`
- Methods:
  - `generateToken(String email)` — subject=email, 24h expiry.
  - `extractEmail(String token)` — returns subject.

Issues / recommendations
- Current implementation creates a new random signing key at startup. Tokens become invalid after restart.
  - Recommendation: load a fixed secret/key from application properties or environment (not hard-coded in repo).
- Consider adding validation methods (expiration check, signature verification wrapped with clear exceptions).
- Use appropriate claims (roles/authorities) if needed.

---

## security/SecurityConfig.java
Purpose
- Spring Security configuration: defines `SecurityFilterChain` and a `PasswordEncoder` bean.

Key details
- CSRF disabled.
- Permits access to `/api/auth/**`, root and static resources and currently calls `.anyRequest().permitAll()` for development.
- Declares `BCryptPasswordEncoder` as `PasswordEncoder` bean.

Issues / recommendations
- `.anyRequest().permitAll()` is insecure for production — wrap with a `dev` profile or change to `.anyRequest().authenticated()` when enabling real auth.
- If JWT auth is used later, register a JWT authentication filter here and remove form/basic defaults.
- Keep CSRF enabled for browser sessions if using cookies/sessions.

---

## service/AdminInterceptor.java
Purpose
- MVC interceptor that restricts access to admin routes (`WebConfig` wires it to `/admin/**` and `/users/**`).

Key details
- Checks `HttpSession` attributes:
  - `currentUser` presence,
  - `isAdmin` boolean (casts attribute to `Boolean`) — if true allows request.
- On failure redirects to `/error/403`.

Issues / recommendations
- Logic is fragile and relies on session attributes being correctly set elsewhere.
- Casting attributes repeatedly is error-prone; prefer:
  - store a strongly typed object in session (e.g., User DTO) or
  - centralize auth checks using Spring Security (authorities) rather than manual session flags.
- Redirects inside API endpoints will confuse API clients; use proper HTTP status (403) for APIs.

---

## service/UserService.java
Purpose
- Service for user data operations; uses `UserRepository`.

Key details
- Methods:
  - `getAllUsers()`, `saveUser(User)`, `deleteUser(Integer)`, `getUserByEmail(String)`, `getUserById(Integer)`.
- Declares a `BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();` field.

Issues / recommendations
- `saveUser` currently does not encode/hash the password before saving — serious security issue.
  - Recommendation: call `passwordEncoder.encode(user.getPassword())` in `saveUser` (or accept already-hashed password from controller only after validation).
- Prefer injecting `PasswordEncoder` bean (from SecurityConfig) instead of creating a new encoder instance.

---

## service/WebClientConfig.java
Purpose
- Provides a `WebClient.Builder` bean for reactive HTTP calls.

Key details
- Simple config, returns `WebClient.builder()`.

Notes
- Good for external API calls; no issues observed.

---

## service/WebConfig.java
Purpose
- MVC configuration: registers `AdminInterceptor` for route protection.

Key details
- Adds `adminInterceptor` to:
  - `/admin/**` (excludes `/error/**`, `/login`, `/register`)
  - `/users/**` (excludes several login/register/error routes)

Issues / recommendations
- Interceptor-based protection duplicates security concerns already handled by Spring Security. Consider moving authorization logic to Spring Security (roles/authorities) and use `@PreAuthorize` or filter chains.
- Current exclusion lists should be reviewed for completeness.

---

## controller/AuthController.java
Purpose
- REST endpoints for authentication: register and login.

Endpoints
- POST `/api/auth/register`
  - Accepts `User` JSON, calls `userService.saveUser(user)`, returns 200 with plain text message.
- POST `/api/auth/login`
  - Accepts `User` JSON (email/password).
  - Looks up user by email and checks password using a freshly constructed `BCryptPasswordEncoder().matches(...)`.
  - On success generates a JWT via `JwtUtil.generateToken(email)` and returns plain text "JWT Token: <token>".
  - On failure returns 401 with "Invalid credentials".

Issues / recommendations
- Registration: must ensure password is hashed by `UserService.saveUser` before persisting. Currently that is missing.
- Login: uses a new local BCryptPasswordEncoder instead of the shared `PasswordEncoder` bean — inject the bean to avoid inconsistencies.
- Return structured JSON responses instead of plain text (e.g. `{ "token": "<jwt>" }`).
- Do not return or log raw passwords anywhere.
- Consider validating the incoming payload (non-null email/password) and return consistent error responses.

---

## General security notes / next steps
- Make password hashing consistent:
  - Hash in service layer on registration.
  - Use same `PasswordEncoder` bean for comparison on login.
- Externalize secrets:
  - Move JWT signing key to `application.properties` or an environment variable and load it in `JwtUtil`.
- Prefer Spring Security for auth/authorization (use authorities/roles) instead of session attributes + interceptors.
- Ensure seeded users in `data.sql` use BCrypt-hashed passwords if `login` expects hashed values.
- For dev vs prod behavior:
  - Use Spring profiles (e.g., `application-dev.properties`) to enable `permitAll()` only in dev.

---