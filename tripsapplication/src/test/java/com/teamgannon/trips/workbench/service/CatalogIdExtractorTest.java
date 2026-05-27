package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins {@link CatalogIdExtractor} — the pure-function bag of ID parsing
 * + SIMBAD-name helpers extracted from {@link WorkbenchEnrichmentService}
 * in Phase 4.3. Issue 18.
 */
class CatalogIdExtractorTest {

    @Nested
    @DisplayName("extractSimbadCatalogId")
    class ExtractSimbadCatalogId {

        @Test
        @DisplayName("prefers a TYC entry over HD / HIP / others")
        void prefersTyc() {
            String input = "HD 12345|HIP 9876|TYC 1234-5-1";
            assertEquals("TYC 1234-5-1", CatalogIdExtractor.extractSimbadCatalogId(input));
        }

        @Test
        @DisplayName("falls back to HD / HIP / HR / BD / GJ / GL / LHS / 2MASS when no TYC")
        void fallsBackToSecondary() {
            assertEquals("HD 209458", CatalogIdExtractor.extractSimbadCatalogId("HD 209458|FOO 123"));
            assertEquals("HIP 12345", CatalogIdExtractor.extractSimbadCatalogId("HIP 12345"));
            assertEquals("GJ 581", CatalogIdExtractor.extractSimbadCatalogId("GJ 581"));
            assertEquals("2MASS J12345+6789", CatalogIdExtractor.extractSimbadCatalogId("2MASS J12345+6789"));
        }

        @Test
        @DisplayName("returns empty string for null/blank/non-matching input")
        void emptyForNoMatch() {
            assertEquals("", CatalogIdExtractor.extractSimbadCatalogId(null));
            assertEquals("", CatalogIdExtractor.extractSimbadCatalogId(""));
            assertEquals("", CatalogIdExtractor.extractSimbadCatalogId("WEIRD 12345|OTHER 7"));
        }
    }

    @Nested
    @DisplayName("extractGaiaSourceId")
    class ExtractGaiaSourceId {

        @Test
        @DisplayName("returns the longest digit sequence (not 'DR3')")
        void longestDigitSequence() {
            assertEquals("531415758077608192",
                    CatalogIdExtractor.extractGaiaSourceId("Gaia DR3 531415758077608192"));
            assertEquals("99999999999",
                    CatalogIdExtractor.extractGaiaSourceId("DR2 99999999999"));
        }

        @Test
        @DisplayName("empty for null / blank / no-digits")
        void emptyForNoDigits() {
            assertEquals("", CatalogIdExtractor.extractGaiaSourceId(null));
            assertEquals("", CatalogIdExtractor.extractGaiaSourceId(""));
            assertEquals("", CatalogIdExtractor.extractGaiaSourceId("no digits"));
        }
    }

    @Nested
    @DisplayName("extractHipId")
    class ExtractHipId {

        @Test
        @DisplayName("returns numeric portion of the HIP token")
        void numericFromHip() {
            assertEquals("12345", CatalogIdExtractor.extractHipId("HD 9|HIP 12345|TYC 1-1-1"));
            assertEquals("9876", CatalogIdExtractor.extractHipId("HIP 9876"));
        }

        @Test
        @DisplayName("empty when no HIP entry present")
        void emptyForNoHip() {
            assertEquals("", CatalogIdExtractor.extractHipId(null));
            assertEquals("", CatalogIdExtractor.extractHipId(""));
            assertEquals("", CatalogIdExtractor.extractHipId("HD 12345|TYC 1-1-1"));
        }
    }

    @Nested
    @DisplayName("extractNumericId")
    class ExtractNumericId {

        @Test
        @DisplayName("first digit sequence wins")
        void firstSequence() {
            assertEquals("42", CatalogIdExtractor.extractNumericId("HIP 42 bonus 99"));
            assertEquals("12345", CatalogIdExtractor.extractNumericId("12345"));
        }

        @Test
        @DisplayName("empty when null or no digits")
        void emptyForNoDigits() {
            assertEquals("", CatalogIdExtractor.extractNumericId(null));
            assertEquals("", CatalogIdExtractor.extractNumericId("no digits"));
        }
    }

    @Nested
    @DisplayName("normalizeSimbadKey + escapeAdqlString")
    class NormalizeAndEscape {

        @Test
        @DisplayName("collapses whitespace runs to single spaces")
        void collapsesWhitespace() {
            assertEquals("Alpha Centauri",
                    CatalogIdExtractor.normalizeSimbadKey("  Alpha   Centauri  "));
            assertEquals("HD 12345",
                    CatalogIdExtractor.normalizeSimbadKey("HD\t\t12345"));
        }

        @Test
        @DisplayName("empty / null normalize to empty string")
        void emptyForBlank() {
            assertEquals("", CatalogIdExtractor.normalizeSimbadKey(null));
            assertEquals("", CatalogIdExtractor.normalizeSimbadKey("   "));
        }

        @Test
        @DisplayName("escapeAdqlString doubles single quotes (SQL-92)")
        void doublesSingleQuotes() {
            assertEquals("Barnard''s Star", CatalogIdExtractor.escapeAdqlString("Barnard's Star"));
            assertEquals("plain", CatalogIdExtractor.escapeAdqlString("plain"));
            assertEquals("", CatalogIdExtractor.escapeAdqlString(null));
        }
    }

    @Nested
    @DisplayName("isNumericToken")
    class IsNumericToken {

        @Test
        @DisplayName("digits-only is true")
        void digitsOnly() {
            assertTrue(CatalogIdExtractor.isNumericToken("12345"));
            assertTrue(CatalogIdExtractor.isNumericToken("  9876  "));
        }

        @Test
        @DisplayName("anything else is false")
        void notDigitsOnly() {
            assertFalse(CatalogIdExtractor.isNumericToken(null));
            assertFalse(CatalogIdExtractor.isNumericToken(""));
            assertFalse(CatalogIdExtractor.isNumericToken("Vega"));
            assertFalse(CatalogIdExtractor.isNumericToken("HD 12345"));
            assertFalse(CatalogIdExtractor.isNumericToken("12.5"));
        }
    }

    @Nested
    @DisplayName("appendToken")
    class AppendToken {

        @Test
        @DisplayName("appends with separator when both sides have content")
        void appendsWithSeparator() {
            assertEquals("alpha|beta", CatalogIdExtractor.appendToken("alpha", "beta", "|"));
            assertEquals("a; b", CatalogIdExtractor.appendToken("a", "b", "; "));
        }

        @Test
        @DisplayName("skips when token is already a substring")
        void skipsDuplicate() {
            assertEquals("alpha|beta", CatalogIdExtractor.appendToken("alpha|beta", "beta", "|"));
        }

        @Test
        @DisplayName("returns token when current is blank")
        void replacesBlank() {
            assertEquals("beta", CatalogIdExtractor.appendToken(null, "beta", "|"));
            assertEquals("beta", CatalogIdExtractor.appendToken("", "beta", "|"));
        }

        @Test
        @DisplayName("returns current when token is blank")
        void keepsCurrentWhenTokenBlank() {
            assertEquals("alpha", CatalogIdExtractor.appendToken("alpha", null, "|"));
            assertEquals("alpha", CatalogIdExtractor.appendToken("alpha", "", "|"));
            assertEquals("", CatalogIdExtractor.appendToken(null, "", "|"));
        }
    }

    @Nested
    @DisplayName("getPreferredSimbadName")
    class GetPreferredSimbadName {

        @Test
        @DisplayName("prefers commonName when present and non-numeric")
        void prefersCommonName() {
            StarObject star = new StarObject();
            star.setCommonName("Vega");
            star.setDisplayName("Alpha Lyrae");
            assertEquals("Vega", CatalogIdExtractor.getPreferredSimbadName(star));
        }

        @Test
        @DisplayName("falls back to displayName when commonName is numeric or 'NA'")
        void fallsBackToDisplayName() {
            StarObject star = new StarObject();
            star.setCommonName("12345");
            star.setDisplayName("HD 12345");
            assertEquals("HD 12345", CatalogIdExtractor.getPreferredSimbadName(star));

            StarObject star2 = new StarObject();
            star2.setCommonName("NA");
            star2.setDisplayName("Sirius");
            assertEquals("Sirius", CatalogIdExtractor.getPreferredSimbadName(star2));
        }

        @Test
        @DisplayName("falls back to catalog id when both names are numeric or blank")
        void fallsBackToCatalogId() {
            StarObject star = new StarObject();
            star.setCommonName("");
            star.setDisplayName("99999");
            star.setCatalogIdList("HD 99999|TYC 1-1-1");
            assertEquals("TYC 1-1-1", CatalogIdExtractor.getPreferredSimbadName(star));
        }
    }
}
