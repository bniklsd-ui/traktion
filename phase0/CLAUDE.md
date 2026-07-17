---
status: live
purpose: Phasen-Kopf für P0 — Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike. Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P0; vor jedem Schritt in P0.2–P0.4; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE0_PLAN.md      # Konzept/Plan, aus dem P0 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-07-16
---

# Phase 0 — Fundament, Konventions-Import & Messinstrument

> **Status:** P0.1 ✅ · P0.3 ✅ · P0.2 Step 2 ✅ (train-core grün) · P0.2 Step 3 ✅ · P0.2 Step 4 ✅ · P0.4 ✅ (T-D3 bestätigt)
>
> **Ausnahme P0:** Planung UND Ausführung in derselben Session. Ab P1 getrennt.
>
> **Konzept:** `docs/plans/PHASE0_PLAN.md` (gelockte Entscheidungen, Schritt-Sequenz, Akzeptanzkriterien).
>
> **P0 ist abgeschlossen.** Nächster Schritt: P1 (train-core Durchstich) in neuer Session.

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
| P0.2 Step 2 — train-mc Build-Fehler behoben | ✅ | 0c32c06 | Non-remap Plugin-ID, Mappings entfernt, Java 25 |
| P0.4 — MC-Spike (Code geschrieben) | ✅ | 605ad0f | Branch `p0.4-mc-spike`, kompiliert |
| P0.4 — MC-Spike (Spawn-Fix) | ✅ | 6b88975 | `SERVER_STARTED`-Callback + `EntityType.spawn` |
| P0.4 — MC-Spike (Renderer-Fix) | ✅ | a60edf9 | `PathEntityRenderer` + `SpikeClientInitializer` — NPE-Crash behoben |
| P0.4 — MC-Spike (Smoke-Test) | ✅ | (Nikinger) | T-D3 bestätigt: Entity fährt Pfad, despawnt, rekonstruiert an Chunk-Grenze |

---

## Session stopped — 2026-07-16 (P0.4 Smoke-Test bestätigt T-D3 — P0 abgeschlossen)

### Completed (diese Session)
- **P0.4 Smoke-Test bestätigt (Nikinger):** Manueller Test auf Nikingers PC mit der Renderer-Fix-
  JAR. Ergebnis: **T-D3 ist technisch bewiesen.** Zwei quadratische Hitboxen bei (0,0) sichtbar
  (F3+B), die im Quadrat fahren. Persistenz bestätigt: Hitboxen genau an der Chunk-Grenze, nach
  Entfernung und Annäherung zustandserhaltend rekonstruiert.
- **Beobachtungen (nicht kritisch für T-D3):**
  - **Zwei Hitboxen statt einer:** `SERVER_STARTED` feuert beim integrierten Server vermutlich
    zweimal (Welt-Generierung + Welt-Laden), oder der Callback wurde doppelt registriert. Für den
    Spike irrelevant — Persistenz wird dadurch sogar besser getestet.
  - **y≈100 statt y=1:** `PathEntity.setPos(x, 1.0, z)` setzt y=1, aber die Entity hat
    `noPhysics = true` und Vanilla scheint sie auf die Oberfläche zu heben. Für den Spike nicht
    kritisch — die Position ist konsistent, das ist was zählt.
- **P0 ist abgeschlossen.** Alle Akzeptanzkriterien erfüllt:
  - `gradle :train-core:test` grün ✅
  - `docs/CONVENTIONS.md` existiert und benennt Verworfenes ✅
  - Spike beantwortet T-D3 mit **ja** ✅
  - Preregistration liegt vor dem ersten Trial in der History ✅
  - `example_project/` ist stillgelegt ✅
- **Branch gepusht:** `p0.4-mc-spike` auf `origin` (wird nie gemerged — Wegwerf-Spike).

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
- **P0.4 Spike-Code** (605ad0f): PathEntity + SpikeModInitializer + MC26_API_NOTES.
- **P0.4 Spawn-Fix** (6b88975): SERVER_STARTED-Callback + EntityType.spawn.
- **P0.4 Renderer-Fix** (a60edf9): PathEntityRenderer + SpikeClientInitializer — NPE-Crash behoben.

### Next
- **P1 — train-core Durchstich (Kategorie A):** Neue Session. Plan §5/P1:
  - `RailGraph` — Knoten, Kanten, `RailKind`, `gradient`, Länge (Z1)
  - `Consist` — `carCount`, `tareMassKg`, `payloadMassKg` (T-D7)
  - `Physics` — **eine** Funktion `requiredPowerW(consist, speed, gradient)` (Regel 2)
  - `PowerGrid` — Bedarf, Angebot über `PowerSupply`, Unterwerk-Reset (Z4 ohne `condition`)
  - `Simulator` — fixed-dt-Substep-Schleife, Token bewegt sich, Unterversorgung bremst (Z3, T-D13)
  - `BlockSection` — Reservierung, Kollisionsfreiheit, Deadlock-Erkennung (Z2)
  - Done when: Z1–Z4 grün; null externe Abhängigkeiten außer Test-Bibliotheken; zwei Läufe mit
    gleichem Seed liefern bitgleiche Ergebnisse (Regel 8).
- **Vor P1:** `phase1/CLAUDE.md` als allererste Aktion schreiben (Plan §11, Anti-Pattern §9).
- **jqwik [VERIFY]:** Noch offen. P1 muss klären, ob es unter Gradle 9.5.1 läuft. Fallback:
  JUnit 5 + eigene Generatoren.

### Open questions / blockers
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P1 muss es klären. Fallback:
  JUnit 5 + eigene Generatoren.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(...)` wird im Spike
  verwendet. Bleibt [VERIFY], bis P4 echte 26.2-Quellen prüft.
- **`.opencode/agents/build-traktion.md` uncommitted:** Permission-Änderung (deny→ask) durch
  Operator, nicht durch Agent. Unangetastet gelassen.
- **Tool-Calls:** Diese Session benutzte ~5 Tool-Calls (Doku-Update + Push).
