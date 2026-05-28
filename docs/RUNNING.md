# Running TRIPS

## From a release package
1. Once installed, the install directory is complete and independent.
2. In the main directory, run either:
   - `runme.bat` (Windows)
   - `runme.sh` (Mac/Linux)

## From source (developer)
- See [Contributing](../CONTRIBUTING.md) and [CLAUDE Notes](../CLAUDE.md) for development guidance.

## Release-Prep Profiling With JFR
Use this path when validating large plot/clear cycles, Advanced Query export,
and dense route settings.

1. Start TRIPS with JDK 25 and Java Flight Recorder:
   ```bash
   ./scripts/run-jfr-java25.sh
   ```
2. Exercise the scenario:
   - Plot a large dataset, clear it, and repeat several times.
   - Run Advanced Query export on a large result.
   - Try route settings near the density guardrail.
3. Close the app normally. The script writes recordings under:
   ```text
   tripsapplication/target/jfr/
   ```

To dump the recording while the app is still running:
```bash
$JAVA_HOME/bin/jps -lv
$JAVA_HOME/bin/jcmd <pid> JFR.dump name=trips-release filename=tripsapplication/target/jfr/manual-dump.jfr
```

For the FX-thread database check, open the `.jfr` in JDK Mission Control and
inspect the `JavaFX Application Thread` samples. During large plot/export/route
work it should not be spending time inside repository, `EntityManager`,
Hibernate, H2, or JDBC calls. A quick thread dump during the same workload is
also useful:
```bash
$JAVA_HOME/bin/jcmd <pid> Thread.print -l > tripsapplication/target/jfr/thread-dump.txt
```
