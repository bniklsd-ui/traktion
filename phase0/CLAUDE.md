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
| P0.2 Step 2 — train-mc Build-Fehler behoben | ✅ | (diese Session) | Non-remap Plugin-ID, Mappings entfernt, Java 25 |
| P0.4 — MC-Spike | ⏳ | — | eigener Branch `p0.4-mc-spike`, eigene Session |

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
