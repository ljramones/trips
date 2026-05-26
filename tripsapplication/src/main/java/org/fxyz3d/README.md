# In-tree F(X)yz fork (heavily modified)

This directory holds a **heavily-modified fork** of the F(X)yz library
(<https://github.com/FXyz/FXyz>), vendored into the TRIPS source tree under the
original `org.fxyz3d.*` package names.

## Why is this in the source tree (not a Maven dependency)?

The fork carries substantial local modifications relative to upstream — enough
that swapping it for `org.fxyz3d:fxyz3d` from Maven Central would regress
behaviour. Two production code paths in `com.teamgannon.trips` use classes
from this fork directly:

| Caller | Imported class |
|---|---|
| `com.teamgannon.trips.experimental.AsteroidFieldWindow` | `org.fxyz3d.geometry.Point3D`, `org.fxyz3d.shapes.primitives.ScatterMesh` |
| `com.teamgannon.trips.particlefields.RingFieldRenderer` | `org.fxyz3d.geometry.Point3D`, `org.fxyz3d.shapes.primitives.ScatterMesh` |

Internal coupling within the fork means the other ~115 classes are needed
transitively even though TRIPS code doesn't reference them by name.

## License

F(X)yz is published under a 3-clause BSD license. The copyright notice
"Copyright (c) 2013-2019, F(X)yz" is preserved at the top of every source
file in this tree. Local modifications are also covered by that license.

## Phase 3.5 (codebase review)

Phase 3.5 of the codebase-review remediation
(see `trips-full-codebase-review-2026.md`) originally proposed replacing this
in-tree copy with a Maven dependency. That option was retired once the fork's
local modifications were confirmed. The in-tree fork is **intentional**; this
README is the externalization deliverable.

A future option (not pursued) would be to lift this tree into its own Maven
module (e.g. `tripsapplication-fxyz3d-fork/`) so the fork status is more
visible at the build level. That refactor is tracked as a Phase 7 / clean-up
follow-up only.

## Don't

- **Don't** delete this tree.
- **Don't** add `org.fxyz3d:fxyz3d` to `tripsapplication/pom.xml` — it will
  shadow this fork on the classpath, and `AsteroidFieldWindow` /
  `RingFieldRenderer` rely on this fork's constructor signatures
  (e.g. `new Point3D(float, float, float, int, float, float)`) that upstream
  does not expose.
- **Don't** rename the `org.fxyz3d.*` package without auditing every site
  inside this tree plus the two TRIPS callers listed above.
