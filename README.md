# aap-fss-proxy
Proxy for team aap for å gjøre kall mot tjenester i FSS

## Technology Stack
- Kotlin with Ktor framework
- Gradle build system
- JDK 17

## Building
```bash
./gradlew build
```

## Running
```bash
./gradlew run
```

## Endpoints
- `/internal/isalive` - Liveness check
- `/internal/isready` - Readiness check
- `/internal/prometheus` - Metrics endpoint
- `/arena/*` - Arena service endpoints
- `/inntektskomponent/*` - Inntektskomponent service endpoints
