---
status: live
purpose: Phasen-Kopf für P0 — Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike. Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P0; vor jedem Schritt in P0.2–P0.4; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE0_PLAN.md      # Konzept/Plan, aus dem P0 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-07-15
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
| P0.2 Step 2 — train-mc Build-Fehler behoben | ✅ | 0c32c06 | Non-remap Plugin-ID, Mappings entfernt, Java 25 |
| P0.4 — MC-Spike (Code geschrieben) | ⏳ | (diese Session) | Branch `p0.4-mc-spike`, kompiliert, Smoke-Test offen |

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
