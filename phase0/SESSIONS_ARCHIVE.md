---
status: archive
purpose: Archiv der alten Session-stopped-Blöcke von Phase 0. Verbatim, uneditiert, newest-first. Rotation: wenn ein neuer Block in phase0/CLAUDE.md geschrieben wird, wandert der bisher-neueste hierher.
read-when: bei Bedarf an historische Session-Details (was wurde wann gebaut, welche Blocker gab es)
detail: L3
up: ./CLAUDE.md
down:
updated: 2026-07-15
---

# Phase 0 — Sessions-Archiv

> Alte `## Session stopped`-Blöcke, aus `phase0/CLAUDE.md` rausrotiert. Verbatim, uneditiert.
> Newest-first. Keine Größenbeschränkung. Kein Editieren — sie sind Historie.
>
> Rotationsregel siehe `docs/DOC_LAYERS_CONVENTION.md`.

---

## Session stopped — 2026-07-15 (P0.4 Spike-Code + GitHub-Setup + API-Recherche)

### Completed (diese Session)
- **GitHub-Setup:** SSH-Verbindung zu `github.com:bniklsd-ui/traktion.git` hergestellt. `master` →
  `main` umbenannt, gepusht. `README.md` geschrieben (menschliche Oberfläche mit Mission, Architektur,
  Setup, Versions-Tabelle, Doku-Einstiegspunkte). `master`-Verweise in Doku auf `main` korrigiert.
- **Contributor-Attribution:** Build-Agent + Nikinger auf `README.md`, `train-mc/build.gradle.kts`,
  `settings.gradle.kts`, `gradle.properties`. Zukünftig als `Co-authored-by:`-Trailer im Commit.
- **P0.4 MC-Spike — Branch angelegt:** `p0.4-mc-spike` (wird nie gemerged).
- **P0.4 MC-Spike — API-Recherche:** `phase0/MC26_API_NOTES.md` geschrieben. Klärt:
  - Entity-Registrierung: `EntityType.Builder.of()`, `Registry.register(BuiltInRegistries.ENTITY_TYPE, ...)`
  - Entity-Persistenz: `addAdditionalSaveData(ValueOutput)` / `readAdditionalSaveData(ValueInput)` —
    nicht mehr `writeNbt`/`readNbt` mit `NbtCompound` (26.x API-Änderung)
  - Welt-attached Persistent State (T-D15): `SavedData` + `SavedDataType` + `level.getDataStorage()`
  - `ValueInput.getDoubleOr(String, double)` statt `getDouble(String).orElse(...)` (aus JAR verifiziert)
  - `Identifier` statt `ResourceLocation` in 26.2 (aus JAR verifiziert)
- **P0.4 MC-Spike — Code geschrieben:** `PathEntity.java` (Entity folgt hartkodiertem Quadrat-Pfad,
  speichert `pathProgress` in `ValueOutput`/`ValueInput`), `SpikeModInitializer.java` (Registrierung),
  `fabric.mod.json` (angepasst für Spike, Java 25, Entrypoint).
- **P0.4 MC-Spike — Build grün:** `gradle :train-mc:build` BUILD SUCCESSFUL. Spike-Code kompiliert
  gegen MC 26.2 (unobfuskiert, Mojang-Namen direkt).
- **Anti-Pattern-Check:** Spike-Code ist in `train-mc` (nicht `train-core`). Kein Verstoß. ✅

### Completed (vorherige Sessions, zusammengefasst)
- **P0.1** (c2d132b): Konventions-Import. `docs/CONVENTIONS.md` (17 übernommen, 13 verworfen).
- **P0.3** (a40e818): `M1_PREREGISTRATION.md` FROZEN. Entspricht Plan §6. Nicht berührt.
- **P0.2 Step 1** (780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` im selben Commit.
- **P0.2 Step 2 Skelett** (c11ab63): Gradle-Multi-Modul-Skelett geschrieben.
- **P0.2 Step 2 Wrapper** (75ad958): Gradle-Wrapper 9.5.1 generiert (T-D12).
- **P0.2 Step 2 Test grün** (verifiziert): `./gradlew :train-core:test` PASSED (SmokeTest).
- **P0.2 Step 2 Doc-Layers** (fabd860): `phase0/` angelegt, `DOC_LAYERS_CONVENTION.md`.
- **P0.2 Step 3** (bed7b49): ROADMAP.md + ARCHITECTURE.md Stubs.
- **P0.2 Step 4** (97b7add): Log-Konventionen + Testmatrix in `docs/CONVENTIONS.md`.
- **P0.2 Step 2 Build-Fix** (0c32c06): train-mc Build-Fix (non-remap Loom, Java 25, keine Mappings).

### Next
- **P0.4 MC-Spike — Smoke-Test (Operator):** `./gradlew :train-mc:runClient` (oder äquivalent) im
  Spike-Branch starten. Prüfen:
  1. Entity fährt entlang hartkodiertem Quadrat-Pfad im Client sichtbar?
  2. Entity despawnt bei Spielerentfernung (Chunk-Unload)?
  3. Entity wird bei Annäherung zustandserhaltend rekonstruiert (Position auf Pfad stimmt)?
  4. Antwort auf T-D3: ja oder nein. Wenn nein: sofort melden.
- **P0.4 [VERIFY]-Fragen:** PersistentState-API-Name geklärt (SavedData, siehe MC26_API_NOTES.md).
  jqwik noch offen. Java-Version geklärt (Java 25).
- **Nach P0.4:** P0 ist abgeschlossen, wenn der Smoke-Test T-D3 bestätigt. Dann P1 (train-core
  Durchstich) in neuer Session.

### Open questions / blockers
- **P0.4 Smoke-Test offen:** Code kompiliert und baut, aber nicht im Client getestet. Operator muss
  `./gradlew :train-mc:runClient` ausführen und die drei Akzeptanzkriterien prüfen.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 oder P1 klärt, ob es
  unter Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(...)` wird im Spike
  verwendet. Bleibt [VERIFY], bis P4 echte 26.2-Quellen prüft.
- **`.opencode/agents/build-traktion.md` uncommitted:** Permission-Änderung (deny→ask) durch
  Operator, nicht durch Agent. Unangetastet gelassen.
- **Tool-Calls:** Diese Session benutzte ~30 Tool-Calls (GitHub-Setup + Recherche + Spike-Code).

---

## Session stopped — 2026-07-15 (train-mc Build-Fix + Mappings-Recherche)

### Completed (diese Session)
- **P0.2 Step 2 — `train-mc` Build-Fehler behoben:** `gradle :train-mc:build` BUILD SUCCESSFUL.
  Der Fehler hatte mehrere Schichten, die nacheinander gelöst wurden:
  1. `property("loom_version")` im `plugins`-Block → String-Literal `"1.16.3"` (Kotlin-DSL-Constraint).
  2. Loom 1.17 hat keine Maven-Artefakte (POM 404 verifiziert) → auf 1.16.3 ausgewichen (neueste
     Maven-verfügbare Version, Gradle-9.5-kompatibel).
  3. `expand("version": ...)` war Groovy-Syntax → `expand(mapOf("version" to ...))` (Kotlin DSL).
  4. MC 26.2 erfordert Java 25 (Loom-Fehlermeldung), nicht Java 21 → Operator hat JDK 25 installiert,
     `java_mod_target=25` in `gradle.properties`, Toolchain-Überschreibung in `train-mc/build.gradle.kts`.
  5. Yarn-Mappings für 26.2 fehlen (Meta-API leer) → Mojmap versucht → `officialMojangMappings()`
     scheitert (kein `client_mappings` im Mojang-Manifest).
  6. **Recherche-Auftrag an externes Modell** (Nikinger) → Ergebnis in
     `phase0/Fabric_Loom_Mappings_Fix_01.md`: MC 26.x ist **unobfuskiert** (seit 26.1), Yarn/Mojmap
     sind obsolet. Fix: non-remap Plugin-ID `net.fabricmc.fabric-loom`, Mappings-Zeile komplett
     entfernen, `modImplementation`→`implementation`.
  7. Umsetzung: `settings.gradle.kts` (resolutionStrategy auf `net.fabricmc.fabric-loom`),
     `train-mc/build.gradle.kts` (Plugin-ID, keine Mappings, `implementation`), `gradle.properties`
     (Kommentare mit Verifikationsstand).
- **`gradle :train-core:test` weiterhin grün** (SmokeTest PASSED, UP-TO-DATE).
- **Doku nachgezogen:** `docs/plans/PHASE0_PLAN.md` T-D12/T-D17 + Verifizierte-Versionen-Tabelle
  aktualisiert. `gradle.properties` Kommentare. `phase0/Fabric_Loom_Mappings_Fix_01.md` mit
  Header-Card versehen, One-Liner in `docs/INDEX.md`.
- **Anti-Pattern-Check:** keine `net.minecraft.*`-Importe in `train-core`. Kein Verstoß. ✅

### Completed (vorherige Sessions, zusammengefasst)
- **P0.1** (c2d132b): Konventions-Import. `docs/CONVENTIONS.md` (17 übernommen, 13 verworfen).
- **P0.3** (a40e818): `M1_PREREGISTRATION.md` FROZEN. Entspricht Plan §6. Nicht berührt.
- **P0.2 Step 1** (780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` im selben Commit.
- **P0.2 Step 2 Skelett** (c11ab63): Gradle-Multi-Modul-Skelett geschrieben.
- **P0.2 Step 2 Wrapper** (75ad958): Gradle-Wrapper 9.5.1 generiert (T-D12).
- **P0.2 Step 2 Test grün** (verifiziert): `./gradlew :train-core:test` PASSED (SmokeTest).
- **P0.2 Step 2 Doc-Layers** (fabd860): `phase0/` angelegt, `DOC_LAYERS_CONVENTION.md`.
- **P0.2 Step 3** (bed7b49): ROADMAP.md + ARCHITECTURE.md Stubs.
- **P0.2 Step 4** (97b7add): Log-Konventionen + Testmatrix in `docs/CONVENTIONS.md`.

### Next
- **P0.2 Done-When-Check:** `gradle :train-mc:build` ist jetzt grün. P0.2-Akzeptanz "train-mc:build"
  erfüllt. P0.2 ist damit vollständig abgeschlossen (alle Steps ✅, Build grün).
- **P0.4 — MC-Spike:** auf eigenem Branch `p0.4-mc-spike`, eigene Session. Beantwortet T-D3
  (Token ⇄ Entity, zustandserhaltende Rekonstruktion). `train-mc` baut jetzt — Voraussetzung erfüllt.
  Offene [VERIFY]-Fragen: PersistentState-API-Name in 26.2, jqwik-Unterstützung unter Gradle 9.5.1.
  Recherche-Grenze für P0.4 aufgehoben.

### Open questions / blockers
- **PersistentState-API-Name [VERIFY]:** T-D15 verweist auf "welt-attached Persistent State pro
  Dimension". Der API-Name in 26.2 ist noch ungeprüft (26.1 hat das Welt-Datenformat geändert).
  P0.4 muss es gegen echte 26.2-Quellen verifizieren. Das Recherche-Dokument bestätigt die 26.1-
  Änderung ("Tiny Takeover"), aber nicht den spezifischen API-Namen.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 klärt, ob es unter
  Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(...)` ist 1.21.x-Muster.
  Bleibt [VERIFY], bis P0.4 oder P4 echte 26.2-Quellen prüft. In `docs/CONVENTIONS.md` markiert.
- **`.opencode/agents/build-traktion.md` uncommitted:** Permission-Änderung (deny→ask) durch
  Operator, nicht durch Agent. Unangetastet gelassen.
- **Tool-Calls:** Diese Session benutzte ~25 Tool-Calls (Build-Fix + Recherche + Doku).

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
