---
status: reference (Start-Config für OpenCode; NIM-Block gelockt am 2026-07-12, Skill-Liste offen bis Prereg)
purpose: Initiale OpenCode-Konfiguration für Traktion — Trennung von Planungs- und Ausführungssession, mechanische Durchsetzung der Plan-§9-Anti-Patterns über Permissions statt über Prosa, NIM-Provider und Skill-Freeze.
read-when: Setup des Repos; Wechsel der Modellzuordnung; vor jedem M1-Trial
detail: L2
up: ./TRAKTION_OVERALL_PLAN.md
related: ./TRAKTION_CHAT_PROMPTS.md · ./TRAKTION_PHASE0_PROMPTS.md · ./TRAKTION_SETUP_RUNBOOK.md
updated: 2026-07-12
---

# Traktion — OpenCode Start-Config

**Kernidee:** Was in Plan §9 als Anti-Pattern steht und mechanisch verhinderbar ist, wird
mechanisch verhindert. Ein Prompt ist eine Bitte. Eine `deny`-Regel ist eine Grenze.

| Wird durch Prompt geregelt | Wird durch Permission geregelt |
|---|---|
| „Physik existiert einmal" | `m1/**` ist für Agenten unschreibbar |
| „Planer ruft Simulator nicht auf" | `example_project/**` ist unlesbar |
| „[VERIFY] stehen lassen" | Planer darf keinen Code schreiben |
| „kein `net.minecraft.*` in core" | Planer darf `webfetch`/Skills nur mit Rückfrage bzw. gar nicht |

---

## VM-Baseline (gelockt 2026-07-12)

| Fakt | Wert | Herkunft |
|---|---|---|
| OpenCode-Version | **1.17.18** | `opencode --version`, frische VMware-Ubuntu-VM |
| Installation | `curl -fsSL https://opencode.ai/install \| bash` | PATH via `source ~/.bashrc` aktiviert |
| Provider | **NVIDIA NIM** (custom, OpenAI-kompatibel) | `/models`-Picker zeigt „GLM 5.2 (NIM)", Rauchtest grün (~14 s) |
| Modell | **`z-ai/glm-5.2`** | Anzeigename NVIDIA-Seite; per Rauchtest als `model`-String bestätigt |
| Auth | `auth.json` (Weg B), **kein `apiKey` in der Config** | `opencode auth login` → „Other" → ID `nvidia-nim` |

⚠ **Alle drei Konstanten (Version, Provider, Modell) sind ab Trial 1 eingefroren.** Kein Upgrade,
kein Provider-Wechsel, keine spontane Umstellung auf eines der anderen freien Modelle im Picker
(DeepSeek V4 Flash, MiMo V2.5, Big Pickle) — dieselbe T-D12-Logik wie beim Minecraft-Pin. Ein
Faktorwechsel mitten im Strang macht Trials unvergleichbar. Gehört wörtlich in `M1_PREREGISTRATION.md`.

⚠ **Free-Tier ist ein Studienrisiko, kein Kostenvorteil.** ~14 s Latenz für „hi" heißt: unter Last
drohen Timeouts in langen Agent-Läufen. Bricht ein Trial ab, unterscheide **Modell-Fähigkeit** von
**Endpunkt-Verfügbarkeit**, sonst kontaminierst du Kategorie-B-Zahlen. Der Endpunkt kann ohne
Vorwarnung wegbrechen oder rate-limitieren — als bekanntes Risiko in die Prereg.

---

## Dateibaum

```
opencode.json                      ← Projekt-Config (Permissions, Agent-Modellzuordnung)
~/.config/opencode/opencode.json   ← global: autoupdate:false + NIM-Provider-Block (unten)
~/.local/share/opencode/auth.json  ← Credentials, NIE ins Repo
AGENTS.md                          ← harness-neutraler Einstieg (Plan §5/P0.2)
.opencode/agents/plan-traktion.md  ← Planungssession
.opencode/agents/build-traktion.md ← Ausführungssession
.opencode/skills/<name>/SKILL.md   ← eingefrorenes Skill-Set (projektlokal, versioniert)
docs/plans/PHASE<N>_PLAN.md        ← Output der Planungssession
docs/concepts/                     ← zeitlose Design-Dokumente, keine Pläne
m1/trials.jsonl                    ← nur Nikinger. deny für alle Agenten.
```

> **Benennung:** `docs/concepts/` ist im Altprojekt für *zeitlose* Konzepte belegt. Ein Phasenplan
> ist datiert und verfällt. Deshalb `docs/plans/`. Vermischst du beides, weißt du in P4 nicht mehr,
> welches Dokument noch gilt.

> **Provider-Split:** Der NIM-Zugang steht in der **globalen** Config (unten). Die
> **Modellzuordnung pro Agent** gehört ins Repo (`.opencode/agents/*.md`), versioniert — sonst
> weißt du in P4 nicht mehr, welches Modell welchen Trial gefahren hat.

---

## NIM-Provider (global, `~/.config/opencode/opencode.json`)

Weg B (Auth über `auth.json`): **kein `apiKey`-Feld**, nur `baseURL`. OpenCode löst den Key über
die Provider-ID `nvidia-nim` auf.

```json
{
  "$schema": "https://opencode.ai/config.json",
  "autoupdate": false,
  "provider": {
    "nvidia-nim": {
      "npm": "@ai-sdk/openai-compatible",
      "name": "NVIDIA NIM",
      "options": {
        "baseURL": "https://integrate.api.nvidia.com/v1"
      },
      "models": {
        "z-ai/glm-5.2": {
          "name": "GLM 5.2 (NIM)",
          "limit": { "context": 128000, "output": 8192 }
        }
      }
    }
  }
}
```

⚠ **`limit` ist geschätzt — [VERIFY]** gegen die echte GLM-5.2-Modellkarte. Falsche Werte werfen
keinen Fehler, sondern schneiden still Kontext ab.

---

## `opencode.json` (Projekt-Root)

```json
{
  "$schema": "https://opencode.ai/config.json",
  "autoupdate": false,
  "default_agent": "build-traktion",
  "instructions": ["docs/CONVENTIONS.md"],

  "permission": {
    "read": {
      "*": "allow",
      "example_project/**": "deny"
    },
    "edit": {
      "*": "allow",
      "m1/**": "deny",
      "M1_PREREGISTRATION.md": "deny",
      "M1_RESULTS.md": "deny",
      "M1_DECISION.md": "deny",
      "TRAKTION_OVERALL_PLAN.md": "ask"
    },
    "bash": {
      "*": "ask",
      "grep *": "allow",
      "rg *": "allow",
      "ls *": "allow",
      "find *": "allow",
      "cat *": "allow",
      "git status": "allow",
      "git diff*": "allow",
      "git log*": "allow",
      "git add *": "allow",
      "git commit *": "ask",
      "git push*": "deny",
      "./gradlew *": "allow",
      "gradle *": "allow",
      "rm *": "deny",
      "sudo *": "deny"
    },
    "skill": {
      "*": "deny",
      "browser": "allow",
      "systematic-debugging": "allow"
    },
    "webfetch": "allow",
    "external_directory": "ask"
  }
}
```

**`skill: "*": "deny"` als Default ist Absicht.** Nur namentlich erlaubte Skills feuern. Jeder
nicht-gelistete Skill (versehentlich global installiert) ist damit stumm. Die konkreten Namen
(`browser`, `systematic-debugging`) sind **Platzhalter bis zum Prereg-Lock** — siehe Skills-Abschnitt.

**`git push` global `deny`:** Push ist dein Akt, nicht der des Agenten. Er markiert das Ende eines
Trials.

---

## Skills — projektlokal, eingefroren (offen bis Prereg-Lock)

**Prinzip:** Ein Skill ist eine Variable. Für eine Studie gilt: so wenige wie möglich, alle
versioniert, alle in jedem Trial identisch, keine nach Trial 1 dazu.

### Zwei Kisten

| Kandidat | Kiste | Urteil |
|---|---|---|
| **Browser** | Domäne/Recherche | rein (entschieden) — **definiert mit, was Kategorie B misst** |
| **Systematic-Debugging** | Disziplin | borderline rein — addiert etwas, das die Prompts nicht abdecken |
| Test-Writing / TDD | Disziplin | raus — Prompt erzwingt es schon |
| Code-Review | Disziplin | raus — Review macht der Planungschat/Operator |
| Commit-/Git-Hygiene | Disziplin | raus — im Prompt |
| Java/Gradle/**Fabric/MC**-Spezifisches | Domäne | **verboten** — injiziert genau das 26.2-Wissen, dessen Abwesenheit B misst |

### ⚠ Zwei Mechanik-Fallen

1. **Skills feuern nicht-deterministisch** (semantische Ähnlichkeit zur Anfrage, on-demand). Das
   kollidiert mit `temperature: 0` und dem Determinismus aus Regel 8: derselbe Skill ist mal im
   Kontext, mal nicht. Gegenmaßnahme: Set klein halten + Planer bekommt gar keine Skills
   (`tools: skill: false`, siehe Agent-Frontmatter).
2. **`npx skills add` NICHT benutzen.** Die CLI streut Skills nach `~/.claude/skills/`,
   `~/.agents/skills/` und global — OpenCode liest alle. Das macht die Kontaminations-Gegenprobe
   ungültig und ist unsichtbar. **Von Hand** nach `.opencode/skills/<name>/SKILL.md` kopieren,
   committen.

### Audit-Kriterium vor dem Kopieren jeder `SKILL.md`

> Steht stack-spezifisches Wissen drin (Java-Idiome, Gradle-Tasks, Fabric-Registries,
> MC-Versionsdetails)? → **raus**, kontaminiert B.
> Reiner Prozess (wie debuggen, wie strukturieren)? → **Kandidat**.

Quellen zum Selbst-Prüfen (nicht auditiert, kein Paket ist „freigegeben"): `addyosmani/agent-skills`,
`superpowers`, das Verzeichnis auf skills.sh. **Keine als geprüft übernehmen, ohne die `SKILL.md`
gegen das Kriterium oben gelesen zu haben.**

### Installation

```bash
mkdir -p .opencode/skills
# pro Skill: SKILL.md-Ordner von Hand kopieren nach .opencode/skills/<name>/
git add .opencode/skills && git commit -m "chore: freeze skill set for M1"
```

---

## `.opencode/agents/plan-traktion.md`

```markdown
---
description: Schreibt den Phasenplan für Traktion. Schreibt keinen Code, führt nichts aus.
mode: primary
model: nvidia-nim/z-ai/glm-5.2
temperature: 0
tools:
  skill: false
permission:
  read:
    "*": allow
    "example_project/**": deny
  edit:
    "*": deny
    "docs/plans/**": allow
    "docs/INDEX.md": allow
    "docs/concepts/**": allow
  bash:
    "*": deny
    "grep *": allow
    "rg *": allow
    "ls *": allow
    "find *": allow
    "git log*": allow
    "git diff*": allow
  webfetch: ask
---

Du planst Phase P<N> der Fabric-Mod "Traktion". Du schreibst keinen Code und führst
nichts aus. Dein einziges Artefakt ist docs/plans/PHASE<N>_PLAN.md.

Lesereihenfolge: AGENTS.md · docs/INDEX.md · CLAUDE.md ·
TRAKTION_OVERALL_PLAN.md (§2 Locks, §3 Hard Rules, §4 Ziele, §5 Phase, §9 Anti-Patterns) ·
PHASE<N-1>_HANDOVER.md (bei P0: existiert nicht — sag es, konstruiere nichts).
Referenzierte Dateien werden nicht automatisch geladen. Öffne sie on-need-to-know.

RECHERCHE-GRENZE — verletzt du sie, ist die Studie beschädigt:
  ERLAUBT: Toolchain und Build-Infrastruktur (Gradle, Loom, Loader, fabric-api,
           Java-Version, Testbibliotheken). Infrastruktur, keine gemessene Fähigkeit.
  VERBOTEN: jede Minecraft-/Fabric-RUNTIME-API von 26.2 aufzulösen — Persistenz-API,
           Entity-Lifecycle, Blaze3D, Registries, Packets. Diese bleiben [VERIFY].
  Grund: Plan §6, Kategorie B misst, ob die AUSFÜHRUNGSSESSION recherchieren und lesen
  kann. Löst du das vorweg, misst du nichts mehr.
  webfetch ist auf "ask" gesetzt. Jede Anfrage ist ein sichtbarer Messpunkt.

Der Plan ist HARNESS- und MODELLNEUTRAL. Er nennt kein Modell, keinen Harness und ist
nicht auf eine Modellstärke kalibriert. Der Detailgrad steht in M1_PREREGISTRATION.md
und wird pro Phase nicht nachjustiert.

Der Plan enthält:
  - Architektur-Entscheidungen dieser Phase (Tabelle, IDs im Schema T-D<n>)
  - Schritt-Sequenz, jeder Schritt einzeln committbar
  - pro Schritt: Typnamen, Dateipfade, Testliste, Akzeptanzkriterien
  - Zuordnung zu Zielen Z<x> (Plan §4) und Kategorie A oder B (Plan §7)
  - Step 0: Altlasten (Namensdrift, tote Verweise). Darf leer sein, nicht fehlen.
  - Step 0b: Doc-Drift. Code ist Wahrheit. Doku fixen, Historie nie umschreiben.

Was seit dem letzten Repo-Stand gedriftet sein könnte: [VERIFY]. Nie als gesichert.

Stelle ZUERST alle offenen Fragen an mich. Dann schreibe.
Stößt du auf ein Anti-Pattern aus Plan §9 — auch im eigenen Entwurf: anhalten, benennen,
fragen. Nicht stillschweigend korrigieren.
Widerspricht diese Anweisung dem Plan: der Plan gewinnt, und du sagst es mir.
Nach ~20-30 Tool-Calls: "## Session stopped"-Block schreiben, anhalten.
```

> **`tools: skill: false` beim Planer** ist die mechanische Umsetzung der Recherche-Grenze auf
> Skill-Ebene: Der Planer sieht die `<available_skills>`-Sektion gar nicht, kann also nicht über
> einen Browser-Skill vorweg recherchieren, was der Executor recherchieren soll.

---

## `.opencode/agents/build-traktion.md`

```markdown
---
description: Führt einen Phasenplan von Traktion aus. TDD, atomare Commits.
mode: primary
model: nvidia-nim/z-ai/glm-5.2
temperature: 0
permission:
  read:
    "*": allow
    "example_project/**": deny
  edit:
    "*": allow
    "m1/**": deny
    "M1_*.md": deny
    "docs/plans/**": deny
    "TRAKTION_OVERALL_PLAN.md": deny
  skill:
    "*": deny
    "browser": allow
    "systematic-debugging": allow
  webfetch: allow
---

Du führst docs/plans/PHASE<N>_PLAN.md aus. Der Plan ist die Anweisung,
TRAKTION_OVERALL_PLAN.md ist die Wahrheit. Bei Widerspruch gewinnt der Overall Plan,
und du sagst es mir.

Lesereihenfolge: AGENTS.md · docs/INDEX.md · CLAUDE.md · phase<N>/CLAUDE.md
("## Session stopped" zuerst) · docs/plans/PHASE<N>_PLAN.md ·
TRAKTION_OVERALL_PLAN.md §2 §3 §4 §9.

[VERIFY]-Marken im Plan sind DEIN Auftrag. Recherchiere, dekompiliere, lies Quellen.
Verlass dich nicht auf Erinnerung an 1.21.x — Minecraft 26.2 liegt hinter deinem
Trainingsschnitt. Jedes 1.21er-Tutorial ist potenziell falsch.
Ungeprüfte [VERIFY]-Marken bleiben stehen. Du löschst keine, die du nicht geprüft hast.

Harte Grenzen (Plan §3):
  - Kein net.minecraft.*, kein NBT, kein ItemStack in train-core.
  - Die Physikformel existiert genau EINMAL. Planer und Simulator rufen dieselbe Funktion.
  - Der Planer ruft NIE den Simulator auf. Sonst ist Z5 tautologisch.
  - Fixed dt, geordnete Iteration, gesäter Zufall. Kein Wall-Clock, kein HashSet
    in der Physikschleife.
  - Kein Interface ohne zwei heute benennbare Implementierungen.
  - Kein roher OpenGL-Call. Nur Blaze3D.

Arbeitsweise:
  - TDD in train-core. Ein Subtask ist nicht fertig, bevor `./gradlew test` grün ist.
  - Atomare Commits, Format `<scope>: <imperative>`.
  - Commit ⇒ Note-Update: Statuszeile + "## Session stopped" im selben Commit.
  - Jede neue .md: L1-Header-Card + One-Liner in docs/INDEX.md, im selben Commit.
  - Code ist Wahrheit > Konzept-Dokument > Status-Prosa.

Anti-Pattern aus Plan §9 — auch im eigenen Entwurf: anhalten, benennen, fragen.
Nicht stillschweigend korrigieren, nicht umgehen. Diese Momente sind Messpunkte.

Nach ~20-30 Tool-Calls: "## Session stopped"-Block schreiben, anhalten.
```

---

## Was die Permissions mechanisch erzwingen

| Plan-Regel | Mechanik |
|---|---|
| §9 „Agent liest `example_project/` nach P0" | `read: example_project/** → deny`, für beide Agenten |
| §7 „Kein Agent schreibt in `trials.jsonl`" | `edit: m1/** → deny` |
| §6 „Preregistration wird nie editiert" | `edit: M1_*.md → deny` |
| Planer schreibt keinen Code | `edit: * → deny`, nur `docs/plans/**` offen |
| Planer recherchiert nicht über Skills | `tools: skill: false` |
| Executor ändert den Plan nicht nachträglich | `edit: docs/plans/** → deny` |
| Recherche-Grenze (§6, Kategorie B) | Planer `webfetch: ask` + keine Skills, Executor `webfetch: allow` + Browser-Skill |
| Skill-Freeze | `skill: "*": deny` + namentliche Allow-Liste, projektlokal versioniert |
| T-D12 Version-Freeze | `autoupdate: false` |

Was **nicht** mechanisch geht: Regel 2, Z5-Tautologie, Determinismus des Modells selbst. Die
bleiben Prompt + Review + Test. Deshalb stehen sie in §7 als *Metrik*, nicht als Verbot.

---

## [VERIFY] vor dem ersten Trial

**Erledigt (2026-07-12):**
- ~~OpenCode-Version~~ → 1.17.18, gepinnt via `autoupdate: false`
- ~~NIM Base-URL~~ → `https://integrate.api.nvidia.com/v1`, Rauchtest grün
- ~~NIM Modell-ID~~ → `z-ai/glm-5.2`, per Picker+Rauchtest als `model`-String bestätigt

**Offen:**
- **GLM-5.2 `limit`-Werte** (context/output) gegen die echte NIM-Modellkarte. Falsch = stille Truncation.
- Ob `bash: "cat *"` das `read`-deny auf `example_project/**` umgeht.
  **Wenn ja: `cat`, `head`, `tail`, `sed` in `bash` auf `ask` setzen.** Sonst ist die
  Kontaminationssperre Theater. (Runbook Schritt 5.)
- Ob `deny` auf `edit` auch `write` und `patch` abdeckt, nicht nur `edit`.
- Ob `skill: "*": deny` verlässlich greift und ob `tools: skill: false` die
  `<available_skills>`-Sektion wirklich komplett entfernt (gegen 1.17.18 testen).
- Ob `model: nvidia-nim/z-ai/glm-5.2` im Agent-Frontmatter sauber auflöst (Doppel-Slash im
  Modell-ID-Teil — Pattern testen, nicht annehmen).
- `git push*` vs `git push` — Pattern-Matching testen.

---

## Was jetzt noch fehlt (in dieser Reihenfolge)

1. **Entscheidung `(a)` oder `(b)`** — Planer fixiert, oder Setup-Paar. Vor Prereg.
   Aktuell fahren beide Agenten `z-ai/glm-5.2` ⇒ faktisch (a) mit GLM als Konstante.
   Formal committen, nicht implizit lassen.
2. **Skill-Liste locken** — `browser` (welcher genau?) + ggf. `systematic-debugging`.
   Namen in Prereg als Konstante, `SKILL.md`-Inhalte gegen das Audit-Kriterium geprüft.
3. **`M1_PREREGISTRATION.md`** — inkl.: Detailgrad des Plans als Konstante · Modell/Version/Provider
   als Konstanten · Skill-Set namentlich · die beiden Confounds (Claude-geschriebener Plan,
   Selbstbewertung im Handover) · Free-Tier-Risiko · Entscheidung (a)/(b).
4. **`AGENTS.md`** — harness-neutraler Einstieg, Plan §5/P0.2, allererste Aktion.
5. **`docs/CONVENTIONS.md`** — via Prompt A aus `TRAKTION_PHASE0_PROMPTS.md`.
