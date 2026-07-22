---
status: live
purpose: Phasen-Übersicht P0–P6 mit Status pro Phase. Stub, der auf TRAKTION_OVERALL_PLAN.md als Wahrheit zeigt.
read-when: vor Phasen-Übergang; bei der Frage "welche Phase kommt als nächste"; für einen schnellen Projekt-Überblick
detail: L1
up: ./CLAUDE.md
down:
updated: 2026-07-22
---

# Traktion — Roadmap

> **Wahrheit:** `TRAKTION_OVERALL_PLAN.md` §5 (Phasenordnung). Diese Datei ist ein Stub —
> gekürzte Tabelle, die auf das Plan-Dokument zeigt. Bei Drift: Plan vertrauen, hier fixen.
>
> **Prinzip:** Jede Phase ist vollständig testbar, bevor die nächste beginnt. Kein Vorgreifen.
> Nach P4 fährt ein Zug im Spiel. Alles danach ist Ausbau.

---

## Phasen P0–P6

| Phase | Inhalt | Status | Kategorie |
|---|---|---|---|
| **P0** | Fundament, Konventions-Import, Skelett, Vorregistrierung, MC-Spike | ✅ T-D3 bestätigt | — |
| **P1** | `train-core`: Durchstich — Graph, Physik, Energie, Simulator, Blockabschnitte (Z1–Z4) | ✅ abgeschlossen (101 Tests, Z1–Z4 grün, Determinismus bestätigt) | A |
| **P2** | Verschleiß + Ports — `condition`, `wear`, `PowerSupply`/`MaintenanceSupply` (Z6, Z7) | ⏳ nächster Schritt | A |
| **P3** | Planer — grobe Auflösung, dieselbe Physikfunktion, Z5 property-based (Z5) | ⏳ | A |
| **P4** | `train-mc`: erste spielbare Version — Gleiseditor, Persistenz, Token⇄Entity, Leitstand (Z9–Z11) | ⏳ | B |
| **P5** | Fahrplan + Lokführer — `Schedule`, `ManualOverride`, Z5-Vertragsgrenze (Z8) | ⏳ | A + B |
| **P6** | Auswertung M1-Strang — `M1_RESULTS.md` (A/B getrennt), `M1_DECISION.md` | ⏳ | docs-only |

> **Status-Symbole:** ✅ erledigt · ⏳ ausstehend · 🔄 aktiv

---

## Drop-Order unter Zeitdruck (Plan §10)

> Unter Druck fällt zuerst, was oben steht. **Nie umsortieren.**

1. P6 (Auswertung) — verschiebbar, nicht streichbar
2. P5 (Fahrplan / Lokführer) — die Mod ist ohne ihn spielbar
3. Die *Schönheit* der Leitstand-Ausgabe — Chat-Text statt GUI ist erlaubt
4. Rendering-Qualität in P4 — hässlich reicht
5. Die Leitstand-*Ausgabe* selbst fällt nie (Z11)
6. P3 (Planer) fällt nie — Kern-Orakel
7. Z7 (Softlock-Schutz) fällt nie

---

## Siehe auch

- `TRAKTION_OVERALL_PLAN.md` §5 — vollständige Phasenordnung mit Done-When-Kriterien
- `TRAKTION_OVERALL_PLAN.md` §4 — Ziele Z1–Z11 mit Zuordnung zu Phasen
- `phase1/CLAUDE.md` — Phasen-Kopf P1 (abgeschlossen) mit Build-Log und Session-stopped-Block
- `phase0/CLAUDE.md` — Phasen-Kopf P0 (historisch, abgeschlossen)
- `ARCHITECTURE.md` — Architektur-Schnitt (train-core / train-mc)
