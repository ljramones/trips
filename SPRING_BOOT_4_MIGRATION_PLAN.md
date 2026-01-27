# Spring Boot 4 Migration Plan (3.5.9 → 4.0.2)

## Overview

This document outlines the migration path from Spring Boot 3.5.9 to Spring Boot 4.0.2.

**Current State:**
- Spring Boot 3.5.9
- Hibernate 6.6.39
- Java 17
- Jakarta EE 10

**Target State:**
- Spring Boot 4.0.2
- Spring Framework 7.0.3
- Hibernate 7.x (via Spring Data 2025.1)
- Jakarta EE 11 / Servlet 6.1
- Java 17 (compatible) or Java 21 (recommended)

## Risk Assessment

| Area | Risk | Impact |
|------|------|--------|
| Jackson 3 migration | Medium | 14 files use Jackson - package names change |
| Starter POM changes | Low | Minor dependency updates |
| FxWeaver compatibility | Medium | Needs verification with Spring 7 |
| Groovy compatibility | Low | Used for scripting only, not Spock tests |
| Test framework | Low | No MockBean/SpyBean usage found |

## Phase 1: Pre-Migration Verification

### 1.1 Verify FxWeaver Compatibility
- Check if FxWeaver 2.0.1 is compatible with Spring Framework 7
- May need to update or find alternative if incompatible

### 1.2 Check Third-Party Dependencies
- Verify Orekit 11.1.2 works with Spring Boot 4
- Verify ControlsFX 11.2.1 compatibility
- Verify JGraphT 1.5.2 compatibility

## Phase 2: Core Migration

### 2.1 Update Spring Boot Version
```xml
<spring.boot.version>4.0.2</spring.boot.version>
```

### 2.2 Starter POM Changes

| Current | Spring Boot 4 |
|---------|---------------|
| `spring-boot-starter-aop` | `spring-boot-starter-aspectj` |
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` (if using MVC) |

**Note:** TRIPS uses `spring-boot-starter-webflux`, which remains unchanged.

### 2.3 Jakarta EE 11 Updates
- Update JAXB dependencies if needed:
  ```xml
  <jakarta.xml.bind-api.version>4.0.2</jakarta.xml.bind-api.version>
  <!-- May need upgrade for Jakarta EE 11 -->
  ```

## Phase 3: Jackson 3 Migration

Spring Boot 4 uses Jackson 3 with new package names.

### 3.1 Package Changes
```
com.fasterxml.jackson.* → tools.jackson.*
```
**Exception:** `jackson-annotations` retains `com.fasterxml.jackson`

### 3.2 Files Requiring Updates (14 files)
1. `DataSetDescriptorSerializationService.java`
2. `ProceduralPlanetPersistenceHelper.java`
3. `DataSetDescriptor.java`
4. `TransitRangeDef.java`
5. `Route.java`
6. `KuiperBelt.java`
7. `OortCloud.java`
8. `OrbitalParameters.java`
9. `SystemObject.java`
10. `AsteroidBelt.java` (model)
11. `Theme.java`
12. `CustomDataDefinition.java`
13. `CustomDataValue.java`
14. `JSONConfiguration.java`

### 3.3 Annotation Changes
- `@JsonComponent` → `@JacksonComponent`
- `@JsonMixin` → `@JacksonMixin`

### 3.4 Alternative: Jackson 2 Compatibility Layer
If immediate migration is not feasible, add:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-jackson2</artifactId>
</dependency>
```
Then configure via `spring.jackson2.*` properties. **This is deprecated and temporary.**

## Phase 4: Modularization Adjustments

### 4.1 Classic Starter Option (Intermediate Step)
If modularization causes issues, temporarily use:
```xml
<!-- Provides all modules like Spring Boot 3.x -->
<artifactId>spring-boot-starter-classic</artifactId>
<artifactId>spring-boot-starter-test-classic</artifactId>
```

### 4.2 New Module Structure
Spring Boot 4 has smaller, focused modules. May need additional starters if imports break.

## Phase 5: Configuration Updates

### 5.1 Property Changes
| Old Property | New Property |
|--------------|--------------|
| `spring.jackson.read.*` | `spring.jackson.json.read.*` |
| `spring.jackson.write.*` | `spring.jackson.json.write.*` |

### 5.2 DevTools
Live Reload is now disabled by default. Enable if needed:
```properties
spring.devtools.livereload.enabled=true
```

### 5.3 Health Probes
Liveness/readiness probes enabled by default. Disable if unwanted:
```properties
management.endpoint.health.probes.enabled=false
```

## Phase 6: JSpecify Null Safety (Optional)

Spring Boot 4 adds JSpecify annotations. If using null checkers:
- `org.springframework.lang.Nullable` → `org.jspecify.annotations.Nullable`

## Phase 7: Testing & Validation

### 7.1 Build Verification
```bash
./mvnw-java17.sh clean compile
```

### 7.2 Test Execution
```bash
./mvnw-java17.sh test
```

### 7.3 Application Startup
```bash
./mvnw-java17.sh spring-boot:run
```

### 7.4 Functional Testing
- Verify 3D visualization works
- Verify database operations work
- Verify data import/export works

## Phase 8: Java 21 Upgrade (Optional)

Spring Boot 4 recommends Java 21. Benefits:
- Virtual threads support
- Pattern matching improvements
- Better performance

If upgrading:
```xml
<java.version>21</java.version>
<maven.compiler.target>21</maven.compiler.target>
<maven.compiler.source>21</maven.compiler.source>
```

## Rollback Plan

If migration fails:
1. Revert `spring.boot.version` to `3.5.9`
2. Revert any Jackson package changes
3. Revert starter POM changes
4. Run `./mvnw-java17.sh clean install`

## Estimated Complexity

| Phase | Effort | Notes |
|-------|--------|-------|
| Phase 1 | Low | Verification only |
| Phase 2 | Low | Version bumps |
| Phase 3 | Medium | 14 files, mostly import changes |
| Phase 4 | Low | May not need changes |
| Phase 5 | Low | Configuration tweaks |
| Phase 6 | Optional | Only if using null checkers |
| Phase 7 | Medium | Testing time |
| Phase 8 | Optional | Java upgrade |

**Total Estimated Effort:** Lower than the 2.7→3.5 migration. The Jackson 3 package rename is the main work.

## References

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Boot 4.0.2 Release](https://spring.io/blog/2026/01/22/spring-boot-4-0-2-available-now)
