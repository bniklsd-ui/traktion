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
| P0.4 — MC-Spike (Spawn-Fix) | ⏳ | (diese Session) | `SERVER_STARTED`-Callback + `EntityType.spawn` — Entity erscheint jetzt |

---

## Session stopped — 2026-07-16 (P0.4 Spawn-Fix nach Smoke-Test-Befund)

### Completed (diese Session)
- **Smoke-Test-Befund ausgewertet (Nikinger):** Die letzte Session endete mit einem technischen
  Fehler — der Smoke-Test zeigte: Entity wird registriert, aber nicht erschafft. Minecraft hing im
  Ladebildschirm. Die letzte Session hatte das Problem erkannt, aber den Fix nicht mehr committet
  oder dokumentiert. Dieser Session-stopped-Block schließt die Lücke.
- **Wurzelursache gefunden:** `SpikeModInitializer.onInitialize()` registrierte nur den `EntityType`,
  spawnte aber nie eine Entity. Der Kommentar "Beim Start wird eine Entity am Spawn gespawnt" war
  eine Lüge — der Code machte es nicht. Registrierung allein bringt keine Entity in die Welt.
- **API verifiziert gegen dekompilierte JARs (nicht aus Erinnerung):**
  - `ServerLifecycleEvents.SERVER_STARTED` (Callback: `MinecraftServer`) — aus
    `fabric-lifecycle-events-v1-4.1.3` JAR via `javap` dekompiliert
  - `MinecraftServer.overworld()` → `ServerLevel` — aus `minecraft-merged-deobf-26.2.jar`
  - `EntityType.spawn(ServerLevel, BlockPos, EntitySpawnReason)` → `T` — aus `EntityType.class`
  - `EntitySpawnReason.COMMAND` — passender Grund für programmatischen Spawn (wie `/summon`)
  - `ServerLevel.addFreshEntity(Entity)` → `boolean` — Alternative, nicht verwendet
- **Spawn-Fix geschrieben:** `SpikeModInitializer` registriert jetzt einen `SERVER_STARTED`-Callback,
  der beim Serverstart eine `PathEntity` in der Overworld bei `(0, 1, 0)` spawnt. API-Verifikation
  im Klassen-Kommentar dokumentiert.
- **Build grün:** `gradle :train-mc:build` BUILD SUCCESSFUL. Fix kompiliert gegen MC 26.2.
- **API-Notes ergänzt:** `phase0/MC26_API_NOTES.md` — neuer Abschnitt "Entity-Spawn-API" mit
  verifizierten Signaturen, `EntitySpawnReason`-Werten und Erklärung, warum `onInitialize()` nicht
  für Spawn-Logik taugt (läuft vor Weltexistenz).
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
- **P0.4 Spike-Code** (605ad0f): PathEntity + SpikeModInitializer + MC26_API_NOTES. Build grün,
  Smoke-Test offen (siehe Archiv 2026-07-15).

### Next
- **P0.4 MC-Spike — Smoke-Test (Operator):** `./gradlew :train-mc:runClient` im Spike-Branch
  starten. Der Spawn-Fix sollte die Entity jetzt bei `(0, 1, 0)` erscheinen lassen. Prüfen:
  1. Entity fährt entlang hartkodiertem Quadrat-Pfad im Client sichtbar?
  2. Entity despawnt bei Spielerentfernung (Chunk-Unload)?
  3. Entity wird bei Annäherung zustandserhaltend rekonstruiert (Position auf Pfad stimmt)?
  4. Antwort auf T-D3: ja oder nein. Wenn nein: sofort melden.
- **P0.4 [VERIFY]-Fragen:** PersistentState-API-Name geklärt (SavedData). Spawn-API geklärt
  (SERVER_STARTED + EntityType.spawn). jqwik noch offen. Java-Version geklärt (Java 25).
- **Nach P0.4:** P0 ist abgeschlossen, wenn der Smoke-Test T-D3 bestätigt. Dann P1 (train-core
  Durchstich) in neuer Session.

### Open questions / blockers
- **P0.4 Smoke-Test offen:** Spawn-Fix kompiliert und baut, aber nicht im Client getestet. Operator
  muss `./gradlew :train-mc:runClient` ausführen und die drei Akzeptanzkriterien prüfen.
- **Ladebildschirm-Hängen (vorheriger Smoke-Test):** Nikinger berichtete, Minecraft hing im
  Ladebildschirm. Das kann an der fehlenden Entity gelegen haben (Client wartete?), oder ein
  separates Problem sein. Der neue Smoke-Test muss zeigen, ob das behoben ist.
- **jqwik [VERIFY]:** Auskommentiert in `train-core/build.gradle.kts`. P0.4 oder P1 klärt, ob es
  unter Gradle 9.5.1 läuft. Fallback: JUnit 5 + eigene Generatoren.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(...)` wird im Spike
  verwendet. Bleibt [VERIFY], bis P4 echte 26.2-Quellen prüft.
- **`.opencode/agents/build-traktion.md` uncommitted:** Permission-Änderung (deny→ask) durch
  Operator, nicht durch Agent. Unangetastet gelassen.
- **Tool-Calls:** Diese Session benutzte ~25 Tool-Calls (Recherche + Spawn-Fix + Doku).
