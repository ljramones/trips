#!/usr/bin/env bash
#
# check-logging.sh — surface SLF4J string-concatenation in log calls.
#
# Rule: always use parameter substitution.
#   GOOD:  log.info("loaded {} stars", count);
#   BAD:   log.info("loaded " + count + " stars");
#
# See AGENTS.md > "Logging" for rationale.
#
# Usage:
#   scripts/check-logging.sh                # list violations, exit 1 if any
#   scripts/check-logging.sh --count        # just the count
#
# Phase 7.4 of the codebase-review remediation plan will wire this into
# maven-checkstyle-plugin once the existing violations are cleaned up.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/tripsapplication/src/main/java"

# Matches:   log.<level>( "..." +
#   covers the common single-line concatenation pattern.
PATTERN='log\.(trace|debug|info|warn|error)\(\s*"[^"]*"\s*\+'

if [[ ! -d "$SRC" ]]; then
    echo "error: source tree not found at $SRC" >&2
    exit 2
fi

count=$(grep -rE "$PATTERN" "$SRC" 2>/dev/null | wc -l | tr -d ' ')

if [[ "${1:-}" == "--count" ]]; then
    echo "$count"
    exit 0
fi

if [[ "$count" -eq 0 ]]; then
    echo "OK: no SLF4J string-concat violations under $SRC"
    exit 0
fi

echo "Found $count SLF4J string-concatenation site(s). Use parameterized logging:"
echo "  log.info(\"count={}\", count);    // not: log.info(\"count=\" + count);"
echo
grep -rEn "$PATTERN" "$SRC" 2>/dev/null
exit 1
