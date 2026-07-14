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

> **Status:** P0.1 ✅ · P0.3 ✅ · P0.2 ⏳ (Skelett da, Test blockiert) · P0.4 ⏳
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
| P0.2 Step 2 — Gradle-Skelett | ⏳ | c11ab63 | Skelett geschrieben, `gradle test` blockiert (Java 21 fehlt) |
| P0.2 Step 2 — Doc-Layers-Migration | ✅ | (dieser Commit) | `phase0/` angelegt, Blöcke migriert, `DOC_LAYERS_CONVENTION.md` geschrieben |
| P0.2 Step 3 — ROADMAP/ARCHITECTURE Stubs | ⏳ | — | ausstehend |
| P0.2 Step 4 — Log-Konventionen + Testmatrix | ⏳ | — | ausstehend |
| P0.4 — MC-Spike | ⏳ | — | eigener Branch `p0.4-mc-spike`, eigene Session |

---

## Session stopped — 2026-07-14 (Verifizierungs-Session)

### Completed (diese Session)
- **Stand verifiziert, kein Code gebaut.** Lesereihenfolge komplett durchlaufen: AGENTS.md →
  docs/INDEX.md → CLAUDE.md → PHASE0_PLAN.md → TRAKTION_OVERALL_PLAN.md §2/§3/§4/§9.
- **Skelett gegen Doku abgeglichen** — alle Dateien aus P0.2 Step 2 (Commit c11ab63) physisch
  vorhanden und gelesen: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`,
  `train-core/build.gradle.kts`, `train-mc/build.gradle.kts`, `train-core/src/.../package-info.java`,
  `train-core/src/test/.../SmokeTest.java`, `train-mc/src/.../package-info.java`,
  `train-mc/src/main/resources/fabric.mod.json`. Skelett ist sauber.
- **Anti-Pattern-Check wiederholt:** `grep net.minecraft train-core/src/` → nur Kommentar in
  `package-info.java`. Kein Import. Kein Verstoß. ✅
- **Java-21-Blocker bestätigt:** `java`/`javac` nicht installiert. Kein `/usr/lib/jvm`, kein
  sdkman, kein asdf. `openjdk-21-jdk-headless` ist im apt-cache verfügbar, User `traktion` ist in
  `sudo`-Gruppe, aber `sudo` braucht Passwort (non-interaktiv nicht lösbar). **Operator-Action nötig.**
- **Permission-Änderung verifiziert:** `.opencode/agents/build-traktion.md` hat uncommitted Diff
  (deny→ask für `docs/plans/**`, `M1_*.md`, `TRAKTION_OVERALL_PLAN.md`, `m1/**`). Operator hat das
  gemacht, ich lasse es unangetastet.
- **Doc-Layers-Migration durchgeführt:** `phase0/CLAUDE.md` + `phase0/SESSIONS_ARCHIVE.md` angelegt,
  `docs/DOC_LAYERS_CONVENTION.md` geschrieben. Session-stopped-Block aus Root-CLAUDE.md hierher
  migriert. Alter Block aus PHASE0_PLAN.md ins Archiv verschoben. Root-CLAUDE.md schlank gehalten.

### Completed (vorherige Sessions, zusammengefasst)
- **P0.1** (c2d132b): Konventions-Import. `docs/CONVENTIONS.md` mit "Übernommen" (17) + "Verworfen" (13).
- **P0.3** (a40e818): `M1_PREREGISTRATION.md` FROZEN. Entspricht Plan §6 vollständig. Nicht berührt.
- **P0.2 Step 1** (780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` im selben Commit.
- **P0.2 Step 2** (c11ab63): Gradle-Multi-Modul-Skelett geschrieben. Akzeptanz "grün" blockiert.

### Next
- **⚠ BLOCKER — Java 21 installieren (Operator):** `sudo apt-get install -y openjdk-21-jdk-headless`
  (Passwort nötig) ODER sdkman User-space-Installation (Operator hat freigegeben). Ohne Java kein
  `gradle test`, kein P0.2-Abschluss, kein P0.4-Spike.
- **P0.2 Step 2 — Rest nach Java:** Gradle-Wrapper (`gradlew` + `gradle/wrapper/`) fehlt. Nach
  Java-Installation: `gradle wrapper --gradle-version 9.5.1` (braucht `gradle` auf PATH) ODER
  Wrapper manuell anlegen. Dann `./gradlew :train-core:test` — muss grün sein (Smoke-Test).
- **P0.2 Step 3:** `ROADMAP.md` + `ARCHITECTURE.md` Stubs mit Header-Card, One-Liner in `docs/INDEX.md`.
- **P0.2 Step 4:** Log-Konventionen (slf4j, nicht System.out) + Testmatrix (Kategorie A/B) in
  `docs/CONVENTIONS.md` ergänzen.
- **P0.4:** MC-Spike auf eigenem Branch `p0.4-mc-spike`. [VERIFY]: Java-Mod-Target, PersistentState-API-Name
  in 26.2, jqwik-Unterstützung unter Gradle 9.5.1.

### Open questions / blockers
- **⚠ BLOCKER: Java 21 nicht installiert.** Siehe "Next" oben. `sudo` braucht Passwort — Agent
  kann es nicht non-interaktiv lösen. Operator hat sdkman User-space-Installation freigegeben.
- **Yarn mappings Version [VERIFY]:** `train-mc/build.gradle.kts` verwendet `yarn:26.2+build.4:v2`.
  Build-Nummer `build.4` ist Annahme — verifizieren gegen maven.fabricmc.net, sobald Java läuft.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 klärt, ob es unter
  Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **Gradle-Wrapper fehlt:** `gradlew` + `gradle/wrapper/` müssen noch generiert werden.
- **Tool-Calls:** Diese Session benutzte ~22 Tool-Calls (Verifizierung + Doc-Layers-Migration).
