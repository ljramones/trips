package com.teamgannon.trips.file.csvin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the bad-row collector behaviour added for Issue 38: rejects are still
 * counted past the sample cap, the sample list is immutable to callers, and
 * regular accept/total counting is unaffected.
 */
class RegCSVFileTest {

    @Nested
    @DisplayName("recordBadRow")
    class RecordBadRow {

        @Test
        @DisplayName("samples up to the cap and counts every reject")
        void samplesUpToCapAndCountsEveryReject() {
            RegCSVFile file = new RegCSVFile();
            int over = RegCSVFile.MAX_BAD_ROW_SAMPLES + 5;

            for (int i = 0; i < over; i++) {
                file.recordBadRow("row " + i + ": NumberFormatException: bad");
            }

            assertEquals(over, file.getNumbRejects(),
                    "every bad row should increment the reject counter");
            assertEquals(RegCSVFile.MAX_BAD_ROW_SAMPLES, file.getBadRowSamples().size(),
                    "sample list should cap at MAX_BAD_ROW_SAMPLES");
            assertEquals("row 0: NumberFormatException: bad", file.getBadRowSamples().get(0),
                    "earliest samples are retained");
        }

        @Test
        @DisplayName("returns an unmodifiable view")
        void returnsUnmodifiableView() {
            RegCSVFile file = new RegCSVFile();
            file.recordBadRow("row 1: oops");

            assertThrows(UnsupportedOperationException.class,
                    () -> file.getBadRowSamples().add("forbidden"));
        }
    }

    @Nested
    @DisplayName("accept/total counters")
    class AcceptAndTotal {

        @Test
        @DisplayName("incAccepts and incTotal track independently of rejects")
        void countersTrackIndependently() {
            RegCSVFile file = new RegCSVFile();

            file.incAccepts();
            file.incAccepts();
            file.incTotal();
            file.incTotal();
            file.incTotal();
            file.recordBadRow("row 99: NPE");

            assertEquals(2, file.getNumbAccepts());
            assertEquals(3, file.getSize());
            assertEquals(1, file.getNumbRejects());
        }
    }
}
