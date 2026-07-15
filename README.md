---
status: live
purpose: Menschliche Übersicht + Machine-Setup für das GitHub-Repo. Einstieg für Besucher, die nicht das Doku-System kennen.
read-when: Repo-Besuch über GitHub; vor dem Setup einer Entwicklungsmaschine
detail: L1
up: ./CLAUDE.md
down:
updated: 2026-07-15
---

# Traktion

> Ein Zugnetz ist kein Fahrzeug, sondern ein Betrieb.

Traktion ist ein Fabric-Mod für Minecraft 26.2, der ein elektrifiziertes Schienennetz simuliert,
das der Spieler plant, baut und am Leben hält. Züge sind Verbraucher an einem Netz, das der
Spieler selbst errichtet hat. Der Reiz liegt nicht darin, dass ein Zug fährt, sondern darin, dass
er fährt, **weil ein System existiert, das ihn fahren lässt** — und langsamer wird, wenn das
System schlampig gebaut ist.

## Architektur

```
train-core/   reines Java. Kein Fabric, kein net.minecraft.*, kein NBT.
              Graph · Physik · Energie · Verschleiß · Planer · Simulation · Fahrplan
              → gradle :train-core:test läuft in Sekunden, ohne Client-Start.

train-mc/     dünner Adapter. Blöcke, Entities, Rendering, Packets, Persistenz, GUI.
              → übersetzt Weltzustand in Zahlen und Zahlen zurück in Weltzustand.
```

Der Schnitt ist die wichtigste Entscheidung: die Domäne kennt kein Minecraft. Alles, was testbar
sein soll, lebt in `train-core` — ohne Minecraft-Abhängigkeit. `train-mc` ist ein dünner Adapter,
der Weltzustand in Zahlen übersetzt und zurück.

## Status

Phase 0 (Fundament) ist weitgehend abgeschlossen. Die Mod ist noch nicht spielbar — das erste
spielbare Milestone ist Phase 4. Siehe `ROADMAP.md` für die Phasenübersicht P0–P6.

## Setup (Entwicklungsmaschine)

**Voraussetzungen:**
- Java 21 (Gradle/Loom Build-Toolchain)
- Java 25 (Mod-Target für MC 26.2 — Loom fordert es)
- Gradle 9.5.1 (über Wrapper, wird mitgeliefert)

**Build:**
```bash
./gradlew :train-core:test          # Unit-Tests (Kategorie A, Sekunden)
./gradlew :train-mc:build           # Mod-Build (Kategorie B, lädt MC 26.2)
```

`train-core:test` braucht `--configure-on-demand`, solange `train-mc` nicht konfiguriert ist.

## Versions-Pinning (Stand 2026-07-15)

| Komponente | Wert |
|---|---|
| Minecraft | 26.2 (stable) |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.154.0+26.2 |
| Fabric Loom | 1.16.3 (non-remap Plugin-ID) |
| Gradle | 9.5.1 |
| Java (Build) | 21 |
| Java (Mod-Target) | 25 |

MC 26.x ist unobfuskiert (seit 26.1). Yarn/Mojmap sind obsolet; die non-remap Loom-Plugin-ID
`net.fabricmc.fabric-loom` wird verwendet. Siehe `phase0/Fabric_Loom_Mappings_Fix_01.md`.

## Doku-System

Dieses Repo verwendet ein Layer-basiertes Doku-System. Einstiegspunkte:

- `CLAUDE.md` — Single Source of Truth (Projekt-Identität, Phasenstatus)
- `docs/INDEX.md` — L0-Karte (eine Zeile pro Doku, Navigation über Header-Cards)
- `TRAKTION_OVERALL_PLAN.md` — Mission, Locks, Hard Rules, Phasen, Ziele
- `ROADMAP.md` — Phasenübersicht P0–P6
- `ARCHITECTURE.md` — Architektur-Schnitt und Ports
- `phase0/CLAUDE.md` — Phasen-Kopf P0 mit Build-Log und Session-stopped-Block

Jede Session liest `AGENTS.md` → `docs/INDEX.md` → `CLAUDE.md` → `phase<N>/CLAUDE.md` →
`docs/plans/PHASE<N>_PLAN.md` → `TRAKTION_OVERALL_PLAN.md` (§2/§3/§4/§9).

## Mess-Strang M1

Dieses Projekt ist Teil einer Modell-/Harness-Studie (M1). Die Vorregistrierung liegt in
`M1_PREREGISTRATION.md` (FROZEN, vor dem ersten Trial committet). Kategorie A (`train-core`)
und Kategorie B (`train-mc`) werden getrennt ausgewertet. Kein Agent schreibt in `m1/trials.jsonl`
— die Messung gehört nicht dem Gemessenen.
