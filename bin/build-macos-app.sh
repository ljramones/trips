#!/usr/bin/env bash
set -euo pipefail

./mvnw-java17.sh -pl tripsapplication clean -Pjpackage package
