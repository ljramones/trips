# Spring Boot 3 Migration Plan

## TRIPS Application: Spring Boot 2.7.4 → 3.2.x

**Document Version:** 1.0
**Created:** 2026-01-27
**Target Spring Boot Version:** 3.2.x (LTS recommended)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Pre-Migration Checklist](#pre-migration-checklist)
3. [Phase 1: Preparation](#phase-1-preparation)
4. [Phase 2: Dependency Updates](#phase-2-dependency-updates)
5. [Phase 3: Code Migration](#phase-3-code-migration)
6. [Phase 4: Plugin & Build Updates](#phase-4-plugin--build-updates)
7. [Phase 5: Testing & Validation](#phase-5-testing--validation)
8. [Phase 6: Post-Migration Cleanup](#phase-6-post-migration-cleanup)
9. [Rollback Plan](#rollback-plan)
10. [Risk Assessment](#risk-assessment)

---

## Executive Summary

### Scope
- **Files requiring code changes:** ~24 Java files
- **JPA entity files:** 13 files with 137 annotation usages
- **Import statement changes:** ~66 imports across codebase
- **Plugin replacements:** 2 (JAXB, Spring Boot)

### Key Breaking Changes
1. `javax.*` → `jakarta.*` namespace migration
2. Hibernate 5 → Hibernate 6
3. JAXB 2.x → JAXB 4.x (Jakarta)
4. Spring Security changes (if applicable)
5. Property name changes

### Estimated Effort
- **Automated migration:** 60-70% of changes
- **Manual review/fixes:** 30-40% of changes
- **Testing:** Significant (all JPA operations, FxWeaver integration)

---

## Pre-Migration Checklist

### Before Starting
- [ ] Create a new git branch: `feature/spring-boot-3-migration`
- [ ] Ensure all tests pass on current version
- [ ] Back up the database (`./data/tripsdb.*`)
- [ ] Document current application behavior for comparison
- [ ] Review Spring Boot 3.0, 3.1, and 3.2 release notes

### Environment Requirements
- [ ] Java 17 installed (already satisfied)
- [ ] Maven 3.8+ installed
- [ ] IDE updated with Jakarta EE support

---

## Phase 1: Preparation

### 1.1 Create Migration Branch

```bash
git checkout develop
git pull origin develop
git checkout -b feature/spring-boot-3-migration
```

### 1.2 Verify Current Build

```bash
./mvnw-java17.sh clean install
```

Ensure all tests pass before proceeding.

### 1.3 Add OpenRewrite Plugin (Optional but Recommended)

Add to `tripsapplication/pom.xml` temporarily for automated migration:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.23.1</version>
    <configuration>
        <activeRecipes>
            <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-spring</artifactId>
            <version>5.6.0</version>
        </dependency>
    </dependencies>
</plugin>
```

Run the migration:
```bash
./mvnw-java17.sh rewrite:run
```

Review changes, then remove the plugin.

---

## Phase 2: Dependency Updates

### 2.1 Update Properties in `tripsapplication/pom.xml`

```xml
<properties>
    <!-- Core Spring Boot -->
    <spring.boot.version>3.2.2</spring.boot.version>

    <!-- FxWeaver - verify compatibility -->
    <fxweaver.version>2.0.1</fxweaver.version>

    <!-- Update these dependencies -->
    <jackson-databind.version>2.16.1</jackson-databind.version>
    <controlsfx.version>11.2.1</controlsfx.version>
    <ikonli.version>12.3.1</ikonli.version>
    <testcontainers.version>1.19.3</testcontainers.version>
    <orekit.version>12.1</orekit.version>

    <!-- Remove - replaced by Jakarta -->
    <!-- <javax-validation.version>2.0.1.Final</javax-validation.version> -->
</properties>
```

### 2.2 Replace javax.validation Dependency

**Remove:**
```xml
<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>validation-api</artifactId>
    <version>${javax-validation.version}</version>
</dependency>
```

**Add:**
```xml
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>3.0.2</version>
</dependency>
```

### 2.3 Add Jakarta XML Bind (JAXB) Dependencies

**Add for JAXB support:**
```xml
<!-- Jakarta JAXB API -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.1</version>
</dependency>

<!-- JAXB Runtime -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>4.0.4</version>
    <scope>runtime</scope>
</dependency>
```

### 2.4 Remove Outdated Dependencies

**Remove commons-lang (ancient version):**
```xml
<!-- REMOVE THIS -->
<dependency>
    <groupId>commons-lang</groupId>
    <artifactId>commons-lang</artifactId>
    <version>20030203.000129</version>
</dependency>
```

You already have `commons-lang3:3.18.0` which is the modern replacement.

### 2.5 Update Dependency Management BOMs

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.kordamp.ikonli</groupId>
            <artifactId>ikonli-bom</artifactId>
            <version>12.3.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>5.10.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring.boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2.6 Full Dependency Compatibility Matrix

| Dependency | Current | Target | Notes |
|------------|---------|--------|-------|
| spring-boot | 2.7.4 | 3.2.2 | Core upgrade |
| h2 | 2.2.224 | 2.2.224 | Compatible |
| lombok | 1.18.34 | 1.18.34 | Compatible |
| javafx | 21.0.5 | 21.0.5 | Compatible |
| fxweaver | 2.0.1 | 2.0.1 | Verify Boot 3 |
| jackson | 2.14.0-rc1 | 2.16.1 | Update |
| jgrapht | 1.5.1 | 1.5.2 | Optional update |
| groovy | 4.0.4 | 4.0.17 | Optional update |
| orekit | 11.1.2 | 12.1 | Check API changes |
| controlsfx | 11.1.1 | 11.2.1 | Update |
| guava | 33.0.0-jre | 33.0.0-jre | Compatible |
| hipparchus | 3.0 | 3.0 | Compatible |
| testcontainers | 1.17.6 | 1.19.3 | Update |

---

## Phase 3: Code Migration

### 3.1 JPA Entities (13 files)

**Files to update:**
```
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/
├── StarObject.java
├── ExoPlanet.java
├── DataSetDescriptor.java
├── SolarSystem.java
├── AsteroidBelt.java
├── TripsPrefs.java
├── TransitSettings.java
├── StarDetailsPersist.java
├── GraphEnablesPersist.java
├── GraphColorsPersist.java
├── CivilizationDisplayPreferences.java
├── StarCatalogIds.java
└── StarWorldBuilding.java

tripsapplication/src/main/java/com/teamgannon/trips/dataset/model/
└── FontDescriptor.java

tripsapplication/src/main/java/com/teamgannon/trips/graphics/entities/
└── RouteDescriptor.java
```

**Search and replace imports:**
```
javax.persistence.* → jakarta.persistence.*
```

**Example transformation:**
```java
// BEFORE
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Table;

// AFTER
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
```

For wildcard imports:
```java
// BEFORE
import javax.persistence.*;

// AFTER
import jakarta.persistence.*;
```

### 3.2 Validation Annotations (3 files)

**Files to update:**
```
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/DataSetDescriptor.java
tripsapplication/src/main/java/com/teamgannon/trips/dialogs/inventory/ComputerInventoryDialog.java
```

**Search and replace:**
```
javax.validation.constraints.NotNull → jakarta.validation.constraints.NotNull
```

### 3.3 Repository/Service Layer (4 files)

**Files to update:**
```
tripsapplication/src/main/java/com/teamgannon/trips/jpa/repository/impl/StarObjectRepositoryImpl.java
tripsapplication/src/main/java/com/teamgannon/trips/service/StarService.java
tripsapplication/src/main/java/com/teamgannon/trips/service/DatabaseManagementService.java
tripsapplication/src/test/java/com/teamgannon/trips/jpa/repository/BaseRepositoryIntegrationTest.java
```

**Search and replace:**
```
javax.persistence.EntityManager → jakarta.persistence.EntityManager
javax.persistence.PersistenceContext → jakarta.persistence.PersistenceContext
javax.persistence.Query → jakarta.persistence.Query
javax.persistence.TypedQuery → jakarta.persistence.TypedQuery
javax.persistence.criteria.* → jakarta.persistence.criteria.*
```

### 3.4 @PostConstruct Annotations (6 files)

**Files to update:**
```
tripsapplication/src/main/java/com/teamgannon/trips/service/SolPlanetsInitializer.java
tripsapplication/src/main/java/com/teamgannon/trips/constellation/ConstellationLoader.java
tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/sol/SolSolarSystem.java
tripsapplication/src/main/java/com/teamgannon/trips/planetarymodelling/chemical/MolecularWeightCalculator.java
tripsapplication/src/main/java/com/teamgannon/trips/measure/MetricManagement.java
tripsapplication/src/main/java/com/teamgannon/trips/measure/OshiMeasure.java
```

**Search and replace:**
```
javax.annotation.PostConstruct → jakarta.annotation.PostConstruct
```

### 3.5 JAXB Code (2 files)

**Files to update:**
```
tripsapplication/src/main/java/com/teamgannon/trips/utility/SesameResolver.java
tripsapplication/src/main/java/com/teamgannon/trips/dialogs/sesame/SesameNameResolverDialog.java
```

**Search and replace:**
```
javax.xml.bind.JAXBContext → jakarta.xml.bind.JAXBContext
javax.xml.bind.JAXBElement → jakarta.xml.bind.JAXBElement
javax.xml.bind.JAXBException → jakarta.xml.bind.JAXBException
javax.xml.bind.Unmarshaller → jakarta.xml.bind.Unmarshaller
```

### 3.6 Automated Migration Script

Create a shell script for bulk replacement:

```bash
#!/bin/bash
# migrate-jakarta.sh

JAVA_DIR="tripsapplication/src/main/java"
TEST_DIR="tripsapplication/src/test/java"

# Function to replace imports
replace_imports() {
    local dir=$1

    # javax.persistence → jakarta.persistence
    find "$dir" -name "*.java" -exec sed -i '' 's/import javax\.persistence/import jakarta.persistence/g' {} +

    # javax.validation → jakarta.validation
    find "$dir" -name "*.java" -exec sed -i '' 's/import javax\.validation/import jakarta.validation/g' {} +

    # javax.annotation.PostConstruct → jakarta.annotation.PostConstruct
    find "$dir" -name "*.java" -exec sed -i '' 's/import javax\.annotation\.PostConstruct/import jakarta.annotation.PostConstruct/g' {} +

    # javax.xml.bind → jakarta.xml.bind
    find "$dir" -name "*.java" -exec sed -i '' 's/import javax\.xml\.bind/import jakarta.xml.bind/g' {} +
}

echo "Migrating main sources..."
replace_imports "$JAVA_DIR"

echo "Migrating test sources..."
replace_imports "$TEST_DIR"

echo "Migration complete. Please review changes."
```

---

## Phase 4: Plugin & Build Updates

### 4.1 Replace JAXB Maven Plugin

**Remove from `tripsapplication/pom.xml`:**
```xml
<!-- REMOVE THIS -->
<plugin>
    <groupId>org.jvnet.jaxb2.maven2</groupId>
    <artifactId>maven-jaxb2-plugin</artifactId>
    <version>0.14.0</version>
    ...
</plugin>
```

**Add Jakarta-compatible JAXB plugin:**
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>xjc</id>
            <goals>
                <goal>xjc</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <sources>
            <source>src/main/resources/xsd</source>
        </sources>
        <packageName>com.teamgannon.trips.generated</packageName>
    </configuration>
</plugin>
```

### 4.2 Update Spring Boot Maven Plugin

Already managed by version property, but verify configuration:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${spring.boot.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 4.3 Update Maven Compiler Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.12.1</version>
    <configuration>
        <release>17</release>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### 4.4 Update jpackage Profile (if using)

The `jpackage-maven-plugin` should continue to work, but update the main class if Spring Boot's launcher changed:

```xml
<!-- Spring Boot 3 uses different launcher -->
<mainClass>org.springframework.boot.loader.launch.JarLauncher</mainClass>
```

---

## Phase 5: Testing & Validation

### 5.1 Compilation Check

```bash
./mvnw-java17.sh clean compile
```

**Expected issues to resolve:**
- Import errors (should be fixed by Phase 3)
- JAXB generated class errors (fixed by Phase 4.1)

### 5.2 Unit Test Execution

```bash
./mvnw-java17.sh test
```

**Focus areas:**
- JPA repository tests
- Service layer tests
- Any tests using `@PostConstruct`

### 5.3 Integration Test Execution

```bash
./mvnw-java17.sh verify
```

**Focus areas:**
- Database connectivity
- Entity persistence
- Query execution (especially Criteria API)

### 5.4 Manual Testing Checklist

#### Database Operations
- [ ] Application starts successfully
- [ ] Existing database loads correctly
- [ ] New star records can be created
- [ ] Star queries return expected results
- [ ] Dataset import works
- [ ] Dataset export works

#### JavaFX/FxWeaver Integration
- [ ] Main window displays
- [ ] All dialogs open correctly
- [ ] Controllers receive injected dependencies
- [ ] Event publishing/listening works
- [ ] 3D visualization renders

#### Solar System Features
- [ ] "Jump Into" displays solar system
- [ ] Planet rendering works
- [ ] Orbit visualization correct
- [ ] Context menus functional

#### SESAME Resolver (JAXB)
- [ ] SESAME name resolution works
- [ ] XML parsing functions correctly

### 5.5 Hibernate 6 Behavioral Changes to Test

Hibernate 6 has some behavioral differences:

1. **Implicit type coercion** - More strict about types
2. **HQL changes** - Some syntax differences
3. **ID generation** - Verify `@GeneratedValue` strategies work
4. **Lazy loading** - May behave differently in some edge cases

**Test these queries specifically:**
```java
// In StarObjectRepositoryImpl - Criteria API queries
// In StarService - Native queries
// In DataSetDescriptorRepository - JPQL queries
```

### 5.6 Performance Baseline

Run before and after migration:

```bash
# Time the application startup
time ./mvnw-java17.sh spring-boot:run

# Monitor memory usage during typical operations
```

---

## Phase 6: Post-Migration Cleanup

### 6.1 Remove Migration Tools

Remove OpenRewrite plugin if added:

```xml
<!-- REMOVE after migration -->
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    ...
</plugin>
```

### 6.2 Update Documentation

- [ ] Update `CLAUDE.md` with new Spring Boot version
- [ ] Update `README.md` if it mentions Spring Boot version
- [ ] Document any behavioral changes discovered

### 6.3 Clean Up Deprecated Usage

Spring Boot 3 removes many deprecations. Search for:

```bash
# Find any remaining deprecated usage
grep -r "@Deprecated" --include="*.java" tripsapplication/src/
```

### 6.4 Update application.yml

Check for property name changes:

```yaml
# Some properties renamed in Spring Boot 3
# Example: spring.redis.* → spring.data.redis.*

# Verify H2 console configuration
spring:
  h2:
    console:
      enabled: true  # May need explicit enabling
```

### 6.5 Commit and PR

```bash
git add .
git commit -m "Upgrade to Spring Boot 3.2.2

- Migrate javax.* to jakarta.* namespace (24 files)
- Update JAXB plugin to Jakarta-compatible version
- Update dependencies for Spring Boot 3 compatibility
- Verify FxWeaver integration works with Boot 3

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"

git push -u origin feature/spring-boot-3-migration
```

---

## Rollback Plan

### If Migration Fails

1. **Immediate rollback:**
   ```bash
   git checkout develop
   ```

2. **Database recovery (if corrupted):**
   ```bash
   # Restore from backup
   rm -rf ./data/tripsdb.*
   cp -r ./data-backup/tripsdb.* ./data/
   ```

3. **Partial rollback** (keep some dependency updates):
   - Revert `spring.boot.version` to 2.7.4
   - Revert `javax` → `jakarta` changes
   - Keep updated library versions that are backward compatible

### Checkpoint Commits

Create commits at each phase to enable partial rollback:

```bash
# After Phase 2
git commit -m "chore: update dependencies for Spring Boot 3 prep"

# After Phase 3
git commit -m "refactor: migrate javax to jakarta namespace"

# After Phase 4
git commit -m "build: update plugins for Spring Boot 3"

# After Phase 5
git commit -m "test: verify Spring Boot 3 migration"
```

---

## Risk Assessment

### High Risk Areas

| Area | Risk | Mitigation |
|------|------|------------|
| FxWeaver Integration | Library may have undocumented Boot 3 issues | Test early, have fallback plan |
| Hibernate 6 Queries | Criteria API behavior changes | Comprehensive query testing |
| JAXB Generation | Generated classes may differ | Regenerate and compare |
| Database Schema | Hibernate may generate different DDL | Test with fresh and existing DB |

### Medium Risk Areas

| Area | Risk | Mitigation |
|------|------|------------|
| Orekit 12.x | API changes from 11.x | Review changelog, update calls |
| @PostConstruct timing | May execute at different point | Verify initialization order |
| Property binding | Stricter validation | Test all configuration |

### Low Risk Areas

| Area | Risk | Mitigation |
|------|------|------------|
| JavaFX | No Spring Boot coupling | Should work unchanged |
| JGraphT | Pure Java library | Should work unchanged |
| Apache Commons | Standalone libraries | Should work unchanged |

---

## Appendix A: Complete File List

### Files Requiring javax → jakarta Migration

```
# JPA Entities (15 files)
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarObject.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/ExoPlanet.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/DataSetDescriptor.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/SolarSystem.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/AsteroidBelt.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/TripsPrefs.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/TransitSettings.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarDetailsPersist.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/GraphEnablesPersist.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/GraphColorsPersist.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/CivilizationDisplayPreferences.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarCatalogIds.java
tripsapplication/src/main/java/com/teamgannon/trips/jpa/model/StarWorldBuilding.java
tripsapplication/src/main/java/com/teamgannon/trips/dataset/model/FontDescriptor.java
tripsapplication/src/main/java/com/teamgannon/trips/graphics/entities/RouteDescriptor.java

# Repository/Service (4 files)
tripsapplication/src/main/java/com/teamgannon/trips/jpa/repository/impl/StarObjectRepositoryImpl.java
tripsapplication/src/main/java/com/teamgannon/trips/service/StarService.java
tripsapplication/src/main/java/com/teamgannon/trips/service/DatabaseManagementService.java
tripsapplication/src/test/java/com/teamgannon/trips/jpa/repository/BaseRepositoryIntegrationTest.java

# @PostConstruct (6 files)
tripsapplication/src/main/java/com/teamgannon/trips/service/SolPlanetsInitializer.java
tripsapplication/src/main/java/com/teamgannon/trips/constellation/ConstellationLoader.java
tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/sol/SolSolarSystem.java
tripsapplication/src/main/java/com/teamgannon/trips/planetarymodelling/chemical/MolecularWeightCalculator.java
tripsapplication/src/main/java/com/teamgannon/trips/measure/MetricManagement.java
tripsapplication/src/main/java/com/teamgannon/trips/measure/OshiMeasure.java

# JAXB (2 files)
tripsapplication/src/main/java/com/teamgannon/trips/utility/SesameResolver.java
tripsapplication/src/main/java/com/teamgannon/trips/dialogs/sesame/SesameNameResolverDialog.java

# Validation (1 additional file - others overlap with JPA)
tripsapplication/src/main/java/com/teamgannon/trips/dialogs/inventory/ComputerInventoryDialog.java
```

---

## Appendix B: Useful Commands

```bash
# Find all remaining javax imports (after migration)
grep -r "import javax\." --include="*.java" tripsapplication/src/

# Verify no javax.persistence remains
grep -r "javax\.persistence" --include="*.java" tripsapplication/src/

# Check for deprecated APIs
grep -r "@Deprecated" --include="*.java" tripsapplication/src/

# Run specific test class
./mvnw-java17.sh test -Dtest=StarObjectRepositoryTest

# Run with debug logging
./mvnw-java17.sh spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.hibernate=DEBUG"

# Generate dependency tree
./mvnw-java17.sh dependency:tree > dependency-tree.txt
```

---

## Appendix C: Reference Links

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Boot 3.2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)
- [Hibernate 6 Migration Guide](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc)
- [Jakarta EE 9 Migration](https://jakarta.ee/resources/jakarta-ee-9-migration-guide/)
- [OpenRewrite Spring Boot Migration](https://docs.openrewrite.org/recipes/java/spring/boot3/upgradespringboot_3_2)
