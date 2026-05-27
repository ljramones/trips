package com.teamgannon.trips.service.importservices;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for {@link ImportResult} (Issue 47 — Phase 7.9
 * follow-on, the importservices test sweep).
 * <p>
 * Note: a follow-up grep showed `ImportResult` has no live callers in
 * src/main — all references to "ImportResult" elsewhere are nested types
 * on dialogs / other services. The class may be dead code awaiting an
 * import-pipeline refactor; until then these tests pin its current
 * shape so it can be safely deleted or merged later.
 */
@DisplayName("ImportResult")
class ImportResultTest {

    @Nested
    @DisplayName("Lombok @Data + @Builder defaults")
    class Defaults {

        @Test
        @DisplayName("no-arg builder yields success=false, message=null")
        void builderDefaults() {
            ImportResult r = ImportResult.builder().build();
            assertFalse(r.isSuccess());
            assertNull(r.getMessage());
        }

        @Test
        @DisplayName("success=true is honoured")
        void successTrue() {
            ImportResult r = ImportResult.builder().success(true).build();
            assertTrue(r.isSuccess());
        }

        @Test
        @DisplayName("message is preserved verbatim")
        void messagePreserved() {
            ImportResult r = ImportResult.builder()
                    .message("loaded 1234 stars")
                    .build();
            assertEquals("loaded 1234 stars", r.getMessage());
        }
    }

    @Nested
    @DisplayName("equality + hashCode (Lombok @Data)")
    class Equality {

        @Test
        @DisplayName("two builds with identical fields are equal")
        void identicalFieldsEqual() {
            ImportResult a = ImportResult.builder().success(true).message("ok").build();
            ImportResult b = ImportResult.builder().success(true).message("ok").build();
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("differing success flag breaks equality")
        void successFlagBreaksEquality() {
            ImportResult ok = ImportResult.builder().success(true).message("m").build();
            ImportResult ng = ImportResult.builder().success(false).message("m").build();
            assertNotEquals(ok, ng);
        }

        @Test
        @DisplayName("differing message breaks equality")
        void messageBreaksEquality() {
            ImportResult a = ImportResult.builder().success(true).message("m1").build();
            ImportResult b = ImportResult.builder().success(true).message("m2").build();
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("null message vs empty message are distinct")
        void nullVsEmptyMessage() {
            ImportResult withNull = ImportResult.builder().success(true).build();
            ImportResult withEmpty = ImportResult.builder().success(true).message("").build();
            assertNotEquals(withNull, withEmpty);
        }
    }

    @Nested
    @DisplayName("mutability via setters (@Data)")
    class Mutability {

        @Test
        @DisplayName("setSuccess + setMessage post-construction work")
        void settersWork() {
            ImportResult r = ImportResult.builder().build();
            r.setSuccess(true);
            r.setMessage("now set");
            assertTrue(r.isSuccess());
            assertEquals("now set", r.getMessage());
        }
    }

    @Nested
    @DisplayName("toString")
    class StringForm {

        @Test
        @DisplayName("includes both fields")
        void toStringIncludesFields() {
            ImportResult r = ImportResult.builder()
                    .success(true)
                    .message("ok")
                    .build();
            String s = r.toString();
            assertTrue(s.contains("success=true"));
            assertTrue(s.contains("message=ok"));
        }
    }
}
