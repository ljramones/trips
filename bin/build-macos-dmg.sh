#!/usr/bin/env bash
set -euo pipefail

version="$(./mvnw-java25.sh -q -pl tripsapplication -Dexpression=project.version -DforceStdout help:evaluate)"
app_version="$version"
if [[ "$app_version" == 0* ]]; then
  app_version="1${app_version:1}"
fi

./mvnw-java25.sh -pl tripsapplication clean -Pjpackage -Djpackage.type=DMG -Djpackage.appVersion="$app_version" package
