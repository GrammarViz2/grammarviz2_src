# Changelog

All notable changes to GrammarViz2 are documented here.
This project adheres to [Semantic Versioning](https://semver.org/).

## [3.0.0] — 2026-06-30

Modernization + correctness release: upgrades to the jMotif 2.0.0 line, moves the
build to Java 21, and fixes a cluster of long-standing defects in the GUI
"Guess parameters" (automated SAX parameter selection) workflow.

### Added
- **`CHANGELOG.md`** (this file).
- **Coverage-aware parameter selection.** The guesser now selects, among the sampled
  (window, PAA, alphabet) points, those whose pruned grammar covers at least the user's
  **minimal rule-cover threshold**, and returns the most concise of those — instead of
  ignoring coverage entirely. (`GrammarvizParamsSampler.selectBest`)
- **User feedback when sampling cannot satisfy the request:**
  - `SAMPLING_BELOW_THRESHOLD` — best params applied, but no point reached the cover
    threshold; an informational dialog tells the user which params were used instead.
  - `SAMPLING_FAILED` — no usable parameters (empty grid or sampler error); a warning
    dialog suggests widening the ranges, and the "Guess" button is reset.
  (`GrammarvizChartPanel`)
- **Input validation on the guesser dialog.** Invalid ranges (MIN > MAX, non-positive
  step, empty interval, alphabet outside [2, 20], cover outside [0, 1]) are rejected with
  a message and the dialog stays open for correction. (`GrammarvizGuesserDialog`,
  `GrammarvizGuesserPane.saveValues`)
- **Test coverage for the grammar reductor and parameter selector** (previously almost
  none — only one `TestRulePruner` in jmotif-gi and zero in this repo):
  - `TestGrammarReductor` (9) — contract tests against the public jmotif-gi 2.0.0 reductor
    API GrammarViz depends on: the byte-cost model (`computeGrammarSize`/`computeRuleSize`),
    the coverage primitives (`computeCover`/`isCovered`/`hasEmptyRanges`/`updateRanges`,
    including the `isCovered() ⟺ coverage==1.0` invariant behind the P1 fix), pruning
    size + idempotence, and an end-to-end `RulePruner.sample()` golden test on a bundled
    series for both Sequitur and RePair.
  - `TestParamsSamplerGrid` (5) — the extracted pure grid scan (`sampleGrid`): inclusive
    MAX bounds, multi-step coverage, PAA>window skip, degenerate→empty, and bad-alphabet
    point skipped (not fatal).
  - `TestParamsSamplerSelection` (6) — the selection core (`selectBest`): empty, threshold
    filter, fallback, deterministic tie-break, covered-beats-uncovered, full-tie stability.
  Suite total: **26 tests** (was 6).

### Fixed
- **Guesser dialog edits were silently discarded.** The dialog populated its fields from
  the session but never read them back; the OK handler was a no-op (`// set params`). All
  typed window/PAA/alphabet ranges, steps, interval, and cover threshold were dropped, so
  the sampler always ran the hardcoded default grid. The OK handler now commits and
  validates the fields into the session via the new `saveValues()`.
  (`GrammarvizGuesserDialog`, `GrammarvizGuesserPane`)
- **Coverage threshold had no effect.** `minimalCoverThreshold` was shown in the dialog but
  never read by the sampler; selection ranked purely by rule-reduction. Now filtered by
  `getCoverage() >= threshold` (see Added). (`GrammarvizParamsSampler`)
- **Silent UI hang on an empty/degenerate grid.** When the sampling grid produced no points
  (e.g. a short selected interval), `res.get(0)` threw inside a `Future` whose result was
  never read, so the exception was swallowed and the executor was never shut down —
  `awaitTermination` blocked for the full 10 minutes with the button stuck on "Stop!".
  Now: the grid is guarded (empty → `SAMPLING_FAILED`), `call()` is wrapped so it always
  fires a terminal event, and the executor is `shutdown()` right after submit so
  `awaitTermination` returns promptly. The same unguarded `res.get(0)` on the
  interrupt/Stop path is fixed too. (`GrammarvizParamsSampler`, `GrammarvizChartPanel`)
- **Maximum range bounds were exclusive.** The sampling loops used `<`, so the user's typed
  MAX (window/PAA/alphabet) was never actually evaluated. Now `<=` (inclusive).
  (`GrammarvizParamsSampler`)
- **One bad parameter point aborted the whole grid.** A `SAXException` from a single
  `RulePruner.sample()` call (e.g. an out-of-range alphabet) previously propagated and
  killed the run; such points are now logged and skipped. (`GrammarvizParamsSampler`)
- **Shared static Swing widgets in the guesser pane.** The dialog's text fields/labels were
  `static final` although a new pane is constructed on every invocation; they are now
  instance fields, removing a cross-instance aliasing hazard.
  (`GrammarvizGuesserPane`)

### Changed
- **Extracted `GrammarvizParamsSampler.sampleGrid(...)`** as a pure, Swing-free static method
  (the grid loop formerly inline in `call()`), so the sampling behavior is unit-testable.
  Behavior is unchanged; `call()` now delegates to it.
- **Dependencies:** `jmotif-sax` 1.2.0 → **2.0.0**, `jmotif-gi` 1.1.0 → **2.0.0**
  (the aligned, Maven-Central 2.0.0 line). No source changes were required — the existing
  code compiles unchanged against the 2.0.0 API.
- **Build:** Java target `1.8` → **`<release>21`**; maven-compiler 3.8.0 → 3.13.0,
  surefire 2.22.2 → 3.2.5, jacoco 0.8.7 → 0.8.13.
- **CI:** build matrix `java [8, 11, 17]` → **`[21, 25]`**; publish matrix `[8, 11]` and
  publish step `11` → **`21`**; actions bumped (checkout/setup-java v2 → v4,
  codecov-action v2 → v4).
- **Default grammar-induction algorithm is now RePair** (was Sequitur), via
  `UserSession.DEFAULT_GI_ALGORITHM`. This affects the whole app — grammar inference in the
  model and the parameter guesser — for users who don't explicitly pick an algorithm in
  Options.
- **Project version:** `1.0.1-SNAPSHOT` → **`3.0.0`**.

### Behavior changes (note for existing users)
The guesser may now return **different "best parameters"** than pre-3.0.0 for the same
input — these are intentional consequences of the fixes above:
- **GI algorithm:** the guesser previously always scored with **RePair** (hardcoded); it now
  honors the session's GI algorithm, matching what the rest of the app uses. The session
  default is **RePair** (see Changed), so the guesser's default algorithm is unchanged from
  pre-3.0.0 — but it now follows the user's choice if they switch to Sequitur in Options.
  (`GrammarvizParamsSampler` issue #3)
- **Normalization threshold:** the guesser previously used a hardcoded `0.01`; it now uses
  the session's normalization threshold (default **0.05**), affecting SAX discretization
  during scoring.
- **Tie-breaking:** points with equal reduction are now ordered deterministically by grammar
  size (previously: arbitrary grid-iteration order).
- **Coverage filter + inclusive bounds** (above) also shift which point wins.

### Known follow-ups (not in this release)
- README still brands "GrammarViz 3.0" with stale build traces / a phantom
  `0.0.1-SNAPSHOT` jar name — prose refresh pending.
- Publish workflow deploys to **GitHub Packages** (default `distributionManagement`);
  a Maven Central profile would need separate activation.
- CLI `GrammarVizAnomaly` passes PAA/alphabet **transposed** to `findRRAPruned` at 5 sites
  (separate from the guesser; not addressed here).
- A fresh "Stop!" `ActionListener` is added to the operational button on every guess run and
  never removed (listeners accumulate across runs — benign for a single run).
- Pre-existing off-EDT Swing updates in the sampler callbacks (new user dialogs are
  correctly marshalled via `SwingUtilities.invokeLater`; the surrounding `resetChartPanel`
  calls were left as-is, matching prior behavior).

### Verification
- `mvn clean test` green on JDK 25 / `<release>21` against jmotif 2.0.0 — **26 tests pass**
  (1 RRA + 9 reductor + 5 grid + 6 selection + 5 interval).
- Design + implementation reviewed by adversarial verify/critic passes; all changed GUI
  flows re-walked by fake-user UX traces (edits-honored, invalid-input, empty-grid-no-hang,
  below-threshold, coverage-met + issue #3, Stop-interrupt) — all pass, no regressions.
