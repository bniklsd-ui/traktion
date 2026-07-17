---
status: handover (P0 abgeschlossen → P1)
purpose: Phasen-Abschlussanalyse für P0. Status, Delta seit Repo-Start, erreichte Ziele, Verstöße, offene Entscheidungen und ein initialer Aufräum-Schritt für P1. Kalt-lesbar ohne PHASE0_PLAN.md oder Code neu lesen zu müssen.
read-when: vor dem Entwurf von PHASE1_PLAN.md; vor P1-Start; vor dem ersten P1-Schritt
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ./PHASE0_PLAN.md   # der Plan, den dieses Handover bilanziert
related: ../phase0/CLAUDE.md   # Build-Log + letzter Session-stopped-Block
updated: 2026-07-17
---

# Phase 0 — Handover (Abschlussanalyse)

> **Status:** abgeschlossen.
> **P0 war die Initial-Phase** — Planung UND Ausführung in derselben Session (Ausnahme, ab P1
> getrennt). Deshalb ist dieses Handover zugleich Phasen-Abschlussanalyse und Übergabe an P1.
> Es ersetzt nicht den `## Session stopped`-Block in `phase0/CLAUDE.md` (der bleibt dort), sondern
> ergänzt ihn um die systematische Auswertung gegen Plan §4/§9.

---

## Ergebnis auf einen Blick

| Dimension | Wert |
|---|---|
| **Status** | abgeschlossen |
| **Tests** | grün (`gradle :train-core:test` PASSED, SmokeTest) |
| **Live-Validierung** | durchgeführt (P0.4 Smoke-Test, Nikinger, 2026-07-16) |
| **Git** | committed/gepusht auf `main`; Spike-Branch `p0.4-mc-spike` gepusht, nie gemerged |
| **T-D3** | bestätigt (technisch bewiesen) |
| **Regel-2-Verstoß** | nein (keine Physikformel in P0 — `Physics` kommt erst in P1) |
| **Z5-Tautologie** | nein (kein Planer/Simulator in P0 — kommt erst P1/P3) |

---

## Erreichte Ziele (Plan §4) und woran messbar

P0 ist eine Fundament-Phase. Sie zielt auf keine Z1–Z11 direkt, sondern auf die Voraussetzungen,
damit P1–P6 messbar werden. Die Zuordnung zu §4-Zielen ist deshalb indirekt:

| Ziel | In P0 erreicht? | Messbar an |
|---|---|---|
| **Z9** (Token ⇄ Entity) | vorläufig / vorbereitet | P0.4-Spike beweist T-D3: Entity fährt Pfad, despawnt an Chunk-Grenze, rekonstruiert zustandserhaltend. Das ist die technische Grundlage für Z9, aber **nicht** Z9 selbst (Z9 braucht den `train-core`-Token, der erst in P1 existiert). |
| **Z10** (Graph überlebt Neustart) | nicht in P0 | T-D15 ist gelockt; die `SavedData`-API ist in `phase0/MC26_API_NOTES.md` recherchiert, aber nicht umgesetzt. P4. |
| **Z1–Z8, Z11** | nicht in P0 | diese leben in `train-core` (P1–P3, P5). In P0 existiert kein Domänencode. |

**Was P0 stattdessen erreicht hat (Infrastruktur-Ziele, nicht in §4):**

- **T-D12 verifiziert** — alle gepinnten Versionen (MC 26.2, fabric-api 0.154.0+26.2, loader 0.19.3,
  loom 1.16.3, gradle 9.5.1, Java 21 build / 25 mod-target) stehen in `gradle.properties` und sind
  gegen Maven/Fabric-Doku geprüft. Siehe `phase0/Fabric_Loom_Mappings_Fix_01.md`.
- **T-D17 gelöst** — Java 21 für Gradle-Build, Java 25 für MC 26.2-Mod-Target. Loom 1.16.3
  Fehlermeldung als Quelle.
- **T-D18 gelockt** — Gradle-Multi-Modul `train-core` + `train-mc`, beide in `settings.gradle.kts`.
- **T-D19 offen** — jqwik ist auskommentiert in `train-core/build.gradle.kts`. **P1 muss klären,
  ob jqwik unter Gradle 9.5.1 läuft.** Fallback: JUnit 5 + eigene Generatoren.
- **Preregistration liegt vor dem ersten Trial** — `M1_PREREGISTRATION.md` (a40e818) steht in der
  Git-History **vor** P0.1 (c2d132b). Plan §6 erfüllt.
- **`example_project/` stillgelegt** — physisch nicht im Repo (Operator verschoben), `.gitignore`
  Zeile 1 bleibt als Belt-and-Suspenders. Niemand liest es nach P0.

---

## Verstöße gegen Plan §3 / §9 — im P0-Code gefunden

**Regel-2-Verstoß (Physik an zwei Stellen):** nein.
In P0 existiert keine Physikformel. `Physics` ist P1-Arbeit. Es gibt nichts zu duplizieren.

**Z5-Tautologie (Planer ruft Simulator):** nein.
In P0 existiert weder Planer noch Simulator. P1/P3 sind die Watch-Phasen.

**§9 Anti-Patterns, einzeln geprüft:**

| Anti-Pattern | In P0 gefunden? | Fundstelle / Begründung |
|---|---|---|
| `net.minecraft.*`/NBT-Import in `train-core` | **nein** | `grep -rn "net.minecraft" train-core/src/` findet nur den Kommentar in `package-info.java` ("Kein net.minecraft.*") — kein Import. Kein NBT, kein `ItemStack` in `train-core`. |
| Physik-Formel an zwei Stellen | **nein** | keine Physik in P0 |
| Planer ruft Simulator | **nein** | keine in P0 |
| Variable Schrittweite / Wall-Clock / HashSet in Physikschleife | **nein** | keine Physikschleife in P0 |
| Interface ohne zwei benennbare Implementierungen | **nein** | keine Interfaces in P0 (Skelett hat nur `package-info`) |
| Weltzustand ohne Ausweg (Softlock) | **nein** | keine Weltzustand in P0 |
| Zeitbasierter Verfall / ItemStack im Kern | **nein** | kein Kern-Code in P0 |
| Roher OpenGL-Call | **nein** | Spike-Renderer erbt `EntityRenderer` und lässt `submit()` leer — kein OpenGL, nur Blaze3D-Vererbung |
| Agent liest `example_project/` nach P0 | **nein** | physisch nicht da |
| Trial ohne vorher notierte Erwartung | **nicht anwendbar** | `trials.jsonl` ist leer (0 Bytes) — siehe "Offene Entscheidungen" |
| Metrik mittelt A und B | **nein** | keine Auswertung in P0 |
| Phase ohne CLAUDE.md als erste Aktion | **nein** | `phase0/CLAUDE.md` existiert; Root-CLAUDE in P0.2 Step 1 |
| Session ohne `## Session stopped` | **nein** | Block in `phase0/CLAUDE.md` vorhanden |

**Fazit: Kein einziger §9-Eintrag ist im P0-Code verletzt.** Das ist bei einer Fundament-Phase
erwartbar (kein Domänencode), aber es ist dokumentiert, nicht behauptet.

---

## Was NICHT funktioniert hat / negative Befunde

- **Zwei Hitboxen statt einer im Smoke-Test.** `SERVER_STARTED` feuert beim integrierten Server
  vermutlich zweimal (Welt-Generierung + Welt-Laden), oder der Callback wurde doppelt registriert.
  Für T-D3 nicht kritisch (Persistenz wird dadurch besser getestet), aber ein Hinweis für P4:
  Spawn-Logik muss idempotent sein oder ein Flag setzen.
- **y≈100 statt y=1.** `PathEntity.setPos(x, 1.0, z)` setzt y=1, aber `noPhysics = true` und Vanilla
  hebt die Entity auf die Oberfläche. Für den Spike nicht kritisch (Position konsistent), aber P4
  muss klären, wie Token-Position auf die Schienenhöhe gemappt wird.
- **jqwik [VERIFY] ungelöst.** Auskommentiert in `train-core/build.gradle.kts` (Zeile 20). Weder
  P0.4 noch die Recherche haben geklärt, ob jqwik unter Gradle 9.5.1 läuft. P1 muss das als eine
  der ersten Aktionen klären — Z5 ist property-based und braucht es (oder den Fallback).
- **`[VERIFY] Fabric-Logging-Konvention in 26.2`** bleibt stehen. `LoggerFactory.getLogger(...)`
  wird im Spike verwendet, aber nicht gegen echte 26.2-Quellen geprüft. Bleibt bis P4.

---

## Delta seit letztem Handover

P0 ist die erste Phase — es gibt kein vorheriges Handover. Das Delta ist "von null auf P0-abgeschlossen".

**Commits (main, chronologisch):** 799f103 (skeleton) → a40e818 (preregistration) → c2d132b (P0.1
conventions) → 780c0cd (P0.2 step 1 docs) → c11ab63 (P0.2 step 2 skeleton) → 75ad958 (wrapper) →
fabd860 (doc-layers) → bed7b49 (P0.2 step 3 stubs) → 97b7add (P0.2 step 4 conventions) →
0c32c06 (train-mc build-fix) → 811cb6e/9ba7fc2 (README/attribution) → 625c10a (merge P0 docs).

**Spike-Branch (`p0.4-mc-spike`, nie gemerged):** 605ad0f (spike code) → 6b88975 (spawn-fix) →
a60edf9 (renderer-fix) → b746378/bdf3bdb (docs: P0 done). Auf `origin` gepusht.

**Was auf `main` steht (Code-Wahrheit):**
- `train-core/`: `package-info.java` + `SmokeTest.java` (leerer grüner Test). Kein Domänencode.
- `train-mc/`: `package-info.java` + `fabric.mod.json` (Stub, keine Entrypoints auf main).
- Build-Files: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, beide Modul-Builds.

**Was NICHT auf `main` steht (nur auf `p0.4-mc-spike`):**
- `train-mc/src/main/java/de/traktion/spike/` — PathEntity, SpikeModInitializer, PathEntityRenderer,
  SpikeClientInitializer. Wegwerf-Spike. Wird nie gemerged. Die Erkenntnisse leben in
  `phase0/MC26_API_NOTES.md`, nicht im Code.

---

## Offene Entscheidungen / was P1 vor dem Plan-Entwurf wissen muss

### A. Initialer Aufräum-Schritt (VOR P1-Plan-Entwurf, als Step 0)

Drei Dinge haben sich seit dem letzten committeten Stand gedriftet oder sind uncommitted. Sie
gehören in den Step 0 des P1-Plans (Altlasten), nicht in den Domänen-Code:

1. **`train-mc/src/main/resources/fabric.mod.json` (main) — Java-Version drift.**
   Datei sagt `"java": ">=21"`, aber `train-mc/build.gradle.kts` (Zeile 25) und
   `gradle.properties` (`java_mod_target=25`) sagen Java 25 für MC 26.2. Der Spike-Branch hat
   korrekt `">=25"`. **Code ist Wahrheit → Manifest falsch.** Fix: `">=21"` → `">=25"` auf main.
   Berührt keinen P0-Domänencode, nur das Mod-Manifest.

2. **`.opencode/agents/build-traktion.md` uncommitted (deny→ask).**
   `git diff` zeigt: `m1/**`, `M1_*.md`, `docs/plans/**`, `TRAKTION_OVERALL_PLAN.md` wurden von
   `deny` auf `ask` geändert. Die zu restriktiven `deny`-Permissions haben sich als nicht
   arbeitstauglich erwiesen (Operator-Entscheid). **Anweisung:** diesen Stand committen, damit P1
   die `ask`-Permissions vorfindet. Nicht den Agenten entscheiden lassen, ob er `M1_*.md` oder
   `docs/plans/**` editiert — `ask` zwingt zur Bestätigung, was ausreicht.

3. **`m1/trials.jsonl` ist leer (0 Bytes) — P0-Trials fehlen.**
   Preregistration §7 sagt Trial 1 = P0.1. P0.1–P0.4 sind committed, aber keine Trial-Zeile
   eingetragen. **Anweisung:** P0-Trials als einzelnen Schritt **zusammen mit dem Operator**
   nachtragen. Der Agent darf nicht in `trials.jsonl` schreiben (`edit: m1/** → ask`/deny). Der
   Agent liefert die Rohdaten (siehe "Rohdaten für trials.jsonl" unten), der Operator trägt ein.

### B. Was P1 aus P0 mitnehmen muss (keine Neu-Entscheidungen, nur Verweise)

- **jqwik [VERIFY] — als ERSTE Aktion in P1 klären.** Auskommentiert in
  `train-core/build.gradle.kts` Zeile 20. Wenn jqwik unter Gradle 9.5.1 läuft: einkommentieren.
  Wenn nicht: Fallback JUnit 5 + eigene Generatoren. Z5 (P3) ist property-based und braucht eine
  Antwort. Pin in `gradle.properties` (`jqwik_version=1.9.0`, auskommentiert).
- **`phase1/CLAUDE.md` als allererste Aktion** (Plan §11, Anti-Pattern §9). Bevor irgendein P1-Code
  entsteht. Der aktuelle Session-stopped-Block lebt dann in `phase1/CLAUDE.md`, nicht mehr in
  `phase0/CLAUDE.md`.
- **`Physics.requiredPowerW` ist Regel-2-territorium.** P1 schreibt die EINE Funktion. P3 (Planer)
  muss dieselbe aufrufen. Der P1-Plan muss das als Akzeptanzkriterium festhalten: "genau eine
  Physikfunktion, kein zweiter Formel-Körper". Das ist der P3-Watchpunkt, vorab verankert.
- **Determinismus (Regel 8) ist P1-Akzeptanzkriterium.** Zwei Läufe mit gleichem Seed → bitgleich.
  Fixed dt, geordnete Iteration, gesäter Zufall. Kein `HashSet` in der Physikschleife.
- **`SavedData`-API (T-D15) ist recherchiert, nicht umgesetzt.** `phase0/MC26_API_NOTES.md` hat
  die 26.2-API (`SavedData` + `SavedDataType` + Codec). P4 nutzt das. P1 braucht es nicht.
- **Spike-Erkenntnisse für P4 (nicht P1):** zwei Hitboxen (Spawn-Idempotenz), y-Verschiebung
  (Token→Schienenhöhe). In `phase0/CLAUDE.md` Session-stopped-Block festgehalten.

### C. [VERIFY]-Marken, die P1 erben muss

- `jqwik` unter Gradle 9.5.1 — **P1 klärt es.**
- Fabric-Logging-Konvention in 26.2 (`LoggerFactory.getLogger`) — bleibt bis P4.
- `SavedData`-API-Name — recherchiert in `MC26_API_NOTES.md`, P4 verifiziert gegen echte Quellen.

---

## Dateipfade als Verweise (für den P1-Chat)

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §4 Ziele, §5/P1, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §2 Konstanten, §3 Metriken, §7 Trial-Zählung. Nie editieren. |
| P0-Plan (bilanziert) | `docs/plans/PHASE0_PLAN.md` | Schritt-Sequenz, Akzeptanzkriterien — Referenz, nicht neu lesen |
| P0-Build-Log + Session-stopped | `phase0/CLAUDE.md` | letzter Stand, Smoke-Test-Befunde |
| MC 26.2 API-Notizen | `phase0/MC26_API_NOTES.md` | Entity-Registrierung, `SavedData`, Renderer-API — für P4 |
| Loom/Mappings-Fix | `phase0/Fabric_Loom_Mappings_Fix_01.md` | warum non-remap Plugin-ID, warum Java 25 |
| Konventionen | `docs/CONVENTIONS.md` | Logging, Testmatrix, Root-Layout |
| Build-Files | `settings.gradle.kts`, `gradle.properties`, `train-core/build.gradle.kts`, `train-mc/build.gradle.kts` | gepinnte Versionen, jqwik auskommentiert |
| Spike-Branch | `p0.4-mc-spike` (remote) | Wegwerf-Code, nie mergen. Erkenntnisse in MC26_API_NOTES. |

---

## Rohdaten für die trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei

> Plan §7: "Kein Agent schreibt in `trials.jsonl`. Die Messung gehört nicht dem Gemessenen."
> Diese Rohdaten liefere ich als Text. Nikinger trägt ein. P0 hatte mehrere Sub-Schritte;
> ob eine Zeile pro Sub-Schritt oder eine aggregierte P0-Zeile, entscheidet Nikinger.

**Vorschlag: eine Zeile pro P0-Sub-Schritt (P0.1, P0.2, P0.3, P0.4), da sie unterschiedliche
Kategorien messen (P0.1 = A, P0.2 = A/Infrastruktur, P0.3 = docs, P0.4 = B).**

### P0.1 — Konventions-Import (Kategorie A, docs-only)
- `phase`: P0.1
- `ziel`: Konventions-Import aus Referenzprojekt → `docs/CONVENTIONS.md`
- `kategorie`: A (docs-only, kein Code)
- `iterationen`: 1 (ein Commit c2d132b, keine Regression)
- `diff_lines`: siehe `git show c2d132b --stat` (CONVENTIONS.md neu)
- `tests_gruen`: n/a (docs-only)
- `regressionen`: 0
- `operator_eingriffe`: 0 (Agent las kuratierte Teilmenge, schrieb Destillat)
- `regel2_verstoss`: nein
- `z5_tautologie`: nein
- `recherche_schritte`: 0 (Lesen des Referenz-Repos, keine Web-Recherche)
- `notiz`: 17 übernommen, 13 verworfen. `example_project/` danach stillgelegt.

### P0.2 — Skelett (Kategorie A, Infrastruktur)
- `phase`: P0.2
- `ziel`: Gradle-Multi-Modul + Root-Docs + ROADMAP/ARCHITECTURE + Log/Testmatrix
- `kategorie`: A (Infrastruktur, kein Domänencode)
- `iterationen`: mehrere (Skelett → Wrapper → Doc-Layers → Build-Fix → Stubs → Conventions).
  Commits: c11ab63, 75ad958, fabd860, 780c0cd, bed7b49, 97b7add, 0c32c06.
- `diff_lines`: siehe `git log --oneline c11ab63..97b7add --stat`
- `tests_gruen`: 1 (SmokeTest PASSED, `gradle :train-core:test`)
- `regressionen`: 0
- `operator_eingriffe`: 1 (train-mc Build-Fix brauchte Operator-Kenntnis der non-remap Plugin-ID;
  siehe `Fabric_Loom_Mappings_Fix_01.md` — Recherche vom Planungschat, nicht vom Build-Agent allein)
- `regel2_verstoss`: nein
- `z5_tautologie`: nein
- `recherche_schritte`: mehrere (Maven/Fabric-Doku für Versionen, Loom-Plugin-ID)
- `notiz`: Java 21→25 für mod-target geklärt. `--configure-on-demand` nötig für test.

### P0.3 — Vorregistrierung (docs-only, kein Trial)
- `phase`: P0.3
- `ziel`: `M1_PREREGISTRATION.md` FROZEN vor erstem Trial
- `kategorie`: — (Messinstrument, nicht A/B)
- `iterationen`: 1 (a40e818)
- `tests_gruen`: n/a
- `regressionen`: 0
- `operator_eingriffe`: 0
- `regel2_verstoss`: nein
- `z5_tautologie`: nein
- `recherche_schritte`: 0
- `notiz`: liegt vor P0.1 in der History. Plan §6 erfüllt.

### P0.4 — MC-Spike (Kategorie B, AUSGESETZT — Wegwerf)
- `phase`: P0.4
- `ziel`: T-D3 technisch beweisen (Entity fährt Pfad, despawnt, rekonstruiert)
- `kategorie`: B (Minecraft-Runtime — aber Recherche-Grenze für P0.4 aufgehoben, Plan §5/P0.4)
- `iterationen`: 3 Code-Iterationen (605ad0f spike → 6b88975 spawn-fix → a60edf9 renderer-fix)
- `diff_lines`: siehe `git log p0.4-mc-spike --stat 605ad0f..a60edf9`
- `tests_gruen`: n/a (manueller Smoke, kein Unit-Test)
- `regressionen`: 2 (Spawn-Entity erschien nie → SERVER_STARTED-Fix; NPE-Crash beim Rendern →
  Renderer-Fix)
- `operator_eingriffe`: 1 (Nikinger führte den Smoke-Test manuell auf seinem PC durch — der Agent
  kann keinen Client starten. Das ist ein Kategorie-B-Eingriff per Definition, Plan §7)
- `regel2_verstoss`: nein
- `z5_tautologie`: nein
- `recherche_schritte`: mehrere (docs.fabricmc.net 26.1.2, dekompilierte JARs mit `javap` für
  EntityType.spawn, EntityRenderer, EntityRenderState, EntityRendererRegistry). Siehe
  `phase0/MC26_API_NOTES.md` — dokumentierte Recherche, nicht Recall.
- `notiz`: T-D3 bestätigt. Zwei Hitboxen (SERVER_STARTED doppelt?), y≈100 (noPhysics hebt auf).
  Beide nicht kritisch für T-D3. Branch `p0.4-mc-spike` gepusht, nie gemerged.

---

## Session stopped

> Dieser Block ist die Phasen-Abschluss-Analyse. Der operative `## Session stopped`-Block
> (letzte Session) bleibt in `phase0/CLAUDE.md` stehen — er wird nicht hierher verschoben.
> P1 beginnt mit `phase1/CLAUDE.md` als erster Aktion (Plan §11).

**Diese Session (P0-Handover):**
- P0 systematisch gegen Plan §4/§9 ausgewertet. Kein §9-Verstoß im Code. T-D3 bestätigt.
- Drei initiale Aufräum-Punkte für P1 identifiziert (fabric.mod.json-Drift, uncommitted
  Permissions, fehlende trials.jsonl-Einträge).
- Rohdaten für trials.jsonl als Text an Nikinger geliefert (vier Sub-Schritte).

**Next (P1, neue Session):**
1. `phase1/CLAUDE.md` als allererste Aktion (Plan §11).
2. Initialer Aufräum-Schritt (Step 0): fabric.mod.json Java 25, build-traktion.md committen,
   trials.jsonl mit Operator nachtragen.
3. jqwik [VERIFY] klären — erste Domänen-Aktion.
4. PHASE1_PLAN.md entwerfen (Plan §5/P1): RailGraph, Consist, Physics, PowerGrid, Simulator,
   BlockSection. Akzeptanz: Z1–Z4 grün, Determinismus (Regel 8), null externe Abhängigkeiten.
