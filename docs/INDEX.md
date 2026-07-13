---
status: live
purpose: L0-Karte des Doku-Systems. Eine Zeile pro Doku, Glyph + Größe + read-when-Hook. Navigationseinstieg für jede Session — verhindert Directory-Scans.
read-when: Session-Start; vor der Suche nach einer Doku; bevor eine neue .md erstellt wird (One-Liner hier ergänzen, im selben Commit)
detail: L0
up: ./CLAUDE.md
updated: 2026-07-13
---

# Traktion — Doku-Index

> Navigation über up/down-Links in den Header-Cards, nicht per Verzeichnissuche.
> Glyphen: 📜 Konzept/Plan · 🔒 gelockt/frozen · 🏗 As-Built · 📊 Messung · 🧭 Einstieg

---

## Einstieg (L0/L1)

| Doku | Größe | Wann lesen |
|---|---|---|
| 🧭 `CLAUDE.md` | L1 | Session-Start; Single Source of Truth |
| 🧭 `AGENTS.md` | L1 | Session-Start; harness-neutraler Einstieg |
| 📜 `TRAKTION_OVERALL_PLAN.md` | L2 | vor jeder Architektur-Entscheidung; §2/§3/§4/§9 |

## Konventionen & Konzepte

| Doku | Größe | Wann lesen |
|---|---|---|
| 📜 `docs/CONVENTIONS.md` | L2 | vor jeder neuen .md; vor jedem Commit; bei Zweifel, ob eine Regel gilt |

## Phasenpläne

| Doku | Größe | Wann lesen |
|---|---|---|
| 📜 `docs/plans/PHASE0_PLAN.md` | L2 | Ausführung von P0; Referenz für Folgesessions |

## Messung (M1-Strang)

| Doku | Größe | Wann lesen |
|---|---|---|
| 🔒 `M1_PREREGISTRATION.md` | L2 | vor jedem Trial (Konstanten prüfen); bei Auswertung (Regel anwenden); nie zum Editieren |
| 📊 `m1/trials.jsonl` | — | wird vom Operator geführt, nicht vom Agenten |
