---
status: live
purpose: Single Source of Truth für Traktion. Projekt-Identität, Phasenstatus, Einstiegspunkte. Jede Session liest dies zuerst.
read-when: Session-Start; vor jeder Architektur-Entscheidung; bei Zweifel, was Wahrheit ist
detail: L1
up: (root)
down:
  - ./TRAKTION_OVERALL_PLAN.md   # Wahrheit — Mission, Locks, Hard Rules, Phasen, Ziele
  - ./docs/INDEX.md              # Karte — eine Zeile pro Doku
  - ./docs/CONVENTIONS.md        # übernommene/verworfene Konventionen
  - ./docs/plans/PHASE0_PLAN.md  # aktueller Phasenplan
updated: 2026-07-13
---

# Traktion — Single Source of Truth

> **Fabric-Mod.** Ein Zugnetz ist kein Fahrzeug, sondern ein Betrieb.
> Wahrheit steht in `TRAKTION_OVERALL_PLAN.md`. Diese Datei ist der Einstieg dorthin.

---

## Was das ist

Traktion simuliert ein elektrifiziertes Schienennetz, das der Spieler plant, baut und am Leben
hält. Züge sind Verbraucher an einem Netz. Der Reiz: ein Zug fährt, **weil ein System existiert**,
das ihn fahren lässt — und wird langsamer, wenn das System schlampig gebaut ist.

**Teil 1 (dieser Plan):** Zugnetz — Graph, Physik, Energie, Verschleiß, Planer, Fahrplan, Leitstand.
**Teil 2 (geparkt, §8):** Industrie — Kraftwerke, Fabriken, Arbeiter-NPC, Fracht.

---

## Architektur-Schnitt (Plan §1)

```
train-core/   reines Java. Kein Fabric, kein net.minecraft.*, kein NBT.
              Graph · Physik · Energie · Verschleiß · Planer · Simulation · Fahrplan
              → gradle :train-core:test läuft in Sekunden, ohne Client-Start.

train-mc/     dünner Adapter. Blöcke, Entities, Rendering, Packets, Persistenz, GUI.
              → übersetzt Weltzustand in Zahlen und Zahlen zurück in Weltzustand.
```

**Wenn du in `train-core` ein `net.minecraft.*`-Import brauchst, ist der Schnitt falsch. Stopp.**

---

## Phasenstatus

| Phase | Inhalt | Status |
|---|---|---|
| **P0** | Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike | ⏳ P0.2 Step 2 (Skelett geschrieben, Test blockiert — Java fehlt) |
| P1 | `train-core`: Durchstich (Z1–Z4) | ⏳ |
| P2 | Verschleiß + Ports (Z6, Z7) | ⏳ |
| P3 | Planer (Z5 — Kern-Orakel) | ⏳ |
| P4 | `train-mc`: erste spielbare Version (Z9–Z11) | ⏳ |
| P5 | Fahrplan + Lokführer (Z8) | ⏳ |
| P6 | Auswertung M1-Strang | ⏳ |

**Aktueller Schritt:** P0.2 Step 2 — Gradle-Skelett geschrieben (c11ab63), `gradle :train-core:test`
**blockiert** durch fehlendes Java 21. Siehe `## Session stopped` unten und `docs/plans/PHASE0_PLAN.md`.

---

## Lesereihenfolge für jede Session

1. `AGENTS.md` — harness-neutraler Einstieg
2. `docs/INDEX.md` — Karte. Navigation über up/down-Links, nicht per Verzeichnissuche.
3. `CLAUDE.md` — diese Datei (Single Source of Truth)
4. `docs/plans/PHASE<N>_PLAN.md` — aktueller Phasenplan, insbesondere `## Session stopped`
5. `TRAKTION_OVERALL_PLAN.md` — §2 (Locks), §3 (Hard Rules), §4 (Ziele), §9 (Anti-Patterns)

Referenzierte Dateien werden **nicht** automatisch geladen. Öffne sie on-need-to-know selbst.

---

## Harte Grenzen (Plan §3, gekürzt — im Zweifel dort nachlesen)

- Kein `net.minecraft.*`, kein NBT, kein `ItemStack` in `train-core`.
- Die Physikformel existiert genau **EINMAL**. Planer und Simulator rufen dieselbe Funktion.
- Der Planer ruft **NIE** den Simulator auf. Sonst ist Z5 tautologisch.
- Fixed dt, geordnete Iteration, gesäter Zufall. Kein Wall-Clock, kein `HashSet` in der Physikschleife.
- Kein Interface ohne zwei heute benennbare Implementierungen.
- Kein roher OpenGL-Call. Nur Blaze3D.
- Kein Softlock. Aus jedem erreichbaren Zustand muss der Spieler durch eigene Arbeit herauskommen.

---

## Arbeitsweise

- **Code ist Wahrheit** > Konzept-Dokument > Status-Prosa. Bei Drift: Code vertrauen, Doku fixen.
- **TDD in `train-core`.** Ein Subtask ist nicht fertig, bevor `gradle :train-core:test` grün ist.
- **Atomare Commits**, Format `<scope>: <imperative>`.
- **Commit ⇒ Note-Update:** Statuszeile + `## Session stopped` im selben Commit.
- **Neue `.md` ⇒** L1-Header-Card + One-Liner in `docs/INDEX.md` im selben Commit.
- **`[VERIFY]` bleibt stehen**, bis jemand tatsächlich verifiziert hat.
- **Handover nach ~20–30 Tool-Calls** via `## Session stopped`, dann anhalten.

---

## Anti-Patterns (Plan §9 — sofort stoppen und diskutieren)

- `net.minecraft.*`- oder NBT-Import in `train-core`
- Die Physik-Formel an zwei Stellen (Regel 2)
- Der Planer ruft den Simulator auf (T-D14, Z5 tautologisch)
- Variable Schrittweite, Wall-Clock, `HashSet`-Iteration in der Physikschleife (Regel 8)
- Ein Interface ohne zwei heute benennbare Implementierungen
- Ein Weltzustand, aus dem der Spieler nicht herauskommt (Regel 4)
- Zeitbasierter Verfall (Regel 5) · ein `ItemStack` im Kern (Regel 7)
- Ein roher OpenGL-Call (T-D16)
- Eine Phase ohne `CLAUDE.md` als allererste Aktion
- Eine Session ohne `## Session stopped`-Block

Stößt du auf eines — auch im eigenen Entwurf: **anhalten, benennen, fragen.** Nicht stillschweigend
korrigieren, nicht umgehen. Diese Momente sind Messpunkte.

---

## Session stopped — 2026-07-14

### Completed
- **P0.2 Step 1** (Commit 780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · vollständiges `AGENTS.md`
  erstellt. Alle drei im selben Commit. `docs/INDEX.md` listet alle bisherigen .md-Dateien mit
  Glyph + read-when-Hook. Header-Cards ≤15 Zeilen (CLAUDE.md: 12, INDEX.md: 7).
- **P0.2 Step 2 — Skelett geschrieben** (Commit c11ab63): `settings.gradle.kts` (inkludiert
  `train-core` + `train-mc`), `build.gradle.kts` (Root, gemeinsame Java-21-Toolchain + Repos),
  `gradle.properties` (alle T-D12-Versionen gepinnt), `train-core/build.gradle.kts` (plain Java,
  JUnit 5, NULL weitere Abhängigkeiten, jqwik auskommentiert als [VERIFY]), `train-mc/build.gradle.kts`
  (Fabric Loom 1.17, MC 26.2, Yarn mappings, fabric-api), `train-core` Package-Root + Smoke-Test,
  `train-mc` Package-Root + `fabric.mod.json`-Stub.
- **Anti-Pattern-Check:** `grep net.minecraft train-core/src/` → nur Kommentar in `package-info.java`
  ("Kein Fabric, kein net.minecraft.*..."). Kein Import. Kein Verstoß.

### Next
- **BLOCKER — Java 21 fehlt:** `java`/`javac` sind nicht installiert (`command not found`).
  Kein JDK-Package, kein `/usr/lib/jvm`, kein sdkman/asdf. Node 24 ist da (via nvm).
  → `gradle :train-core:test` kann nicht laufen. Akzeptanzkriterium "grün" ist **blockiert**.
  **Operator muss Java 21 installieren** (z.B. `sdkman install java 21-tem` oder
  `apt install openjdk-21-jdk`). Danach: `./gradlew :train-core:test` ausführen.
  Gradle-Wrapper (`gradlew`) muss noch generiert werden (oder `gradle` direkt, falls installiert).
- **P0.2 Step 2 — Rest:** Gradle-Wrapper (`gradlew` + `gradle/wrapper/`) fehlt noch. Nach
  Java-Installation: `gradle wrapper --gradle-version 9.5.1` ausführen, dann `./gradlew :train-core:test`.
- **P0.2 Step 3:** `ROADMAP.md` + `ARCHITECTURE.md` Stubs mit Header-Card, One-Liner in `docs/INDEX.md`.
- **P0.2 Step 4:** Log-Konventionen (slf4j, nicht System.out) + Testmatrix (Kategorie A/B) in
  `docs/CONVENTIONS.md` ergänzen.
- **P0.4:** MC-Spike auf eigenem Branch `p0.4-mc-spike`. [VERIFY]: Java-Mod-Target, PersistentState-API-Name
  in 26.2, jqwik-Unterstützung unter Gradle 9.5.1.

### Open questions / blockers
- **⚠ BLOCKER: Java 21 nicht installiert.** Siehe "Next" oben. Ohne Java kein `gradle test`, kein
  P0.2-Abschluss, kein P0.4-Spike. Das ist Infrastruktur (zählt nicht gegen H1, Plan §6), aber es
  blockiert die Ausführung.
- **⚠ Widerspruch: Session-stopped-Block vs. Permission-Sperre.** Plan §11 sagt "Commit ⇒ Note-Update:
  Statuszeile + `## Session stopped` im selben Commit" und "genau ein `## Session stopped`-Block pro
  Phasen-`CLAUDE.md`". Die Permission-Config sperrt `edit: docs/plans/**` für den Executor (Preregistration
  §5: Executor darf Pläne nicht ändern — beabsichtigt). PHASE0_PLAN.md liegt in `docs/plans/`. **Ich kann
  den Session-stopped-Block dort nicht schreiben.** Übergangslösung: Block in `CLAUDE.md` (Root), das ich
  editieren darf. **Operator-Entscheidung nötig:** Soll der Plan-Agent den PHASE0_PLAN.md aktualisieren
  (er hat `edit: docs/plans/**` erlaubt), oder soll die Permission-Regel gelockert werden, oder soll der
  Session-stopped-Block dauerhaft in CLAUDE.md leben?
- **Yarn mappings Version [VERIFY]:** `train-mc/build.gradle.kts` verwendet `yarn:26.2+build.4:v2`.
  Die Build-Nummer `build.4` ist eine Annahme — muss gegen maven.fabricmc.net verifiziert werden, sobald
  Java läuft und `gradle :train-mc:build` ausgeführt wird.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 klärt, ob es unter Gradle
  9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **Gradle-Wrapper fehlt:** `gradlew` + `gradle/wrapper/` müssen noch generiert werden (nach Java-Installation).
- **Tool-Calls:** Diese Session benutzte ~24 Tool-Calls.
