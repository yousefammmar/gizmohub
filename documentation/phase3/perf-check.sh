#!/usr/bin/env bash
# NFR-01 performance check for GizmoHub (see documentation/requirements/SRS.md §4).
# Usage:  start the app (cd backend && ./mvnw spring-boot:run), then:
#         bash documentation/phase3/perf-check.sh [base-url] [requests] [parallel]
# Prints p50/p95/max response time and the number of non-200 answers.
# Acceptance criterion (set before testing): p95 <= 300 ms and 0 errors.
set -euo pipefail
BASE="${1:-http://localhost:8080}"
REQUESTS="${2:-200}"
PARALLEL="${3:-10}"
URL="$BASE/api/products?category=AUDIO"
OUT="$(mktemp)"

echo "Warm-up: 20 requests (not measured)"
for _ in $(seq 1 20); do curl -s -o /dev/null "$URL"; done

echo "Measuring $REQUESTS requests, $PARALLEL in parallel: $URL"
seq 1 "$REQUESTS" | xargs -P "$PARALLEL" -I{} \
  curl -s -o /dev/null -w '%{http_code} %{time_total}\n' "$URL" >> "$OUT"

ERRORS=$(awk '$1 != 200' "$OUT" | wc -l | tr -d ' ')
sort -k2 -n "$OUT" | awk -v n="$REQUESTS" '
  { t[NR] = $2 * 1000 }
  END {
    p50 = t[int(n * 0.50 + 0.5)]; p95 = t[int(n * 0.95 + 0.5)]; max = t[NR];
    printf "p50 = %.1f ms\np95 = %.1f ms\nmax = %.1f ms\n", p50, p95, max
  }'
echo "errors (non-200) = $ERRORS"
echo "raw results: $OUT   (copy into documentation/evidence/phase3/ with date and machine details)"
