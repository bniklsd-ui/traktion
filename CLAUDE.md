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
updated: 2026-07-14
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
| **P0** | Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike | ⏳ P0.2 Step 2 (Skelett da, Test blockiert — Java 21 fehlt) |
| P1 | `train-core`: Durchstich (Z1–Z4) | ⏳ |
| P2 | Verschleiß + Ports (Z6, Z7) | ⏳ |
| P3 | Planer (Z5 — Kern-Orakel) | ⏳ |
| P4 | `train-mc`: erste spielbare Version (Z9–Z11) | ⏳ |
| P5 | Fahrplan + Lokführer (Z8) | ⏳ |
| P6 | Auswertung M1-Strang | ⏳ |

**Aktueller Schritt:** P0.2 Step 2 — Gradle-Skelett geschrieben (c11ab63), `gradle :train-core:test`
**blockiert** durch fehlendes Java 21. Skelett verifiziert 2026-07-14. Siehe `## Session stopped` unten.

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

### Completed (vorherige Sessions, zusammengefasst)
- **P0.1** (c2d132b): Konventions-Import. `docs/CONVENTIONS.md` mit "Übernommen" (17) + "Verworfen" (13).
- **P0.3** (a40e818): `M1_PREREGISTRATION.md` FROZEN. Entspricht Plan §6 vollständig. Nicht berührt.
- **P0.2 Step 1** (780c0cd): Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` im selben Commit.
- **P0.2 Step 2** (c11ab63): Gradle-Multi-Modul-Skelett geschrieben. Akzeptanz "grün" blockiert.

### Next
- **⚠ BLOCKER — Java 21 installieren (Operator):** `sudo apt-get install -y openjdk-21-jdk-headless`
  (Passwort nötig) ODER sdkman User-space-Installation. Ohne Java kein `gradle test`, kein P0.2-Abschluss,
  kein P0.4-Spike. Alternative: sdkman ist User-space (kein sudo), aber das ist eine
  Infrastruktur-Entscheidung, die den Operator betrifft — nicht stillschweigend vom Agenten gewählt.
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
  kann es nicht non-interaktiv lösen. Operator muss installieren.
- **⚠ Konventions-Unklarheit: Wo lebt der Session-stopped-Block?** Plan §11 sagt "genau ein
  `## Session stopped`-Block pro Phasen-`CLAUDE.md`". AGENTS.md/CLAUDE.md Lesereihenfolge
  erwähnt `phase<N>/CLAUDE.md` — aber `phase0/` existiert nicht. PHASE0_PLAN.md hat seinen eigenen
  Block (vom 2026-07-13). Dieser Block hier in Root-CLAUDE.md war die Übergangslösung wegen der
  Permission-Sperre. **Frage an Nikinger:** Soll der Block künftig nach `docs/plans/PHASE0_PLAN.md`
  (wo er jetzt erlaubt ist) oder in ein neu anzulegendes `phase0/CLAUDE.md`? Ich habe nichts
  migriert, weil ich keine neue Konvention erfinden will.
- **Yarn mappings Version [VERIFY]:** `train-mc/build.gradle.kts` verwendet `yarn:26.2+build.4:v2`.
  Build-Nummer `build.4` ist Annahme — verifizieren gegen maven.fabricmc.net, sobald Java läuft.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 klärt, ob es unter
  Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **Gradle-Wrapper fehlt:** `gradlew` + `gradle/wrapper/` müssen noch generiert werden.
- **Tool-Calls:** Diese Session benutzte ~16 Tool-Calls (Verifizierung, kein Code-Bau).
