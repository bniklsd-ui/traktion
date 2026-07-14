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
