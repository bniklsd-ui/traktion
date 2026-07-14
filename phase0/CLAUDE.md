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

> **Status:** P0.1 ✅ · P0.3 ✅ · P0.2 Step 2 ✅ (train-core grün) · P0.2 Step 3/4 ⏳ · P0.4 ⏳
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
| P0.2 Step 3 — ROADMAP/ARCHITECTURE Stubs | ⏳ | — | ausstehend |
| P0.2 Step 4 — Log-Konventionen + Testmatrix | ⏳ | — | ausstehend |
| P0.4 — MC-Spike | ⏳ | — | eigener Branch `p0.4-mc-spike`, eigene Session |

---

## Session stopped — 2026-07-14 (Java-Setup + Wrapper + Test grün)

### Completed (diese Session)
- **Java 21 installiert (Operator):** `openjdk-21-jdk-headless` (21.0.11). `java`/`javac` jetzt
  verfügbar. Blocker aufgelöst.
- **Gradle-Wrapper generiert** (Commit 75ad958): `gradle wrapper --gradle-version 9.5.1` mit
  apt-Gradle 4.4.1 (uralt, aber Wrapper-Generierung funktioniert). `gradlew` + `gradle/wrapper/`
  committed. `gradle-wrapper.properties` zeigt auf Gradle 9.5.1 (T-D12-konform).
- **`./gradlew :train-core:test` GRÜN:** SmokeTest `harnessRuns()` PASSED. P0.2 Step 2
  Akzeptanzkriterium "grün" erfüllt. ⚠ benötigt `--configure-on-demand` (siehe Open Questions).
- **Doc-Layers-Migration** (Commit fabd860): `phase0/CLAUDE.md` + `phase0/SESSIONS_ARCHIVE.md`
  angelegt, `docs/DOC_LAYERS_CONVENTION.md` geschrieben. Session-stopped-Blöcke aus Root-CLAUDE.md
  und PHASE0_PLAN.md migriert. Erste Rotation durchgeführt (alter Block ins Archiv).
- **Anti-Pattern-Check:** `grep net.minecraft train-core/src/` → nur Kommentar in `package-info.java`.
  Kein Import. Kein Verstoß. ✅

### Completed (vorherige Sessions, zusammengefasst)
- **P0.1** (c2d132b): Konventions-Import. `docs/CONVENTIONS.md` (17 übernommen, 13 verworfen).
- **P0.3** (a40e818): `M1_PREREGISTRATION.md` FROZEN. Entspricht Plan §6. Nicht berührt.
- **P0.2 Step 1** (780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` im selben Commit.
- **P0.2 Step 2 Skelett** (c11ab63): Gradle-Multi-Modul-Skelett geschrieben.

### Next
- **P0.2 Step 2 — `train-mc` Build-Fehler beheben:** `train-mc/build.gradle.kts` Zeile 5:
  `id("fabric-loom") version "${property("loom_version")}"` — `property()` im `plugins`-Block
  der Kotlin DSL resolved nicht (Gradle 9.5.1 Fehler). Fix: Version als String-Literal oder
  `pluginManagement` in `settings.gradle.kts`. Das blockiert `gradle :train-mc:build` (P0.2
  Akzeptanz), aber nicht `train-core`. Gehört zu P0.4-Vorbereitung.
- **P0.2 Step 3:** `ROADMAP.md` + `ARCHITECTURE.md` Stubs mit Header-Card, One-Liner in `docs/INDEX.md`.
- **P0.2 Step 4:** Log-Konventionen (slf4j, nicht System.out) + Testmatrix (Kategorie A/B) in
  `docs/CONVENTIONS.md` ergänzen.
- **P0.4:** MC-Spike auf eigenem Branch `p0.4-mc-spike`. [VERIFY]: Java-Mod-Target, PersistentState-API-Name
  in 26.2, jqwik-Unterstützung unter Gradle 9.5.1.

### Open questions / blockers
- **⚠ `train-mc` Build-Fehler:** `property("loom_version")` im `plugins`-Block von
  `train-mc/build.gradle.kts` scheitert unter Gradle 9.5.1. `train-core:test` läuft nur mit
  `--configure-on-demand` (überspringt `train-mc`-Konfiguration). Fix in nächster Session oder
  P0.4. Kein Blocker für P0.2 Step 2 (`train-core` ist grün), aber für P0.2 Akzeptanz "train-mc:build".
- **Yarn mappings Version [VERIFY]:** `train-mc/build.gradle.kts` verwendet `yarn:26.2+build.4:v2`.
  Build-Nummer `build.4` ist Annahme — verifizieren gegen maven.fabricmc.net, sobald `train-mc` baut.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 klärt, ob es unter
  Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **Tool-Calls:** Diese Session benutzte ~30 Tool-Calls (Verifizierung + Migration + Java/Wrapper/Test).
