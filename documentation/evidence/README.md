# Evidence

Store **real** proof of executed tests here, never edited or recreated afterwards.

| Folder | Put here |
|---|---|
| `phase1/` | screenshots / API responses for executed black-box cases, cause–effect graph images |
| `phase2/before/` | zipped `target/site/jacoco` and `target/pit-reports` from the baseline suite |
| `phase2/after/` | the same reports after the test-suite enhancement |
| `phase3/` | MockMvc / Maven output, perf-check output, security matrix logs, defect screenshots |

Naming: `<TestCaseID>_<YYYY-MM-DD>_<short>.png|txt`, e.g. `BB-CART-04_2026-11-20_response.txt`.
Each file should show the date, the build (commit hash) and who executed it.
If a test was not executed, leave Actual Result empty and Status "Not executed" — do not invent evidence.
