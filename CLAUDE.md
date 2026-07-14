---
status: live
purpose: Single Source of Truth für Traktion. Projekt-Identität, Phasenstatus, Einstiegspunkte. Jede Session liest dies zuerst.
read-when: Session-Start; vor jeder Architektur-Entscheidung; bei Zweifel, was Wahrheit ist
detail: L1
up: (root)
down:
  - ./TRAKTION_OVERALL_PLAN.md        # Wahrheit — Mission, Locks, Hard Rules, Phasen, Ziele
  - ./docs/INDEX.md                   # Karte — eine Zeile pro Doku
  - ./docs/CONVENTIONS.md             # übernommene/verworfene Konventionen
  - ./docs/DOC_LAYERS_CONVENTION.md   # generische Spec des Doc-Layers-Systems
  - ./phase0/CLAUDE.md                # Phasen-Kopf P0 — Build-Log + aktueller Session-stopped-Block
  - ./docs/plans/PHASE0_PLAN.md       # Konzept/Plan, aus dem P0 gebaut wird
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
4. `phase<N>/CLAUDE.md` — Phasen-Kopf, insbesondere `## Session stopped` (aktuell: `phase0/CLAUDE.md`)
5. `docs/plans/PHASE<N>_PLAN.md` — Konzept/Plan, aus dem die Phase gebaut wird
6. `TRAKTION_OVERALL_PLAN.md` — §2 (Locks), §3 (Hard Rules), §4 (Ziele), §9 (Anti-Patterns)

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

## Session stopped

> Der aktuelle `## Session stopped`-Block lebt in `phase0/CLAUDE.md` (Doc-Layers-Konvention).
> Root-CLAUDE.md ist schlank und enthält keinen Session-stopped-Block.
> Alte Blöcke: `phase0/SESSIONS_ARCHIVE.md`.
