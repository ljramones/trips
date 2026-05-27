package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link ImportTaskControl} interface contract via an anonymous
 * implementation (Issue 47).
 * <p>
 * Both production implementations (`CSVDataImportService` and
 * `CHVDataImportService`) extend {@code javafx.concurrent.Service}, so
 * they can't be instantiated outside an FX context — those need TestFX
 * for real coverage. This file pins the interface shape so a future
 * stub or test double conforms without surprises.
 */
@DisplayName("ImportTaskControl interface contract")
class ImportTaskControlTest {

    @Test
    @DisplayName("anonymous impl satisfies all three required methods")
    void anonymousImplCompiles() {
        ImportTaskControl ctrl = new ImportTaskControl() {
            @Override public boolean cancelImport() { return true; }
            @Override public String whoAmI() { return "stub"; }
            @Override public Dataset getCurrentDataSet() { return null; }
        };
        assertTrue(ctrl.cancelImport());
        assertEquals("stub", ctrl.whoAmI());
        assertNull(ctrl.getCurrentDataSet());
    }

    @Test
    @DisplayName("cancelImport returning false models a no-op cancel (idempotent)")
    void cancelCanBeIdempotent() {
        ImportTaskControl ctrl = new ImportTaskControl() {
            @Override public boolean cancelImport() { return false; }
            @Override public String whoAmI() { return "no-op"; }
            @Override public Dataset getCurrentDataSet() { return null; }
        };
        // Calling twice should be safe — interface contract allows
        // implementations to refuse a redundant cancel.
        assertFalse(ctrl.cancelImport());
        assertFalse(ctrl.cancelImport());
    }

    @Test
    @DisplayName("whoAmI distinguishes implementations for the busy indicator")
    void whoAmIDistinguishes() {
        ImportTaskControl csv = mockOf("CSV importer");
        ImportTaskControl chv = mockOf("CHV importer");
        assertNotEquals(csv.whoAmI(), chv.whoAmI());
    }

    @Test
    @DisplayName("getCurrentDataSet observably reflects mutator state")
    void currentDataSetReflectsState() {
        AtomicBoolean started = new AtomicBoolean(false);
        Dataset placeholder = new Dataset();
        ImportTaskControl ctrl = new ImportTaskControl() {
            @Override public boolean cancelImport() { return started.compareAndSet(true, false); }
            @Override public String whoAmI() { return "x"; }
            @Override public Dataset getCurrentDataSet() {
                return started.get() ? placeholder : null;
            }
        };

        assertNull(ctrl.getCurrentDataSet());
        started.set(true);
        assertSame(placeholder, ctrl.getCurrentDataSet());
        assertTrue(ctrl.cancelImport());
        assertNull(ctrl.getCurrentDataSet());
    }

    private static ImportTaskControl mockOf(String name) {
        return new ImportTaskControl() {
            @Override public boolean cancelImport() { return true; }
            @Override public String whoAmI() { return name; }
            @Override public Dataset getCurrentDataSet() { return null; }
        };
    }
}
