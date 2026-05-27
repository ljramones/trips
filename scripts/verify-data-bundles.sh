#!/usr/bin/env bash
#
# verify-data-bundles.sh — sanity-check the large catalog files that live at
# the repo root and are deliberately NOT tracked in git.
#
# The TRIPS application can technically start without these files (it will
# log a one-line warn for each missing optional file at boot), but the
# Workbench and the bulk import flows need them. This script gives a fresh
# clone a single command to verify which big bundles are present and
# matches them against a SHA-256 manifest.
#
# Usage:
#   scripts/verify-data-bundles.sh                # check all known bundles
#   scripts/verify-data-bundles.sh --list         # list expected files + URLs
#   scripts/verify-data-bundles.sh --update       # rewrite the manifest from
#                                                 # the files currently on disk
#                                                 # (run only after manual
#                                                 # download from a trusted
#                                                 # source)
#
# The manifest lives at scripts/data-bundles.sha256. It's tracked in git so
# that anyone re-downloading the bundles can verify they got the same bytes
# the maintainers have. Lines are `<sha256>  <relative path>` exactly as
# `shasum -a 256` emits — `shasum -a 256 -c` reads the same format.
#
# Issue 16 of trips-full-codebase-review-2026.md.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="$ROOT/scripts/data-bundles.sha256"

# Bundles we know about. Each line is `<relative-path>|<source-url>|<description>`.
# Source URLs are landing pages, not direct downloads — the actual file names
# rotate (HYG version bumps, exoplanet.eu catalog dumps carry a date in the
# name). Pick the latest version from each page when refreshing.
BUNDLES=(
    "HYG-MERGED-2M-TRIPS-11012026202505.csv|https://www.astronexus.com/hyg|HYG database, TRIPS-merged 2M-star bundle (~878 MB)"
    "exoplanet.eu_catalog_13-01-26_15_50_52.csv|https://exoplanet.eu/catalog/csv/|exoplanet.eu confirmed-planet catalog dump"
)

cmd_list() {
    echo "Expected data bundles (place at repo root):"
    echo
    for entry in "${BUNDLES[@]}"; do
        IFS='|' read -r path url desc <<< "$entry"
        echo "  $path"
        echo "    source: $url"
        echo "    notes:  $desc"
        echo
    done
    echo "Bundles are git-ignored (.gitignore excludes *.csv at the repo root)."
    echo "After downloading, run: scripts/verify-data-bundles.sh"
}

cmd_check() {
    if [[ ! -f "$MANIFEST" ]]; then
        echo "no manifest at $MANIFEST" >&2
        echo "run 'scripts/verify-data-bundles.sh --update' after downloading" >&2
        echo "to record the current bundle checksums." >&2
        return 2
    fi

    local any_present=0
    while IFS='|' read -r path _ _; do
        if [[ -f "$ROOT/$path" ]]; then
            any_present=1
        else
            echo "missing: $path (optional; see --list for source)"
        fi
    done < <(printf '%s\n' "${BUNDLES[@]}")

    if [[ "$any_present" -eq 0 ]]; then
        echo "no known bundles present in $ROOT" >&2
        return 1
    fi

    (cd "$ROOT" && shasum -a 256 -c --ignore-missing "$MANIFEST")
}

cmd_update() {
    : > "$MANIFEST.tmp"
    local any=0
    for entry in "${BUNDLES[@]}"; do
        IFS='|' read -r path _ _ <<< "$entry"
        if [[ -f "$ROOT/$path" ]]; then
            (cd "$ROOT" && shasum -a 256 "$path") >> "$MANIFEST.tmp"
            any=1
        else
            echo "skipped (not present): $path"
        fi
    done

    if [[ "$any" -eq 0 ]]; then
        rm -f "$MANIFEST.tmp"
        echo "no bundles found; nothing to manifest" >&2
        return 1
    fi

    mv "$MANIFEST.tmp" "$MANIFEST"
    echo "wrote $MANIFEST:"
    cat "$MANIFEST"
}

case "${1:---check}" in
    --check|"")    cmd_check ;;
    --list)        cmd_list ;;
    --update)      cmd_update ;;
    -h|--help)
        sed -n '2,/^$/p' "$0" | sed 's/^# \{0,1\}//'
        ;;
    *)
        echo "unknown option: $1" >&2
        echo "usage: $0 [--check|--list|--update|--help]" >&2
        exit 2
        ;;
esac
