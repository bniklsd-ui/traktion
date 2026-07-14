---
status: archive
purpose: Archiv der alten Session-stopped-Blöcke von Phase 0. Verbatim, uneditiert, newest-first. Rotation: wenn ein neuer Block in phase0/CLAUDE.md geschrieben wird, wandert der bisher-neueste hierher.
read-when: bei Bedarf an historische Session-Details (was wurde wann gebaut, welche Blocker gab es)
detail: L3
up: ./CLAUDE.md
down:
updated: 2026-07-14
---

# Phase 0 — Sessions-Archiv

> Alte `## Session stopped`-Blöcke, aus `phase0/CLAUDE.md` rausrotiert. Verbatim, uneditiert.
> Newest-first. Keine Größenbeschränkung. Kein Editieren — sie sind Historie.
>
> Rotationsregel siehe `docs/DOC_LAYERS_CONVENTION.md`.

---

## Session stopped — 2026-07-13

### Completed
- `docs/CONVENTIONS.md`: P0.1 Konventions-Import erledigt (Commit c2d132b). Zwei Abschnitte: "Übernommen" (17 Konventionen) und "Bewusst verworfen" (13 Punkte, jeweils begründet). Header-Card nach Doc-Layers-Konvention.
- P0.3 Vorregistrierung geprüft: `M1_PREREGISTRATION.md` (Commit a40e818) entspricht Plan §6 vollständig. NICHT berührt (Plan §6: nie editieren).
- Toolchain-Versionen verifiziert (T-D12): Minecraft 26.2 ✅, fabric-api 0.154.0+26.2 ✅, loader 0.19.3 ✅, loom 1.17 ✅, gradle 9.5.1 ✅ (via Loom 1.17 changelog), Java 21 (Build-Toolchain, via Loom 1.11+ requirement).
- `PHASE0_PLAN.md` geschrieben (Commit c496a52).
- Operator-Infrastruktur committet (cba0351): Permissions gelockert (deny→ask/allow), da Vorgängerversion zu restriktiv. ⚠ Spannung zu Plan §5/P0.1 + §9 notiert: `example_project/**` ist jetzt erlaubt/ask, nicht mehr deny — die Plan-Regel "kein Agent liest es nach P0" wird faktisch durch `.gitignore` getragen, nicht mehr durch Permission-Durchsetzung.

### Next
- **P0.2 Step 1:** Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` erstellen (allererste Aktion von P0.2). Alle drei im selben Commit. `docs/INDEX.md` muss One-Liner für `docs/CONVENTIONS.md` enthalten (nachtragen).
- **P0.2 Step 2:** Gradle-Multi-Modul-Skelett (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `train-core/build.gradle.kts`, `train-mc/build.gradle.kts`). `gradle :train-core:test` muss grün sein (leerer Smoke-Test).
- **P0.2 Step 3:** `ROADMAP.md` + `ARCHITECTURE.md` Stubs.
- **P0.2 Step 4:** Log-Konventionen + Testmatrix in `docs/CONVENTIONS.md` ergänzen.
- **P0.4:** MC-Spike auf eigenem Branch `p0.4-mc-spike`. Beantwortet T-D3. [VERIFY]-Fragen: Java-Mod-Target, PersistentState-API-Name in 26.2, jqwik-Unterstützung.

### Open questions / blockers
- **`M1_PREREGISTRATION.md` related-Verweis:** Die Datei verweist auf `./TRAKTION_OPENCODE_CONFIG.md` (related). Diese Datei ist nicht im Repo-Root sichtbar. Möglicherweise in den Operator-Bereich verschoben (Commit a0cc00d "move opencode config doc to operator-only"). Kein Build-Drift — Operator-Infrastruktur.
- **Java-Mod-Target [VERIFY]:** Build-Toolchain ist Java 21 (Loom 1.11+). Die Java-Version, die die Mod selbst targetet (für MC 26.2), ist noch offen. P0.4 muss es klären.
- **jqwik [VERIFY]:** Ob jqwik unter Gradle 9.5.1 sauber läuft, ist ungeprüft. P0.4 soll es klären. Fallback: JUnit 5 + eigene Generatoren.
- **PersistentState-API-Name [VERIFY]:** Der exakte API-Name des welt-attached Persistent State in 26.2 ist offen. Das Welt-Datenformat hat sich in 26.1 geändert (Plan T-D15). P0.4 muss es gegen echte 26.2-Quellen verifizieren, nicht gegen 1.21er-Tutorials.
- **Permission-Spannung (cba0351) — GELÖST:** `example_project/**` war nach cba0351 erlaubt/ask, was im Widerspruch zu Plan §5/P0.1 + §9 stand. Der Operator hat `example_project/` in den Operator-Bereich verschoben — es ist physisch nicht mehr im Repo. Die Spannung ist damit aufgelöst: kein Agent kann es lesen, weil es nicht da ist. `.gitignore`-Eintrag (Zeile 1) bleibt als Belt-and-Suspenders.
- **Tool-Calls:** Diese Session benutzte ~26 Tool-Calls. P0.2 (Skelett) und P0.4 (Spike) benötigen jeweils eigene Sessions.
