---
status: live
purpose: Architektur-Schnitt train-core / train-mc und die zwei Ports. Stub, der auf TRAKTION_OVERALL_PLAN.md als Wahrheit zeigt.
read-when: vor jeder Architektur-Entscheidung; bei der Frage "wo lebt dieser Code"; vor dem Schnitt zwischen Kern und Adapter
detail: L1
up: ./CLAUDE.md
down:
updated: 2026-07-14
---

# Traktion — Architektur

> **Wahrheit:** `TRAKTION_OVERALL_PLAN.md` §1 (Kernprinzip) und §3.2 (die zwei Ports).
> Diese Datei ist ein Stub — der Schnitt und die Ports, die auf das Plan-Dokument zeigen.
> Bei Drift: Plan vertrauen, hier fixen.

---

## Der Schnitt (Plan §1)

> **Bauprinzip: Die Domäne kennt kein Minecraft.**

```
train-core/   reines Java. Kein Fabric, kein net.minecraft.*, kein NBT.
              Graph · Physik · Energie · Verschleiß · Planer · Simulation · Fahrplan
              → gradle :train-core:test läuft in Sekunden, ohne Client-Start.

train-mc/     dünner Adapter. Blöcke, Entities, Rendering, Packets, Persistenz, GUI.
              → übersetzt Weltzustand in Zahlen und Zahlen zurück in Weltzustand.
```

**`train-core` macht:** Graphstruktur + Invarianten · Fahrphysik · Energiebilanz und
Spannungsabfall · Verschleiß und seine Rückwirkung · Blockabschnitte, Kollisionsfreiheit,
Deadlock-Erkennung · Fahrplan inkl. Override · **den Planer, mit derselben Physikfunktion wie
die Simulation** (Regel 2, §4).

**`train-mc` macht NUR:** Weltinteraktion (Gleiseditor) · Token ⇄ Entity · Persistenz ·
Rendering, GUI, Netcode · **Zahlen liefern**, die der Kern verlangt.

> **Wenn du in `train-core` ein `net.minecraft.*`-Import brauchst, ist der Schnitt falsch. Stopp.**

---

## Die zwei Ports (Plan §3.2)

> Die gesamte geplante Erweiterbarkeit. Ein Interface nur, wo die zweite Implementierung
> **heute benannt** werden kann (Regel 3).

```java
/** Port 1 — woher der Strom kommt. */
interface PowerSupply {
    /** Liefert höchstens `requestedW`. Weniger, wenn nichts da ist. */
    double supply(double requestedW, double dtSeconds);
}
// heute:  ManualGenerator   — fester Output, Brennstoff von Hand
// später: IndustrialGrid    — Kraftwerke, Netz, Speicher

/** Port 2 — woher die Instandhaltungsgüter kommen. */
interface MaintenanceSupply {
    /** Entnimmt bis zu `requested`. Gibt zurück, wie viele es wurden. */
    int withdraw(int requested);
}
// heute:  PlayerLabor       — Spieler repariert selbst, kostet Spielzeit
// später: DepotStock        — Betriebswerk, per Zug beliefert
```

`PlayerLabor` ist **kein Stub**, sondern die dauerhafte Rückfallebene mit echtem Preis (Zeit).
Ein kostenloser Stub würde Z6 und Z7 zu leeren Tests machen.

Mehr Ports sind nicht geplant. Fracht ist keiner (T-D7). Kommt Teil 2, ändert sich am Kern
**nichts** — ein anderer Lieferant schreibt dieselben Zahlen.

---

## Harte Grenzen am Schnitt (Plan §3)

- Kein `net.minecraft.*`, kein NBT, kein `ItemStack` in `train-core`.
- Die Physikformel existiert genau **EINMAL**. Planer und Simulator rufen dieselbe Funktion.
- Der Planer ruft **NIE** den Simulator auf. Sonst ist Z5 tautologisch.
- Fixed dt, geordnete Iteration, gesäter Zufall. Kein Wall-Clock, kein `HashSet` in der Physikschleife.
- Kein Interface ohne zwei heute benennbare Implementierungen.
- Kein roher OpenGL-Call. Nur Blaze3D (T-D16).

---

## Siehe auch

- `TRAKTION_OVERALL_PLAN.md` §1 — Kernprinzip, vollständige Beschreibung des Schnitts
- `TRAKTION_OVERALL_PLAN.md` §3.2 — die zwei Ports mit Implementierungs-Kommentaren
- `TRAKTION_OVERALL_PLAN.md` §3 — Hard Rules (keine Ausnahmen)
- `ROADMAP.md` — Phasen, in denen die Komponenten gebaut werden
