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
| P0.4 — MC-Spike (Code geschrieben) | ⏳ | 605ad0f | Branch `p0.4-mc-spike`, kompiliert, Smoke-Test offen |
| P0.4 — MC-Spike (Spawn-Fix) | ✅ | 6b88975 | `SERVER_STARTED`-Callback + `EntityType.spawn` |
| P0.4 — MC-Spike (Renderer-Fix) | ⏳ | (diese Session) | `PathEntityRenderer` + `SpikeClientInitializer` — NPE-Crash behoben |

---

## Session stopped — 2026-07-16 (P0.4 Renderer-Fix nach Crash-Log-Analyse)

### Completed (diese Session)
- **Crash-Log ausgewertet (Nikinger):** Manueller Smoke-Test auf Nikingers PC (Minecraft 26.2,
  Fabric Loader, fabric-api 0.155.0+26.2). Welt erstellt → Client crasht im Render-Frame.
  Crash-Log: `train-mc/logs/crash-2026-07-16_20.45.15-client.txt`.
- **Wurzelursache gefunden:** `NullPointerException: EntityRenderer.shouldRender()` — `renderer`
  ist null. Die Entity war in der Welt (`Tracked entity count: 2` im Crash-Log bestätigt es), aber
  der Client hatte keinen Renderer für unseren EntityType. Ohne Renderer crasht der Render-Loop.
  Das erklärt auch das "Ladebildschirm-Hängen" aus dem vorherigen Smoke-Test — der Client crashte
  im Render-Frame, was wie ein Hängen aussah.
- **API verifiziert gegen dekompilierte JARs (nicht aus Erinnerung):**
  - `EntityRendererRegistry.register(EntityType, EntityRendererProvider)` — aus
    `fabric-rendering-v1-25.3.0` JAR
  - `EntityRenderer<T, S>` ist abstrakt, `createRenderState()` ist die einzige abstrakte Methode —
    aus `EntityRenderer.class`
  - `EntityRenderState` ist eine konkrete Klasse (keine Ableitung nötig) — aus
    `EntityRenderState.class`
  - `EntityRendererProvider.Context` — Konstruktor-Argument für den Renderer — aus
    `EntityRendererProvider$Context.class`
- **Renderer-Fix geschrieben:**
  - `PathEntityRenderer.java` — minimaler Renderer, der nichts rendert (`submit()` leer). Verhindert
    den NPE. Entity ist unsichtbar (nur Hitbox über F3+B sichtbar).
  - `SpikeClientInitializer.java` — Client-Entrypoint, registriert den Renderer via
    `EntityRendererRegistry.register()`.
  - `fabric.mod.json` — `"client": ["de.traktion.spike.SpikeClientInitializer"]` hinzugefügt.
- **Build grün:** `gradle :train-mc:build` BUILD SUCCESSFUL. JAR enthält alle 4 Klassen.
- **API-Notes ergänzt:** `phase0/MC26_API_NOTES.md` — neuer Abschnitt "Entity-Renderer-API" mit
  verifizierten Signaturen und minimalem Renderer-Code-Beispiel.
- **Anti-Pattern-Check:** Fix ist in `train-mc` (nicht `train-core`). Kein `net.minecraft.*` in
  `train-core`. Kein Verstoß. ✅

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
- **P0.4 Spike-Code** (605ad0f): PathEntity + SpikeModInitializer + MC26_API_NOTES. Build grün.
- **P0.4 Spawn-Fix** (6b88975): SERVER_STARTED-Callback + EntityType.spawn. Build grün, aber
  Client crashte ohne Renderer (siehe Archiv 2026-07-16 Spawn-Fix).

### Next
- **P0.4 MC-Spike — Smoke-Test (Operator):** Neue JAR bauen (`./gradlew :train-mc:build`), nach
  `.minecraft/mods/` kopieren (zusammen mit fabric-api), Welt erstellen oder laden. Prüfen:
  1. Client crasht nicht mehr im Render-Frame?
  2. Entity bei `(0, 1, 0)` — sichtbar über F3+B (Hitbox) oder F3-Entity-Count?
  3. Entity fährt entlang hartkodiertem Quadrat-Pfad?
  4. Entity despawnt bei Spielerentfernung (Chunk-Unload)?
  5. Entity wird bei Annäherung zustandserhaltend rekonstruiert (Position auf Pfad stimmt)?
  6. Antwort auf T-D3: ja oder nein. Wenn nein: sofort melden.
- **P0.4 [VERIFY]-Fragen:** PersistentState-API-Name geklärt (SavedData). Spawn-API geklärt
  (SERVER_STARTED + EntityType.spawn). Renderer-API geklärt (EntityRendererRegistry + minimaler
  Renderer). jqwik noch offen. Java-Version geklärt (Java 25).
- **Nach P0.4:** P0 ist abgeschlossen, wenn der Smoke-Test T-D3 bestätigt. Dann P1 (train-core
  Durchstich) in neuer Session.

### Open questions / blockers
- **P0.4 Smoke-Test offen:** Renderer-Fix kompiliert und baut, aber nicht im Client getestet.
  Operator muss neue JAR bauen und testen.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 oder P1 klärt, ob es
  unter Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(...)` wird im Spike
  verwendet. Bleibt [VERIFY], bis P4 echte 26.2-Quellen prüft.
- **`.opencode/agents/build-traktion.md` uncommitted:** Permission-Änderung (deny→ask) durch
  Operator, nicht durch Agent. Unangetastet gelassen.
- **Tool-Calls:** Diese Session benutzte ~20 Tool-Calls (Crash-Log-Analyse + Renderer-Fix + Doku).
