package com.teamgannon.trips.javafxsupport;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

/**
 * Small helpers for responsive JavaFX layouts. Replaces the hard-coded
 * {@code setPrefWidth(N)} / {@code setPrefHeight(N)} patterns scattered
 * through dialogs that prevent the user from getting useful proportions
 * when they resize the window.
 * <p>
 * Issue 34 of the codebase-review remediation. The worked example is
 * {@code TransferPlannerPanel.configureNodeTable}; this class extracts the
 * pattern so other callers can apply it in one line.
 *
 * <h2>Patterns</h2>
 *
 * <h3>Percentage-based table columns</h3>
 * For tables where each column has a desired width fraction:
 * <pre>{@code
 *   ResponsiveLayouts.bindColumnPercentages(table,
 *           nameCol, 0.30,
 *           massCol, 0.20,
 *           radiusCol, 0.20,
 *           catalogCol, 0.15,
 *           sourceCol, 0.15);
 * }</pre>
 * Percentages should sum to {@code <= 1.0}; JavaFX reserves a few px for
 * the scrollbar/border. Columns reflow automatically when the user resizes
 * the table.
 *
 * <h3>Last-column-flexes resize policy</h3>
 * For tables where columns have sensible default widths but the user-resize
 * gesture should expand the last column to fill leftover space, prefer
 * {@code TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN} (JavaFX
 * built-in) — no helper needed, just one line:
 * <pre>{@code
 *   table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
 * }</pre>
 * This is the cheapest migration when the existing per-column widths are
 * already sensible defaults.
 *
 * <h3>Fill remaining space</h3>
 * For the bottom element of a {@code VBox} (or rightmost in an {@code HBox})
 * that should grow when the container resizes:
 * <pre>{@code
 *   ResponsiveLayouts.fillVerticalSpace(myTable);
 *   ResponsiveLayouts.fillHorizontalSpace(myFiller);
 * }</pre>
 *
 * <h2>What this helper does NOT do</h2>
 * <ul>
 *   <li>Visual validation at 1024×768 vs 4K. That's a UX pass; pick a
 *       dialog, resize the window, eyeball it. The helper just makes the
 *       proportions track the container size.</li>
 *   <li>Per-FXML migration. FXMLs use {@code <ColumnConstraints percentWidth="…"/>}
 *       directly; this helper is for Java code that builds layouts programmatically.</li>
 * </ul>
 */
public final class ResponsiveLayouts {

    private ResponsiveLayouts() {
    }

    /**
     * Bind each column's {@code prefWidth} to a percentage of the table's
     * current width. Arguments are passed as flat {@code (column, percent)}
     * pairs — odd-indexed arguments must be {@code TableColumn}, even-indexed
     * arguments must be {@code Double}.
     *
     * @throws IllegalArgumentException if the argument count is odd or any
     *                                  pair is of the wrong type
     */
    public static void bindColumnPercentages(@NotNull TableView<?> table, Object... columnPercentPairs) {
        if (columnPercentPairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "bindColumnPercentages requires an even number of arguments (column, percent pairs)");
        }
        for (int i = 0; i < columnPercentPairs.length; i += 2) {
            Object colArg = columnPercentPairs[i];
            Object pctArg = columnPercentPairs[i + 1];
            if (!(colArg instanceof TableColumn<?, ?> column)) {
                throw new IllegalArgumentException(
                        "expected TableColumn at index " + i + ", got " + describe(colArg));
            }
            if (!(pctArg instanceof Number percent)) {
                throw new IllegalArgumentException(
                        "expected percentage (Number) at index " + (i + 1) + ", got " + describe(pctArg));
            }
            DoubleBinding binding = table.widthProperty().multiply(percent.doubleValue());
            column.prefWidthProperty().bind(binding);
        }
    }

    /**
     * Make {@code node} expand to fill its parent {@code VBox}'s available
     * vertical space. Equivalent to {@code VBox.setVgrow(node, Priority.ALWAYS)}
     * but reads more naturally at call sites.
     */
    public static void fillVerticalSpace(@NotNull Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
    }

    /**
     * Make {@code node} expand to fill its parent {@code HBox}'s available
     * horizontal space.
     */
    public static void fillHorizontalSpace(@NotNull Node node) {
        HBox.setHgrow(node, Priority.ALWAYS);
    }

    private static String describe(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }
}
