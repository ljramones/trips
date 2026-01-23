package com.teamgannon.trips.tableviews;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarTableCsvExporter.
 */
class StarTableCsvExporterTest {

    @Nested
    @DisplayName("CSV cell formatting - String tests")
    class CsvCellStringTests {

        @Test
        @DisplayName("should return empty string for null")
        void shouldReturnEmptyStringForNull() {
            String result = StarTableCsvExporter.csvCell((String) null);
            assertEquals("", result);
        }

        @Test
        @DisplayName("should return value unchanged when no special characters")
        void shouldReturnValueUnchangedWhenNoSpecialCharacters() {
            String result = StarTableCsvExporter.csvCell("Simple Text");
            assertEquals("Simple Text", result);
        }

        @Test
        @DisplayName("should quote value containing comma")
        void shouldQuoteValueContainingComma() {
            String result = StarTableCsvExporter.csvCell("Value, with comma");
            assertEquals("\"Value, with comma\"", result);
        }

        @Test
        @DisplayName("should quote value containing newline")
        void shouldQuoteValueContainingNewline() {
            String result = StarTableCsvExporter.csvCell("Line1\nLine2");
            assertEquals("\"Line1\nLine2\"", result);
        }

        @Test
        @DisplayName("should escape and quote value containing quotes")
        void shouldEscapeAndQuoteValueContainingQuotes() {
            String result = StarTableCsvExporter.csvCell("He said \"Hello\"");
            assertEquals("\"He said \"\"Hello\"\"\"", result);
        }

        @Test
        @DisplayName("should handle multiple special characters")
        void shouldHandleMultipleSpecialCharacters() {
            String result = StarTableCsvExporter.csvCell("A,B\n\"C\"");
            assertEquals("\"A,B\n\"\"C\"\"\"", result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "123", "Star Name", "Alpha Centauri"})
        @DisplayName("should not quote simple values")
        void shouldNotQuoteSimpleValues(String value) {
            String result = StarTableCsvExporter.csvCell(value);
            assertFalse(result.startsWith("\""));
            assertEquals(value, result);
        }
    }

    @Nested
    @DisplayName("CSV cell formatting - Double tests")
    class CsvCellDoubleTests {

        @Test
        @DisplayName("should return empty string for null Double")
        void shouldReturnEmptyStringForNullDouble() {
            String result = StarTableCsvExporter.csvCell((Double) null);
            assertEquals("", result);
        }

        @Test
        @DisplayName("should format positive double")
        void shouldFormatPositiveDouble() {
            String result = StarTableCsvExporter.csvCell(123.456);
            assertEquals("123.456", result);
        }

        @Test
        @DisplayName("should format negative double")
        void shouldFormatNegativeDouble() {
            String result = StarTableCsvExporter.csvCell(-42.5);
            assertEquals("-42.5", result);
        }

        @Test
        @DisplayName("should format zero")
        void shouldFormatZero() {
            String result = StarTableCsvExporter.csvCell(0.0);
            assertEquals("0.0", result);
        }

        @Test
        @DisplayName("should format very small double")
        void shouldFormatVerySmallDouble() {
            String result = StarTableCsvExporter.csvCell(0.00001);
            assertEquals("1.0E-5", result);
        }

        @Test
        @DisplayName("should format very large double")
        void shouldFormatVeryLargeDouble() {
            String result = StarTableCsvExporter.csvCell(1234567890.123);
            // Java formats large doubles in scientific notation: 1.234567890123E9
            assertTrue(result.contains("E9") || result.contains("1234567890"),
                    "Expected scientific notation or full number, got: " + result);
        }
    }

    @Nested
    @DisplayName("CSV cell formatting - Boolean tests")
    class CsvCellBooleanTests {

        @Test
        @DisplayName("should return empty string for null Boolean")
        void shouldReturnEmptyStringForNullBoolean() {
            String result = StarTableCsvExporter.csvCell((Boolean) null);
            assertEquals("", result);
        }

        @Test
        @DisplayName("should format true")
        void shouldFormatTrue() {
            String result = StarTableCsvExporter.csvCell(true);
            assertEquals("true", result);
        }

        @Test
        @DisplayName("should format false")
        void shouldFormatFalse() {
            String result = StarTableCsvExporter.csvCell(false);
            assertEquals("false", result);
        }
    }

    @Nested
    @DisplayName("GetHeaders tests")
    class GetHeadersTests {

        @Test
        @DisplayName("should return non-empty headers array")
        void shouldReturnNonEmptyHeadersArray() {
            String[] headers = StarTableCsvExporter.getHeaders();

            assertNotNull(headers);
            assertTrue(headers.length > 0);
        }

        @Test
        @DisplayName("should return copy of headers")
        void shouldReturnCopyOfHeaders() {
            String[] headers1 = StarTableCsvExporter.getHeaders();
            String[] headers2 = StarTableCsvExporter.getHeaders();

            assertNotSame(headers1, headers2);
        }

        @Test
        @DisplayName("should include expected headers")
        void shouldIncludeExpectedHeaders() {
            String[] headers = StarTableCsvExporter.getHeaders();

            assertTrue(containsHeader(headers, "Display Name"));
            assertTrue(containsHeader(headers, "Distance (LY)"));
            assertTrue(containsHeader(headers, "Spectra"));
            assertTrue(containsHeader(headers, "RA"));
            assertTrue(containsHeader(headers, "Declination"));
            assertTrue(containsHeader(headers, "X"));
            assertTrue(containsHeader(headers, "Y"));
            assertTrue(containsHeader(headers, "Z"));
        }

        @Test
        @DisplayName("should have 14 headers")
        void shouldHave14Headers() {
            String[] headers = StarTableCsvExporter.getHeaders();
            assertEquals(14, headers.length);
        }

        private boolean containsHeader(String[] headers, String header) {
            for (String h : headers) {
                if (h.equals(header)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty string")
        void shouldHandleEmptyString() {
            String result = StarTableCsvExporter.csvCell("");
            assertEquals("", result);
        }

        @Test
        @DisplayName("should handle string with only spaces")
        void shouldHandleStringWithOnlySpaces() {
            String result = StarTableCsvExporter.csvCell("   ");
            assertEquals("   ", result);
        }

        @Test
        @DisplayName("should handle string with tabs")
        void shouldHandleStringWithTabs() {
            String result = StarTableCsvExporter.csvCell("Tab\there");
            assertEquals("Tab\there", result);
        }

        @Test
        @DisplayName("should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            String result = StarTableCsvExporter.csvCell("Star α Centauri ☆");
            assertEquals("Star α Centauri ☆", result);
        }

        @Test
        @DisplayName("should handle scientific notation in string")
        void shouldHandleScientificNotationInString() {
            String result = StarTableCsvExporter.csvCell("1.5E10");
            assertEquals("1.5E10", result);
        }
    }

    @Nested
    @DisplayName("WriteHeader tests")
    class WriteHeaderTests {

        @Test
        @DisplayName("should write all headers as CSV line")
        void shouldWriteAllHeadersAsCsvLine() throws IOException {
            // Create exporter with null service/query (we're only testing writeHeader)
            StarTableCsvExporter exporter = new StarTableCsvExporter(null, null, "TestDataset");
            StringWriter stringWriter = new StringWriter();
            BufferedWriter writer = new BufferedWriter(stringWriter);

            exporter.writeHeader(writer);
            writer.flush();

            String result = stringWriter.toString();
            assertTrue(result.contains("Display Name"));
            assertTrue(result.contains("Distance (LY)"));
            assertTrue(result.contains(","));
            assertTrue(result.endsWith("\n") || result.endsWith("\r\n"));
        }
    }

    @Nested
    @DisplayName("WriteLine tests")
    class WriteLineTests {

        @Test
        @DisplayName("should write record as CSV line")
        void shouldWriteRecordAsCsvLine() throws IOException {
            StarTableCsvExporter exporter = new StarTableCsvExporter(null, null, "TestDataset");
            StringWriter stringWriter = new StringWriter();
            BufferedWriter writer = new BufferedWriter(stringWriter);

            StarEditRecord record = createTestRecord();

            exporter.writeLine(writer, record);
            writer.flush();

            String result = stringWriter.toString();
            assertTrue(result.contains("Test Star"));
            assertTrue(result.contains("10.5")); // distance
            assertTrue(result.contains("G2V")); // spectra
            assertTrue(result.endsWith("\n") || result.endsWith("\r\n"));
        }

        @Test
        @DisplayName("should handle record with null values")
        void shouldHandleRecordWithNullValues() throws IOException {
            StarTableCsvExporter exporter = new StarTableCsvExporter(null, null, "TestDataset");
            StringWriter stringWriter = new StringWriter();
            BufferedWriter writer = new BufferedWriter(stringWriter);

            StarEditRecord record = new StarEditRecord();
            record.setDisplayName("Minimal Star");
            // All other fields are null

            exporter.writeLine(writer, record);
            writer.flush();

            String result = stringWriter.toString();
            assertTrue(result.contains("Minimal Star"));
            // Should have commas for null fields
            assertTrue(result.contains(",,"));
        }

        private StarEditRecord createTestRecord() {
            StarEditRecord record = new StarEditRecord();
            record.setDisplayName("Test Star");
            record.setDistanceToEarth(10.5);
            record.setSpectra("G2V");
            record.setRadius(1.0);
            record.setMass(1.0);
            record.setLuminosity("1.0");
            record.setRa(180.0);
            record.setDeclination(45.0);
            record.setParallax(100.0);
            record.setXCoord(5.0);
            record.setYCoord(6.0);
            record.setZCoord(7.0);
            record.setReal(true);
            record.setComment("Test comment");
            return record;
        }
    }
}
