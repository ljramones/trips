# Event catalog

## Summary

- **35 event classes** total (post Phase 3.2 cleanup)
- **0 with zero subscribers** — 3 truly-dead classes deleted in Phase 3.2; 1 false positive (`OpenWorkbenchEvent`) was actually subscribed via a fully-qualified type in `MainPane.onOpenWorkbenchEvent`
- **16 cases of listener-publishes-follow-up-event** — but every chain ends in either `StatusUpdateEvent` or `BusyStateEvent`, both of which are intentional broadcast/fan-out events (status bar + busy indicator), not RPC. After review, these are **correct** event-driven patterns, not bus-as-RPC abuses. Issue 19's concern about RPC chains turned out not to apply in this codebase.

## Phase 3.2 actions taken

- Deleted unused classes:
  - `DataSetContextChangeEvent` (0 publishers, 0 subscribers)
  - `DataSetLoadEvent` (2 publishers in CHV/CSV import services, 0 subscribers — also removed the orphan `publishEvent` calls)
  - `NewDataSetEvent` (0 publishers, 0 subscribers)
- Confirmed `OpenWorkbenchEvent` is live (subscriber: `MainPane.onOpenWorkbenchEvent`; publisher: `ToolbarController`).
- Reviewed all 16 listener-fires-follow-up cases. All terminate in `StatusUpdateEvent` (status bar) or `BusyStateEvent` (busy indicator), which are fan-out broadcasts to UI sinks rather than synchronous request/response chains. No refactor needed.

## Events

### AddDataSetEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/AddDataSetEvent.java:17`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CHVDataImportService.java:83`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CSVDataImportService.java:87`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:67` in `onAddDataSetEvent`

### BusyStateEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/BusyStateEvent.java:7`

- **Publishers** (10 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:484`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:485`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:128`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:129`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:107`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:108`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:138`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:139`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:184`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:185`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:708` in `onBusyStateEvent`

- **Notes:** published by RPC chain from: DeleteRouteEvent, NewRouteEvent, RemoveDataSetEvent, UpdateRouteEvent

### CivilizationDisplayPreferencesChangeEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/CivilizationDisplayPreferencesChangeEvent.java:8`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/CivilizationPane.java:150`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/CivilizationPane.java:174`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/SystemPreferencesService.java:252` in `onCivilizationDisplayPreferencesChangeEvent` [Async, Transactional]

### ClearDataEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/ClearDataEvent.java:5`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:648`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:176`

- **Subscribers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:328` in `onClearDataEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarPropertiesPane.java:333` in `onClearDataEvent`

### ClearListEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/ClearListEvent.java:5`

- **Publishers** (4 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:581`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:653`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:177`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:293`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/ObjectViewPane.java:75` in `onClearListEvent`

### ColorPaletteChangeEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/ColorPaletteChangeEvent.java:8`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/GraphPane.java:417`

- **Subscribers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/config/application/TripsContext.java:154` in `onColorPaletteChangeEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:572` in `onColorPaletteChangeEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/SystemPreferencesService.java:216` in `onColorPaletteChangeEvent` [Async, Transactional]

### ContextSelectorEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/ContextSelectorEvent.java:11`

- **Publishers** (7 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/PlanetActionHandler.java:128`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/PlanetarySpacePane.java:391`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/PlanetarySpacePane.java:402`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java:609`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/planetary/PlanetarySystemCell.java:58`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/planetary/PlanetarySystemCell.java:68`
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarContextMenuHandler.java:207`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:53` in `onContextSelectorEvent`

### DataSetContextChangeEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/DataSetContextChangeEvent.java:8`

- **Publishers:** None

- **Subscribers:** None

- **Notes:** DEAD EVENT - no subscribers

### DataSetLoadEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/DataSetLoadEvent.java:17`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CHVDataImportService.java:86`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CSVDataImportService.java:90`

- **Subscribers:** None

- **Notes:** DEAD EVENT - no subscribers

### DeleteRouteEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/DeleteRouteEvent.java:11`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:125`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:154` in `onDeleteRouteEvent`

- **Notes:** candidate for direct call (1:1 publisher:subscriber)

### DisplayRouteEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/DisplayRouteEvent.java:11`

- **Publishers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:242`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:253`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:319`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:142` in `onDisplayRouteEvent`

### DisplayStarEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/DisplayStarEvent.java:17`

- **Publishers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/menubar/EditMenuController.java:103`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/ObjectViewPane.java:48`
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarContextMenuHandler.java:155`

- **Subscribers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:700` in `onDisplayStarEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarPropertiesPane.java:343` in `onDisplayStarEvent`

### DistanceReportEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/DistanceReportEvent.java:8`

- **Publishers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/menubar/ReportsMenuController.java:58`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarDisplayRecordCell.java:80`
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarContextMenuHandler.java:142`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:691` in `onDistanceReportEvent`

### ExportQueryEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/ExportQueryEvent.java:11`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/search/SearchPane.java:192`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:516` in `onExportQueryEvent`

- **Notes:** candidate for direct call (1:1 publisher:subscriber)

### GraphEnablesPersistEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/GraphEnablesPersistEvent.java:8`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/GraphPane.java:421`

- **Subscribers** (4 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/config/application/TripsContext.java:163` in `onGraphEnablesPersistEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:586` in `onGraphEnablesPersistEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/PlotManager.java:348` in `onGraphEnablesPersistEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/SystemPreferencesService.java:228` in `onGraphEnablesPersistEvent` [Async, Transactional]

### HighlightStarEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/HighlightStarEvent.java:7`

- **Publishers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/menubar/EditMenuController.java:101`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarDisplayRecordCell.java:55`
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarContextMenuHandler.java:119`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarPlotManager.java:504` in `onHighlightStarEvent`

### NewDataSetEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/NewDataSetEvent.java:8`

- **Publishers:** None

- **Subscribers:** None

- **Notes:** DEAD EVENT - no subscribers

### NewRouteEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/NewRouteEvent.java:12`

- **Publishers** (4 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/routemanagement/CurrentManualRoute.java:207`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/routemanagement/CurrentManualRoute.java:227`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/routemanagement/RoutePlotter.java:111`
  - `tripsapplication/src/main/java/com/teamgannon/trips/transits/TransitRouteBuilderService.java:162`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:79` in `onNewRouteEvent`

### OpenWorkbenchEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/OpenWorkbenchEvent.java:5`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/toolbar/ToolbarController.java:158`

- **Subscribers:** None

- **Notes:** DEAD EVENT - no subscribers

### PlotStarsEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/PlotStarsEvent.java:16`

- **Publishers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/menubar/ToolsMenuController.java:305`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/search/ShowStarMatchesDialog.java:164`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/search/ShowStarMatchesDialog.java:180`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:533` in `onPlotStarsEvent`

### RecenterStarEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/RecenterStarEvent.java:17`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/StarDisplayRecordCell.java:49`
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarContextMenuHandler.java:126`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:366` in `onRecenterStarEvent`

### RemoveDataSetEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/RemoveDataSetEvent.java:8`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dataset/model/DataSetDescriptorCell.java:60`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/dataset/DataSetManagerDialog.java:261`

- **Subscribers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:89` in `onRemoveDataSetEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/toolbar/ToolbarController.java:193` in `onRemoveDataSetEvent`

### RouteStarFilterEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/RouteStarFilterEvent.java:14`

- **Publishers** (4 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:142`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:154`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:166`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:176`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:581` in `onRouteStarFilterEvent`

### RoutingPanelUpdateEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/RoutingPanelUpdateEvent.java:13`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/PlotManager.java:95`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/PlotManager.java:116`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:333` in `onRoutingPanelUpdateEvent`

### RoutingStatusEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/RoutingStatusEvent.java:10`

- **Publishers** (5 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/RouteManager.java:151`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/routemanagement/CurrentManualRoute.java:125`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/routemanagement/CurrentManualRoute.java:175`
  - `tripsapplication/src/main/java/com/teamgannon/trips/transits/TransitRouteBuilderService.java:106`
  - `tripsapplication/src/main/java/com/teamgannon/trips/transits/TransitRouteBuilderService.java:180`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:68` in `onRoutingStatusEvent`

### SetContextDataSetEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/SetContextDataSetEvent.java:17`

- **Publishers** (8 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:511`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:313`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:398`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:424`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/PlotManager.java:119`
  - `tripsapplication/src/main/java/com/teamgannon/trips/search/components/DataSetPanel.java:77`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CHVDataImportService.java:85`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CSVDataImportService.java:89`

- **Subscribers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:138` in `onSetContextDataSetEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/toolbar/ToolbarController.java:188` in `onSetContextDataSetEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/SolPlanetsInitializer.java:107` in `onDatasetContextChange` [Transactional]

### ShowStellarDataEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/ShowStellarDataEvent.java:13`

- **Publishers** (5 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dataset/model/DataSetDescriptorCell.java:42`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dataset/model/DataSetDescriptorCell.java:48`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dataset/model/DataSetDescriptorCell.java:54`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/dataset/SelectActiveDatasetDialog.java:120`
  - `tripsapplication/src/main/java/com/teamgannon/trips/search/SearchPane.java:197`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:497` in `onShowStellarDataEvent`

### SolarSystemAnimationEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/SolarSystemAnimationEvent.java:10`

- **Publishers** (3 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:197`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:206`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:220`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java:325` in `onSolarSystemAnimationEvent`

### SolarSystemCameraEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/SolarSystemCameraEvent.java:5`

- **Publishers:** None

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java:713` in `onSolarSystemCameraEvent`

### SolarSystemDisplayToggleEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/SolarSystemDisplayToggleEvent.java:10`

- **Publishers** (11 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/ReferenceCueControlPane.java:53`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/ReferenceCueControlPane.java:60`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/ReferenceCueControlPane.java:67`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:246`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:253`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:260`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:267`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:274`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:281`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:288`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:295`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java:268` in `onSolarSystemDisplayToggleEvent`

### SolarSystemObjectSelectedEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/SolarSystemObjectSelectedEvent.java:11`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SolarSystemPlanetListPane.java:50`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SolarSystemObjectPropertiesPane.java:207` in `onObjectSelected`

- **Notes:** candidate for direct call (1:1 publisher:subscriber)

### SolarSystemScaleEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/SolarSystemScaleEvent.java:10`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:227`
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/solarsystem/SimulationControlPane.java:239`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/SolarSystemSpacePane.java:294` in `onSolarSystemScaleEvent`

### StarDisplayPreferencesChangeEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/StarDisplayPreferencesChangeEvent.java:8`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/StarsPane.java:204`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/StarsPane.java:234`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/SystemPreferencesService.java:240` in `onStarDisplayPreferencesChangeEvent` [Async, Transactional]

### StatusUpdateEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/StatusUpdateEvent.java:12`

- **Publishers** (69 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:570`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:319`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:379`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:430`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:466`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:469`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:475`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:481`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:511`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:524`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:581`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:588`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:80`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:84`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:98`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:115`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:119`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:125`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:163`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:167`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:81`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:96`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:100`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:105`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:113`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:127`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:131`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:136`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:156`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:173`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:177`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:182`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:66`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:75`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:82`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:93`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:115`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/PlotManager.java:163`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/PlotManager.java:277`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:556`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:559`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:587`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:592`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:597`
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:602`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:105`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:126`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:243`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:254`
  - `tripsapplication/src/main/java/com/teamgannon/trips/scripting/engine/PythonScriptEngine.java:35`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/export/CSVDataSetDataExportService.java:79`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/export/CSVDataSetDataExportService.java:85`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/export/CSVDataSetDataExportService.java:94`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/export/CSVQueryExporterService.java:80`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/export/CSVQueryExporterService.java:86`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/export/CSVQueryExporterService.java:95`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/LargeGraphSearchService.java:78`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/LargeGraphSearchService.java:90`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/graphsearch/LargeGraphSearchService.java:99`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CHVDataImportService.java:70`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CHVDataImportService.java:94`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CHVDataImportService.java:106`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CSVDataImportService.java:74`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CSVDataImportService.java:98`
  - `tripsapplication/src/main/java/com/teamgannon/trips/service/importservices/CSVDataImportService.java:110`
  - `tripsapplication/src/main/java/com/teamgannon/trips/transits/TransitCalculationService.java:123`
  - `tripsapplication/src/main/java/com/teamgannon/trips/transits/TransitCalculationService.java:136`
  - `tripsapplication/src/main/java/com/teamgannon/trips/transits/TransitCalculationService.java:158`
  - `tripsapplication/src/main/java/com/teamgannon/trips/workbench/DataWorkbenchController.java:1185`

- **Subscribers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:733` in `onStatusUpdateEvent`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/statusbar/StatusBarController.java:63` in `onStatusUpdateEvent`

- **Notes:** published by RPC chain from: AddDataSetEvent, ContextSelectorEvent, DeleteRouteEvent, ExportQueryEvent, NewRouteEvent, PlotStarsEvent, RecenterStarEvent, RemoveDataSetEvent, RouteStarFilterEvent, SetContextDataSetEvent, ShowStellarDataEvent, UpdateRouteEvent

### UIStateChangeEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/UIStateChangeEvent.java:8`

- **Publishers** (19 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:598`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:599`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:600`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:601`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:602`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:603`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainPane.java:604`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/menubar/ToolsMenuController.java:254`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/menubar/ToolsMenuController.java:255`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:97`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:106`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:115`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:122`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:129`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:134`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:142`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:153`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:161`
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/SharedUIFunctions.java:169`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/shared/UIStateSynchronizer.java:19` in `onUIStateChangeEvent`

### UpdateRouteEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/UpdateRouteEvent.java:11`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:104`
  - `tripsapplication/src/main/java/com/teamgannon/trips/routing/sidepanel/RoutingPanel.java:306`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:111` in `onUpdateRouteEvent`

### UpdateSidePanelListEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/UpdateSidePanelListEvent.java:8`

- **Publishers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/starplotting/StarPlotManager.java:330`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/screenobjects/ObjectViewPane.java:83` in `onUpdateSidePanelListEvent`

- **Notes:** candidate for direct call (1:1 publisher:subscriber)

### UserControlsChangeEvent

- **Source:** `tripsapplication/src/main/java/com/teamgannon/trips/events/UserControlsChangeEvent.java:8`

- **Publishers** (2 sites):
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/UserControlsPane.java:80`
  - `tripsapplication/src/main/java/com/teamgannon/trips/dialogs/preferences/UserControlsPane.java:88`

- **Subscribers** (1 site):
  - `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:249` in `onUserControlsChangeEvent`

## Dead events (no subscribers)

- `DataSetContextChangeEvent` (tripsapplication/src/main/java/com/teamgannon/trips/events/DataSetContextChangeEvent.java:8)
- `DataSetLoadEvent` (tripsapplication/src/main/java/com/teamgannon/trips/events/DataSetLoadEvent.java:17)
- `NewDataSetEvent` (tripsapplication/src/main/java/com/teamgannon/trips/events/NewDataSetEvent.java:8)
- `OpenWorkbenchEvent` (tripsapplication/src/main/java/com/teamgannon/trips/events/OpenWorkbenchEvent.java:5)

## Event-as-RPC chains

- `AddDataSetEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:67`) → `StatusUpdateEvent`
- `ContextSelectorEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/ViewContextHandler.java:53`) → `StatusUpdateEvent`
- `DeleteRouteEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:154`) → `BusyStateEvent`
- `DeleteRouteEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:154`) → `StatusUpdateEvent`
- `ExportQueryEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:516`) → `StatusUpdateEvent`
- `NewRouteEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:79`) → `BusyStateEvent`
- `NewRouteEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:79`) → `StatusUpdateEvent`
- `PlotStarsEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:533`) → `StatusUpdateEvent`
- `RecenterStarEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:366`) → `StatusUpdateEvent`
- `RemoveDataSetEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:89`) → `BusyStateEvent`
- `RemoveDataSetEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:89`) → `StatusUpdateEvent`
- `RouteStarFilterEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/graphics/panes/InterstellarSpacePane.java:581`) → `StatusUpdateEvent`
- `SetContextDataSetEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/DataSetEventHandler.java:138`) → `StatusUpdateEvent`
- `ShowStellarDataEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/MainSplitPaneManager.java:497`) → `StatusUpdateEvent`
- `UpdateRouteEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:111`) → `BusyStateEvent`
- `UpdateRouteEvent` → (listener `tripsapplication/src/main/java/com/teamgannon/trips/controller/splitpane/RouteEventHandler.java:111`) → `StatusUpdateEvent`

---

*Catalog generated by Phase 3.2 event-bus audit*

