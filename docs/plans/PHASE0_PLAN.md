---
status: plan (ausführungsreif für P0.2–P0.4)
purpose: Phasenplan für P0 — Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike. Architektur-Entscheidungen, Schritt-Sequenz, Testlisten, Akzeptanzkriterien.
read-when: Ausführung von P0.2–P0.4; Referenz für zukünftige Sessions
detail: L2
up: ./TRAKTION_OVERALL_PLAN.md
down:
  - ./docs/CONVENTIONS.md          # P0.1-Artefakt — übernommene/verworfene Konventionen
  - ./M1_PREREGISTRATION.md         # P0.3-Artefakt — FROZEN, nie editieren
updated: 2026-07-13
---

# Phase 0 — Fundament, Konventions-Import & Messinstrument

> **Status:** P0.1 ✅ erledigt (c2d132b) · P0.3 ✅ erledigt (a40e818, geprüft) · P0.2 ⏳ ausstehend · P0.4 ⏳ ausstehend
>
> **Ausnahme P0:** Planung UND Ausführung in derselben Session. Ab P1 wieder getrennt
> (Planungsagent schreibt PHASE<N>_PLAN.md, Build-Agent führt aus).

---

## Architektur-Entscheidungen dieser Phase

| # | Thema | Lock | Status |
|---|---|---|---|
| **T-D12** | Zielversion + Pinning | Minecraft 26.2, Fabric. Gepinnt: fabric-api 0.154.0+26.2 · loader 0.19.3 · loom 1.17 · gradle 9.5.1 · Java 21 (Build-Toolchain). Kein Update-Chasing während M1. | ✅ verifiziert 2026-07-13 |
| **T-D17** | Build-Toolchain Java | Java 21 für Gradle/Loom (Loom 1.11+ erfordert Java 21). Mod-Target-Java [VERIFY in P0.4] — MC 26.2 erfordert vermutlich 21+. | ✅ Build-Java gelöst · Mod-Java offen |
| **T-D18** | Modul-Layout | Gradle-Multi-Modul: `train-core` (plain Java, null Abhängigkeiten außer Test-Libs) + `train-mc` (Fabric Loom). Root-`settings.gradle.kts` inkludiert beide. | gelockt |
| **T-D19** | Property-Testing | jqwik [VERIFY in P0.4 — läuft unter Gradle 9.5.1?]. Fallback: JUnit 5 + eigene Generatoren. Entscheidung in P0.4. | offen |

---

## Verifizierte Versionen (Stand 2026-07-13)

| Komponente | Wert | Verifiziert gegen |
|---|---|---|
| Minecraft | 26.2 (stable) | meta.fabricmc.net/v2/versions/game |
| fabric-api | 0.154.0+26.2 | maven.fabricmc.net maven-metadata (existiert, neuere 0.154.x+26.2 verfügbar — Pin bleibt) |
| fabric-loader | 0.19.3 (stable) | meta.fabricmc.net/v2/versions/loader |
| fabric-loom | 1.17 (latest) | github.com/FabricMC/fabric-loom/releases |
| gradle | 9.5.1 | Loom 1.17 changelog: "Update to Gradle 9.5" |
| java (build) | 21 | Loom 1.11+ requires Java 21 |
| java (mod target) | [VERIFY] | offen — P0.4 klärt es |

---

## Schritt-Sequenz

### Step 0 — Altlasten (Namensdrift, tote Verweise)

**Status:** Keine Altlasten. Repo ist frisch (799f103 skeleton, 8 Commits insgesamt).
- Kein Code existiert → keine Namensdrift möglich.
- Keine toten Verweise → keine Doku verweist auf noch nicht existierenden Code.
- `example_project/` wurde vom Operator in den Operator-Bereich verschoben (nicht mehr im Repo). `.gitignore`-Eintrag (Zeile 1) bleibt als Belt-and-Suspenders. Kein Agent liest es — es ist physisch nicht mehr da.

**Akzeptanz:** Step 0 ist leer. Darf leer sein, nicht fehlen. ✅

---

### Step 0b — Doc-Drift

**Status:** Keine Doc-Drift. Repo ist frisch.
- `TRAKTION_OVERALL_PLAN.md` ist die Wahrheit, alle Doku verweist darauf.
- `docs/CONVENTIONS.md` (P0.1) verweist korrekt auf `./CLAUDE.md` (up) — diese Datei existiert noch nicht, wird in P0.2 Step 1 erstellt. Das ist kein Drift, sondern geplante Abhängigkeit.
- `M1_PREREGISTRATION.md` verweist auf `./TRAKTION_OPENCODE_CONFIG.md` (related) — [VERIFY: existiert diese Datei? Sie ist nicht im Repo-Root sichtbar. Möglicherweise in den Operator-Bereich verschoben (Commit a0cc00d). Kein Drift im Plan-Sinn — die Datei ist Operator-Infrastruktur, nicht Build-Wahrheit.]

**Akzeptanz:** Step 0b ist leer (bis auf die genannte [VERIFY]-Notiz). ✅

---

### Step 1 — Root-CLAUDE.md · docs/INDEX.md · AGENTS.md (ALLERERSTE Aktion)

> Plan §5/P0.2: "Root-CLAUDE.md · docs/INDEX.md · AGENTS.md (harness-neutraler Einstieg) — erste Aktion"
> Plan §11: "CLAUDE.md als erste Aktion jeder Phase — nie nachgelagert"
> Plan §9 Anti-Pattern: "Eine Phase ohne CLAUDE.md als allererste Aktion"

**Dateien:**
- `CLAUDE.md` (Root) — Header-Card + Projekt-Identität + aktueller Phasenstatus + Verweis auf TRAKTION_OVERALL_PLAN.md + docs/INDEX.md + docs/CONVENTIONS.md
- `docs/INDEX.md` — L0-Karte, eine Zeile pro Doku, Glyph + read-when-Hook
- `AGENTS.md` — vollständige Version (ersetzt 3-Zeilen-Platzhalter von 4f2d9d4). Harness-neutraler Einstieg: Pointer auf CLAUDE.md + Doc-Layers-Navigation + "@ref on need-to-know"-Anweisung

**Typnamen:** keine (docs-only)

**Testliste:** keine (docs-only)

**Akzeptanzkriterien:**
- [ ] `CLAUDE.md` existiert mit Header-Card (≤15 Zeilen YAML)
- [ ] `docs/INDEX.md` existiert, listet alle bisherigen .md-Dateien
- [ ] `AGENTS.md` ist vollständig (nicht mehr 3 Zeilen)
- [ ] Alle drei im selben Commit
- [ ] `docs/INDEX.md` hat One-Liner für `docs/CONVENTIONS.md` (P0.1-Artefakt nachtragen)

**Zielzuordnung:** — (Infrastruktur, kein Z-Ziel)
**Kategorie:** — (Infrastruktur)

---

### Step 2 — Gradle-Multi-Modul-Skelett

> Plan §5/P0.2: "Gradle-Multi-Modul: train-core (plain Java) + train-mc (Fabric Loom 1.17)"
> Plan §1: "train-core: reines Java. Kein Fabric, kein net.minecraft.*, kein NBT."
> Plan §3 Regel 1: "Kein net.minecraft.*, kein NBT in train-core."

**Dateien:**
- `settings.gradle.kts` — inkludiert `train-core` und `train-mc`
- `build.gradle.kts` (Root) — gemeinsame Konfiguration, Plugin-Management mit Loom
- `gradle.properties` — gepinnte Versionen (T-D12)
- `train-core/build.gradle.kts` — plain Java, JUnit 5, jqwik [VERIFY], NULL weitere Abhängigkeiten
- `train-mc/build.gradle.kts` — Fabric Loom, Minecraft 26.2, fabric-api, loader 0.19.3
- `train-core/src/main/java/de/traktion/traincore/` — Package-Root (leer, mit package-info.java)
- `train-core/src/test/java/de/traktion/traincore/` — Test-Root (leer)
- `train-mc/src/main/java/de/traktion/trainmc/` — Package-Root (leer, mit package-info.java)
- `train-mc/src/main/resources/fabric.mod.json` — Mod-Manifest (Stub)
- `train-mc/src/main/resources/<modid>.mixins.json` — Mixin-Config (Stub, falls erforderlich [VERIFY])

**Typnamen:** keine in P0 (nur Skelett, kein Domänencode)

**Testliste:**
- `train-core`: leerer Smoke-Test (z.B. `assertTrue(true)`) — beweist, dass `gradle :train-core:test` läuft
- `train-mc`: kein Test in P0 (Client-Start ist kein Unit-Test)

**Akzeptanzkriterien:**
- [ ] `gradle :train-core:test` läuft und ist grün (leerer Smoke-Test)
- [ ] `gradle :train-mc:build` läuft (Loom lädt MC 26.2, decompiled — [VERIFY: dauert beim ersten Mal lange])
- [ ] `train-core/build.gradle.kts` hat keine Abhängigkeit außer `testImplementation` für JUnit/jqwik
- [ ] `train-core` hat keinen `net.minecraft.*`-Import (Anti-Pattern-Check: `grep -r "net.minecraft" train-core/src/` → leer)
- [ ] `gradle.properties` enthält alle gepinnten Versionen aus T-D12

**Zielzuordnung:** — (Infrastruktur, kein Z-Ziel)
**Kategorie:** — (Infrastruktur)

---

### Step 3 — ROADMAP.md · ARCHITECTURE.md (Stubs)

> Plan §5/P0.2: "ROADMAP.md, ARCHITECTURE.md (Stubs, zeigen auf dieses Dokument)"

**Dateien:**
- `ROADMAP.md` — Header-Card + Phasenübersicht P0–P6 (Tabelle aus Plan §5, gekürzt) + Verweis auf TRAKTION_OVERALL_PLAN.md
- `ARCHITECTURE.md` — Header-Card + Architektur-Schnitt (train-core / train-mc aus Plan §1) + Verweis auf TRAKTION_OVERALL_PLAN.md

**Typnamen:** keine (docs-only)

**Testliste:** keine (docs-only)

**Akzeptanzkriterien:**
- [ ] Beide Dateien existieren mit Header-Card
- [ ] Beide verweisen auf TRAKTION_OVERALL_PLAN.md als Wahrheit
- [ ] Beide haben One-Liner in `docs/INDEX.md` (im selben Commit)

**Zielzuordnung:** — (Infrastruktur)
**Kategorie:** — (Infrastruktur)

---

### Step 4 — Log-Konventionen + Testmatrix festgeschrieben

> Plan §5/P0.2: "Log-Konventionen + Testmatrix festgeschrieben (§7)"
> Plan §7: "slf4j, nie System.out [VERIFY Fabric-Konvention 26.2]"

**Dateien:**
- `docs/CONVENTIONS.md` — ergänzt um Abschnitt "Logging" (slf4j, nicht System.out) und "Testmatrix" (Kategorie A/B aus Plan §7)
- ODER separate Datei `docs/TEST_MATRIX.md` [VERIFY — Entscheidung bei Ausführung]

**Typnamen:** keine (docs-only)

**Testliste:** keine (docs-only)

**Akzeptanzkriterien:**
- [ ] Logging-Konvention festgeschrieben: slf4j in `train-mc`, kein `System.out` (Plan §7)
- [ ] Testmatrix festgeschrieben: Kategorie A (`train-core`, `gradle :train-core:test` grün) vs. Kategorie B (`train-mc`, manueller Smoke)
- [ ] Metriken je Kategorie benannt (Plan §7)

**Zielzuordnung:** — (Infrastruktur)
**Kategorie:** — (Infrastruktur)

---

### P0.3 — Vorregistrierung (ERLEDIGT)

**Status:** ✅ erledigt (Commit a40e818, 2026-07-13)

`M1_PREREGISTRATION.md` existiert und entspricht Plan §6 vollständig:
- ✅ Hypothese H1 + Nullhypothese H0 (§1)
- ✅ Metriken je Kategorie A/B, getrennt (§3)
- ✅ Entscheidungsregel formuliert (§6)
- ✅ Rausch-Regel vorhanden (§3)
- ✅ Versionswahl als Studiendesign-Entscheidung begründet (§2 "Zielplattform")
- ✅ Verbot der Nachträglichen Änderung formuliert (§6 "Verbot" + Header "FROZEN")

**Datei NICHT berührt** — Plan §6: "nie editieren, nur superseded durch neues datiertes Dokument".

---

### P0.4 — MC-Spike (Wegwerf, eigener Branch, wird NIE gemerged)

> Plan §5/P0.4: "Fährt eine Entity entlang eines hartkodierten Pfades? Despawnt sie bei Spielerentfernung
> und wird bei Annäherung zustandserhaltend rekonstruiert? → beweist T-D3 technisch."
> "Widerlegt der Spike T-D3, ist der PLAN falsch, nicht die Realität. Dann zurück zu §2."

**Frage, die der Spike beantworten muss:**
Kann eine Entity entlang eines hartkodierten Pfades fahren, bei Spielerentfernung despawnen
und bei Annäherung zustandserhaltend rekonstruiert werden?

**Das ist die technische Prüfung von T-D3. Der ganze Plan hängt daran.**
Antwort "nein" ist ein wertvolles Ergebnis, kein Misserfolg. Sofort melden, kein Workaround.

**Branch:** `p0.4-mc-spike` (eigener Branch, wird NIE gemerged)

**Dateien (im Spike-Branch, nicht in master):**
- `train-mc/src/main/java/de/traktion/spike/PathEntity.java` — Entity, die einem hartkodierten Pfad folgt
- `train-mc/src/main/java/de/traktion/spike/SpikeModInitializer.java` — Registrierung
- `train-mc/src/main/resources/fabric.mod.json` — angepasst für Spike

**Nebenbei zu klären ([VERIFY]-Notizen zurückmelden):**
- [ ] Erforderliche Java-Version für MC 26.2 (Mod-Target — vermutlich 21+)
- [ ] Exakter API-Name des welt-attached Persistent State in 26.2
  (⚠ das Welt-Datenformat hat sich in 26.1 geändert — 1.21er-Tutorials sind unzuverlässig)
- [ ] Läuft jqwik (oder Alternative) sauber unter Gradle 9.5.1?

**Recherche-Grenze für P0.4:** AUSGESETZT. Du DARFST recherchieren (webfetch oder agent-browser).
Die Recherche-Grenze aus dem normalen Planungs-Prompt ist für P0.4 aufgehoben, weil P0.4 ein
Wegwerf-Spike ist und die eigentliche Kategorie-B-Messung erst in P4 beginnt.

**Testliste:** keine Unit-Tests (Spike). Manuelles Smoke im Client.

**Akzeptanzkriterien:**
- [ ] Entity fährt entlang hartkodiertem Pfad im Client sichtbar
- [ ] Entity despawnt bei Spielerentfernung
- [ ] Entity wird bei Annäherung zustandserhaltend rekonstruiert (Position auf dem Pfad stimmt)
- [ ] [VERIFY]-Fragen zurückgemeldet: Java-Version, PersistentState-API-Name, jqwik-Unterstützung
- [ ] Antwort auf die Hauptfrage: ja oder nein. Wenn nein: sofort melden.

**Zielzuordnung:** T-D3 (technisch bewiesen) · Z9 (Token ⇄ Entity, vorläufig)
**Kategorie:** B (Minecraft-Runtime — aber AUSGESETZT für P0.4, da Wegwerf-Spike)

---

## Done-When (Plan §5/P0)

- [ ] `gradle :train-core:test` grün (leerer Smoke-Test)
- [ ] `docs/CONVENTIONS.md` existiert und benennt Verworfenes ✅ (P0.1, c2d132b)
- [ ] Spike beantwortet T-D3 mit ja/nein ⏳
- [ ] Preregistration liegt vor dem ersten Trial in der History ✅ (P0.3, a40e818)
- [ ] `example_project/` ist stillgelegt ✅ (vom Operator in Operator-Bereich verschoben, `.gitignore` Zeile 1 bleibt)

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
