---
status: live
purpose: Phasen-Kopf für P0 — Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike. Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P0; vor jedem Schritt in P0.2–P0.4; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE0_PLAN.md      # Konzept/Plan, aus dem P0 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-07-14
---

# Phase 0 — Fundament, Konventions-Import & Messinstrument

> **Status:** P0.1 ✅ · P0.3 ✅ · P0.2 Step 2 ✅ (train-core grün) · P0.2 Step 3 ✅ · P0.2 Step 4 ✅ · P0.4 ⏳
>
> **Ausnahme P0:** Planung UND Ausführung in derselben Session. Ab P1 getrennt.
>
> **Konzept:** `docs/plans/PHASE0_PLAN.md` (gelockte Entscheidungen, Schritt-Sequenz, Akzeptanzkriterien).

---

## Build-Log

| Schritt | Status | Commit | Notiz |
|---|---|---|---|
| P0.1 — Konventions-Import | ✅ | c2d132b | `docs/CONVENTIONS.md` (17 übernommen, 13 verworfen) |
| P0.3 — Vorregistrierung | ✅ | a40e818 | `M1_PREREGISTRATION.md` FROZEN, entspricht Plan §6 |
| P0.2 Step 1 — Root-CLAUDE/INDEX/AGENTS | ✅ | 780c0cd | alle drei im selben Commit |
| P0.2 Step 2 — Gradle-Skelett | ✅ | c11ab63 | Skelett geschrieben |
| P0.2 Step 2 — Gradle-Wrapper | ✅ | 75ad958 | Wrapper 9.5.1 generiert (T-D12) |
| P0.2 Step 2 — Doc-Layers-Migration | ✅ | fabd860 | `phase0/` angelegt, `DOC_LAYERS_CONVENTION.md` |
| P0.2 Step 2 — `gradle :train-core:test` grün | ✅ | (verifiziert) | SmokeTest PASSED, `--configure-on-demand` nötig |
| P0.2 Step 3 — ROADMAP/ARCHITECTURE Stubs | ✅ | bed7b49 | beide Stubs + One-Liner in INDEX.md |
| P0.2 Step 4 — Log-Konventionen + Testmatrix | ✅ | 97b7add | Logging + Testmatrix in `docs/CONVENTIONS.md` |
| P0.4 — MC-Spike | ⏳ | — | eigener Branch `p0.4-mc-spike`, eigene Session |

---

## Session stopped — 2026-07-14 (ROADMAP + ARCHITECTURE + Log/Testmatrix)

### Completed (diese Session)
- **P0.2 Step 3 — ROADMAP.md + ARCHITECTURE.md Stubs** (Commit bed7b49): beide Root-Level-Dokumente
  mit Header-Card (≤15 Zeilen YAML), Verweis auf `TRAKTION_OVERALL_PLAN.md` als Wahrheit. ROADMAP
  enthält Phasenübersicht P0–P6 (gekürzt aus Plan §5) + Drop-Order (Plan §10). ARCHITECTURE enthält
  den Schnitt (train-core / train-mc aus Plan §1) + die zwei Ports (Plan §3.2) + Hard Rules (Plan §3).
  One-Liner für beide in `docs/INDEX.md` im selben Commit.
- **P0.2 Step 4 — Log-Konventionen + Testmatrix** (Commit 97b7add): zwei neue Abschnitte in
  `docs/CONVENTIONS.md`: "Logging" (slf4j in `train-mc`, kein `System.out`, `train-core` hat kein
  Logging-Framework, [VERIFY] Fabric-26.2-Konvention) und "Testmatrix" (Kategorie A/B-Tabelle aus
  Plan §7, Prinzipien: keine Erwartung nach dem Ergebnis, A/B getrennt, `trials.jsonl` ist
  Operator-Sache, A deterministisch, B nicht deterministisch).
- **Anti-Pattern-Check:** keine neuen Code-Dateien, nur Doku. Kein `net.minecraft.*`-Import
  möglich. Kein Verstoß. ✅
- **Drift-Beobachtung:** `TRAKTION_OVERALL_PLAN.md` §1 `down:` listet `./ROADMAP.md ·
  ./ARCHITECTURE.md` als down — die existierten bis heute nicht. Das war geplante Abhängigkeit
  (P0.2 Step 3), kein echter Drift. Jetzt sind sie da. Keine Doku-Korrektur nötig.

### Completed (vorherige Sessions, zusammengefasst)
- **P0.1** (c2d132b): Konventions-Import. `docs/CONVENTIONS.md` (17 übernommen, 13 verworfen).
- **P0.3** (a40e818): `M1_PREREGISTRATION.md` FROZEN. Entspricht Plan §6. Nicht berührt.
- **P0.2 Step 1** (780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` im selben Commit.
- **P0.2 Step 2 Skelett** (c11ab63): Gradle-Multi-Modul-Skelett geschrieben.
- **P0.2 Step 2 Wrapper** (75ad958): Gradle-Wrapper 9.5.1 generiert (T-D12).
- **P0.2 Step 2 Test grün** (verifiziert): `./gradlew :train-core:test` PASSED (SmokeTest).
- **P0.2 Step 2 Doc-Layers** (fabd860): `phase0/` angelegt, `DOC_LAYERS_CONVENTION.md`.

### Next
- **P0.2 Step 2 — `train-mc` Build-Fehler beheben:** `train-mc/build.gradle.kts` Zeile 5:
  `id("fabric-loom") version "${property("loom_version")}"` — `property()` im `plugins`-Block
  der Kotlin DSL resolved nicht (Gradle 9.5.1 Fehler). Fix: Version als String-Literal oder
  `pluginManagement` in `settings.gradle.kts`. Das blockiert `gradle :train-mc:build` (P0.2
  Akzeptanz), aber nicht `train-core`. Gehört zu P0.4-Vorbereitung.
- **P0.4 — MC-Spike:** auf eigenem Branch `p0.4-mc-spike`, eigene Session. Beantwortet T-D3
  (Token ⇄ Entity, zustandserhaltende Rekonstruktion). [VERIFY]: Java-Mod-Target, PersistentState-
  API-Name in 26.2, jqwik-Unterstützung unter Gradle 9.5.1. Recherche-Grenze für P0.4 aufgehoben.
- **P0.2 Done-When-Check:** mit Step 3+4 sind alle Doku-Steps von P0.2 erledigt. Offen bleibt nur
  der `train-mc` Build-Fehler (P0.2 Akzeptanz "train-mc:build"). Der kann in P0.4 mit gelöst werden,
  weil P0.4 ohnehin `train-mc` bauen muss.

### Open questions / blockers
- **⚠ `train-mc` Build-Fehler:** `property("loom_version")` im `plugins`-Block von
  `train-mc/build.gradle.kts` scheitert unter Gradle 9.5.1. `train-core:test` läuft nur mit
  `--configure-on-demand` (überspringt `train-mc`-Konfiguration). Fix in P0.4 oder eigener Session.
  Kein Blocker für P0.2 Step 3/4 (Doku-only), aber für P0.2 Akzeptanz "train-mc:build".
- **Yarn mappings Version [VERIFY]:** `train-mc/build.gradle.kts` verwendet `yarn:26.2+build.4:v2`.
  Build-Nummer `build.4` ist Annahme — verifizieren gegen maven.fabricmc.net, sobald `train-mc` baut.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 klärt, ob es unter
  Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(...)` ist 1.21.x-Muster.
  Bleibt [VERIFY], bis P0.4 oder P4 echte 26.2-Quellen prüft. In `docs/CONVENTIONS.md` markiert.
- **Tool-Calls:** Diese Session benutzte ~20 Tool-Calls (Lesen + Step 3 + Step 4 + Rotation).
