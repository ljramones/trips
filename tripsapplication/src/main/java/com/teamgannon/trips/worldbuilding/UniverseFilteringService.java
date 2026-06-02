package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.Universe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The §5 invariants' enforcement point — the single chokepoint for "should this catalog entry
 * be visible right now?" decisions.
 *
 * <p>v2 Phase F.1 §5.3 — every designer panel + every future renderer overlay that respects
 * universe scoping goes through this service. If a consumer queries the raw catalog and bypasses
 * the filter, the §5 invariants don't hold for that consumer. Step 8's invariant tests will
 * catch this across all activation combinations.
 *
 * <p>Two responsibilities, intentionally co-located:
 * <ol>
 *   <li><b>Visibility</b>: {@link #isVisible(Cataloged)} answers per entry;
 *       {@link #filter(List)} bulk-applies for collection-shaped callers.</li>
 *   <li><b>Live refresh broker</b>: {@link #subscribeToFilterChanges(Runnable)} lets short-
 *       lived panels register a refresh callback that fires when the underlying activation
 *       state changes. The service is the natural place because consumers already depend on it
 *       for filtering — adding the subscription API avoids a separate "filter changed" broker.</li>
 * </ol>
 *
 * <p>Threading: subscribers' callbacks are dispatched via
 * {@link FxThread#runOnFxThread(Runnable)} so panels can update their scene graph safely.
 * Spring delivers {@link UniverseActivationChangedEvent} synchronously on the publishing thread
 * (F.1 Step 5's decision); the FX-thread wrap is defensive — if a future off-FX-thread publisher
 * appears, callbacks still land on the FX thread.
 *
 * <p>Lifecycle: subscribers MUST call the {@link Runnable} returned by
 * {@link #subscribeToFilterChanges} when they dispose. Forgetting causes the panel reference to
 * leak through the {@code refreshCallbacks} list. The panels' menu-controller-driven open/close
 * pattern uses a {@code stage.setOnCloseRequest} hook to invoke the unsubscribe handle.
 */
@Slf4j
@Service
public class UniverseFilteringService {

    private final UniverseDesignerService universeService;
    private final List<Runnable> refreshCallbacks = new CopyOnWriteArrayList<>();

    public UniverseFilteringService(UniverseDesignerService universeService) {
        this.universeService = universeService;
    }

    // ---------------------------------------------------------------- visibility

    /**
     * Returns {@code true} iff this entry should currently be visible to the user, per the §5
     * invariants:
     * <ul>
     *   <li>{@code universeId() == null} → always visible (canonical/real, R1.9 + R5.6)</li>
     *   <li>{@code universeId()} references an active Universe row → visible</li>
     *   <li>{@code universeId()} references an inactive Universe row → hidden</li>
     *   <li>{@code universeId()} references a non-existent Universe row → hidden (defensive;
     *       this shouldn't happen in production since the FK ON DELETE SET NULL clears
     *       references when a Universe is deleted, but a stale in-memory record could carry
     *       the dangling id transiently)</li>
     * </ul>
     *
     * <p>Each call issues one repository read for non-null universeIds. For collection-shaped
     * callers, prefer {@link #filter(List)} which bulk-fetches the active set once.
     *
     * @param entry the catalog entry to test
     * @return {@code true} iff the entry should be visible
     */
    public boolean isVisible(Cataloged entry) {
        String universeId = entry.universeId();
        if (universeId == null) {
            return true;
        }
        return universeService.findById(universeId)
                .map(Universe::active)
                .orElse(false);
    }

    /**
     * Returns the set of currently-active universe ids. Single bulk-fetch from the
     * UniverseDesignerService. Phase F.2 §4.5 — exposed as a public method so dependent
     * services (e.g. AliasDesignerService) can apply universe-visibility filtering to their own
     * queries without each one re-deriving the active set.
     *
     * @return the set of universe ids whose {@code active} flag is currently {@code true}
     */
    public Set<String> getActiveUniverseIds() {
        return universeService.findAllActive().stream()
                .map(Universe::id)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a {@code Map<universeId, universeName>} for all currently-active universes. Phase
     * F.2 §6.1 — the renderer tooltip needs to display the universe's human-readable name
     * alongside each alias text ("Vulcan (Star Trek)"), and {@code AliasDesignerService}
     * shouldn't make a second round trip per hover to resolve names. Single bulk-fetch.
     *
     * @return a map keyed by universe id with the display name as value; empty if no
     *         universes are active
     */
    public java.util.Map<String, String> getActiveUniverseNamesById() {
        return universeService.findAllActive().stream()
                .collect(Collectors.toMap(Universe::id, Universe::name));
    }

    /**
     * Bulk-filters a collection by universe visibility. O(N) after a single bulk-fetch of the
     * active-universe id set; preserves source order.
     *
     * @param entries the unfiltered list
     * @param <T>     any {@link Cataloged} subtype
     * @return a new list containing only the visible entries, in source order
     */
    public <T extends Cataloged> List<T> filter(List<T> entries) {
        Set<String> activeIds = getActiveUniverseIds();
        return entries.stream()
                .filter(e -> {
                    String universeId = e.universeId();
                    return universeId == null || activeIds.contains(universeId);
                })
                .toList();
    }

    // ---------------------------------------------------------------- live refresh broker

    /**
     * Subscribe a {@link Runnable} to fire whenever the universe activation state changes (i.e.
     * any time {@link UniverseActivationChangedEvent} arrives). The callback dispatches via the
     * FX thread (panels update scene graph safely).
     *
     * <p>Returns an unsubscribe handle: callers MUST invoke {@link Runnable#run()} on the
     * returned value when they dispose to avoid leaking a reference. The expected lifecycle for
     * F.1's designer panels is:
     * <pre>{@code
     * private Runnable unsubscribe;
     * // constructor:
     * this.unsubscribe = filteringService.subscribeToFilterChanges(this::reload);
     * // disposal:
     * if (unsubscribe != null) unsubscribe.run();
     * }</pre>
     *
     * @param callback the refresh logic — typically a no-arg reload of the panel's backing list
     * @return an unsubscribe handle; call its {@code run()} on disposal
     */
    public Runnable subscribeToFilterChanges(Runnable callback) {
        refreshCallbacks.add(callback);
        return () -> refreshCallbacks.remove(callback);
    }

    /**
     * Listens for {@link UniverseActivationChangedEvent} and fans out to all subscribers.
     * Each callback runs on the FX thread via {@link FxThread#runOnFxThread(Runnable)}.
     * Exceptions in one callback don't prevent others from firing.
     */
    @EventListener
    public void onUniverseActivationChanged(UniverseActivationChangedEvent event) {
        log.debug("Filter cache invalidated — universe '{}' nowActive={}; notifying {} subscriber(s)",
                event.universe().name(), event.nowActive(), refreshCallbacks.size());
        for (Runnable callback : refreshCallbacks) {
            FxThread.runOnFxThread(() -> {
                try {
                    callback.run();
                } catch (Exception ex) {
                    log.error("Filter refresh callback failed", ex);
                }
            });
        }
    }
}
