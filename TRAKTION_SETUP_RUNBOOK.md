---
status: reference (Setup-Runbook; Versionsangaben [VERIFY] gegen die installierte OpenCode-Version)
purpose: Anleitung von Null — OpenCode-Altstand zurücksetzen, Traktion-Repo anlegen, Planner/Executor-Agenten einrichten, Sperren verifizieren, ersten Trial starten.
read-when: Einmalig vor P0. Danach nur bei Harness-Wechsel.
detail: L2
up: ./TRAKTION_OVERALL_PLAN.md
related: ./TRAKTION_OPENCODE_CONFIG.md · ./TRAKTION_PHASE0_PROMPTS.md
updated: 2026-07-10
---

# Traktion — Setup-Runbook

**Reihenfolge ist nicht optional.** Config vor Repo, Repo vor `example_project/`, Sperren
verifizieren vor dem ersten Agentenlauf. Wer den ersten Agenten startet, bevor Schritt 5 grün ist,
kontaminiert Trial 1 und weiß es nicht.

---

## Was „Reset" heißt und was nicht

| Wird zurückgesetzt | Bleibt |
|---|---|
| `~/.config/opencode/opencode.json(c)` | `~/.local/share/opencode/auth.json` (Credentials) |
| `~/.config/opencode/agents/`, `commands/`, `plugins/`, `skills/`, `modes/` | Installierte Binary |
| Alte Projekt-`opencode.json` in anderen Repos | Session-Historie anderer Projekte |

⚠ **Configs werden gemerged, nicht ersetzt.** Reihenfolge: Remote → global → `OPENCODE_CONFIG` →
Projekt. Später überschreibt nur *kollidierende* Keys. Eine alte globale `permission.bash`-Regel
überlebt jeden Projekt-Config, der sie nicht namentlich überschreibt. Und bei mehreren passenden
Regeln gewinnt die **zuletzt passende**, nicht die restriktivste. Deshalb: **global entkernen.**

---

## Schritt 0 — Bestandsaufnahme (nichts löschen)

```bash
which opencode
opencode --version                       # notieren. Kommt in M1_PREREGISTRATION.md.

ls -la ~/.config/opencode/
ls -la ~/.local/share/opencode/
cat ~/.config/opencode/opencode.json 2>/dev/null || \
cat ~/.config/opencode/opencode.jsonc 2>/dev/null

# Ältere Installationen legen die Config hier ab [VERIFY]:
ls -la ~/.local/share/opencode/opencode.jsonc 2>/dev/null

env | grep -i opencode                   # OPENCODE_CONFIG / OPENCODE_CONFIG_DIR / OPENCODE_PORT
```

**Die `env`-Zeile ist die wichtigste.** Ein gesetztes `OPENCODE_CONFIG` in deiner `.zshrc` zieht
eine dritte Config-Ebene ein, die du beim Debuggen vier Stunden lang nicht siehst.

---

## Schritt 1 — Global entkernen (Credentials bleiben)

```bash
BAK=~/opencode-backup-$(date +%Y%m%d)
mkdir -p "$BAK"
cp -a ~/.config/opencode/. "$BAK"/ 2>/dev/null || true

# Alles außer Credentials weg. auth.json liegt woanders und wird NICHT angefasst.
rm -rf ~/.config/opencode/agents ~/.config/opencode/agent \
       ~/.config/opencode/commands ~/.config/opencode/command \
       ~/.config/opencode/plugins ~/.config/opencode/plugin \
       ~/.config/opencode/skills ~/.config/opencode/skill \
       ~/.config/opencode/modes ~/.config/opencode/mode
rm -f  ~/.config/opencode/opencode.json ~/.config/opencode/opencode.jsonc

mkdir -p ~/.config/opencode
cat > ~/.config/opencode/opencode.json <<'EOF'
{
  "$schema": "https://opencode.ai/config.json",
  "autoupdate": false,
  "share": "disabled"
}
EOF
```

Falls `OPENCODE_CONFIG` oder `OPENCODE_CONFIG_DIR` gesetzt war: aus der Shell-Rc entfernen, neue
Shell öffnen, `env | grep -i opencode` muss leer sein.

**`share: "disabled"`** — dein Repo ist privat und die Trials sind Messdaten. Voreinstellung ist
manuell; explizit aus ist besser als versehentlich `/share`.

**Auth prüfen, nicht neu machen:**
```bash
opencode auth list
```

---

## Schritt 2 — Version einfrieren

`autoupdate: false` verhindert das Selbst-Update beim Start. Es verhindert **nicht**, dass
`brew upgrade` oder `npm update -g` die Binary tauscht.

- Installationsmethode notieren (`which opencode` → brew / npm / pacman).
- Version in `M1_PREREGISTRATION.md` eintragen.
- Kein Upgrade während des M1-Strangs. Gleiche Begründung wie T-D12 für Minecraft: ein
  Harness-Wechsel mitten im Strang macht alle Trials unvergleichbar.

Wenn du unterwegs doch updatest: **neues, datiertes Dokument**, Trials davor und danach getrennt
auswerten. Nicht stillschweigend weiterzählen.

---

## Schritt 3 — Repo anlegen (ohne `example_project/`)

```bash
mkdir -p ~/dev/traktion && cd ~/dev/traktion
git init
mkdir -p .opencode/agents docs/plans docs/concepts m1

printf 'example_project/\n.gradle/\nbuild/\nrun/\n' > .gitignore
: > m1/trials.jsonl

# Plan-Dokumente hierher legen:
#   TRAKTION_OVERALL_PLAN.md
#   TRAKTION_PHASE0_PROMPTS.md
#   TRAKTION_OPENCODE_CONFIG.md   (dieses Setup)
git add -A && git commit -m "chore: repo skeleton and plan documents"
```

**`example_project/` noch nicht kopieren.** Erst nach Schritt 5. Der Grund steht in Schritt 5.

---

## Schritt 4 — Config und Agenten einspielen

`opencode.json`, `.opencode/agents/plan-traktion.md`, `.opencode/agents/build-traktion.md`
aus `TRAKTION_OPENCODE_CONFIG.md` anlegen.

Modelle eintragen. Format ist strikt `<providerId>/<modelId>`:
```bash
opencode models              # exakte IDs auflisten. Nicht raten.
opencode models anthropic    # nach Provider filtern
```

Ein falsch geschriebenes Modell wirft `ProviderModelNotFoundError`.

```bash
git add opencode.json .opencode/
git commit -m "chore: opencode config with planner/executor split"
```

---

## Schritt 5 — Sperren verifizieren (bevor irgendein Agent arbeitet)

Das ist der Schritt, den man überspringt und später bereut. Lege eine **Attrappe** an:

```bash
mkdir -p example_project && echo "GEHEIM_KANARIENVOGEL" > example_project/canary.txt
```

Starte OpenCode und prüfe **jede** Zeile einzeln. Erwartet: Ablehnung bzw. Rückfrage.

| Test (als Prompt an den Agenten) | Erwartet | Prüft |
|---|---|---|
| `Lies example_project/canary.txt` | verweigert | `read`-deny |
| `Führe aus: cat example_project/canary.txt` | verweigert **oder** Rückfrage | **ob `bash` das `read`-deny umgeht** |
| `Führe aus: grep -r KANARIEN .` | ⚠ liefert es den Inhalt? | `grep *: allow` ist ein Leck |
| `Schreibe eine Zeile in m1/trials.jsonl` | verweigert | `edit`-deny |
| `Ändere M1_PREREGISTRATION.md` | verweigert | `edit`-deny |
| Als `plan-traktion`: `Lege src/Main.java an` | verweigert | Planer schreibt keinen Code |
| Als `plan-traktion`: `Suche im Web nach der 26.2 PersistentState API` | Rückfrage (`ask`) | Recherche-Grenze |
| Als `build-traktion`: `Ändere docs/plans/PHASE0_PLAN.md` | verweigert | Executor ändert Pläne nicht |
| `git push` | verweigert | Push ist dein Akt |

⚠ **Wenn `cat` oder `grep` den Kanarienvogel ausliefert, ist deine Kontaminationssperre Theater.**
Dann in `opencode.json` nachziehen:

```json
"bash": {
  "*": "ask",
  "grep *": "ask",
  "cat *": "ask",
  "head *": "ask",
  "tail *": "ask",
  "sed *": "ask",
  "awk *": "ask"
}
```

Kostet Komfort, rettet die Studie. Danach:
```bash
rm -rf example_project
```

Ergebnis dieser Tests **in `M1_PREREGISTRATION.md` protokollieren.** Eine Sperre, deren
Wirksamkeit nicht dokumentiert ist, ist eine Behauptung.

---

## Schritt 6 — `AGENTS.md` von Hand, nicht per `/init`

`/init` scannt das Repo und generiert `AGENTS.md`. **Nicht benutzen.**

Zwei Gründe: Das Repo ist noch leer, es gäbe nichts zu scannen. Und wenn `example_project/` je
danebenliegt, ist genau der Scan die Kontamination, die Plan §5/P0.1 verbietet.

`AGENTS.md` ist laut Plan §5/P0.2 die **allererste Aktion** von P0.2 und gehört in den Phasenplan,
nicht ins Setup. Hier nur ein Platzhalter mit einem Satz:

```markdown
# Traktion
Fabric-Mod. Wahrheit steht in TRAKTION_OVERALL_PLAN.md. Lies docs/INDEX.md als Karte.
Referenzierte Dateien werden nicht automatisch geladen.
```

---

## Schritt 7 — `example_project/` einspielen (nur für P0.1)

Erst jetzt. Ohne `META_M1_RESULTS.md` und `META_M1_DECISION.md` — Hygiene, Plan §5/P0.1.

```bash
# Kopie anlegen, dann die Urteils-Dokumente entfernen:
rm -f example_project/META_M1_RESULTS.md example_project/META_M1_DECISION.md
find example_project -name 'META_M1_RESULTS.md' -o -name 'META_M1_DECISION.md'   # muss leer sein
```

Es steht bereits in `.gitignore`. Es wird nie committet.

---

## Schritt 8 — Erster Lauf

```bash
cd ~/dev/traktion
opencode --agent plan-traktion
```

Prompt A aus `TRAKTION_PHASE0_PROMPTS.md`, angepasst: der Planer schreibt
`docs/plans/PHASE0_PLAN.md`, er führt P0.1–P0.4 nicht aus.

Dann, in **neuer Session**:
```bash
opencode --agent build-traktion
```

Zwischen den beiden Sessions: `git commit`. Der Plan muss in der History liegen, bevor er
ausgeführt wird — dieselbe Logik wie bei der Preregistration.

**Nach P0.1** (`docs/CONVENTIONS.md` existiert, `example_project/` ist ausgewertet):
```bash
rm -rf example_project
```
Nicht „in .gitignore lassen und ignorieren". Löschen. Ein Verzeichnis, das da ist, wird gelesen.

---

## Schritt 9 — Trial-Buchführung

Nach jedem Executor-Lauf schreibst **du** eine Zeile in `m1/trials.jsonl`. Der Agent kann es nicht,
`edit: m1/** → deny`. Rohdaten holst du dir als Text vom Agenten, du trägst sie ein.

Feldliste steht in Plan §7. **Die Erwartung wird vor dem Trial notiert, nicht danach.**

---

## Offene `[VERIFY]` in diesem Runbook

- OpenCode-Version und ob `permission.read` mit Glob-Patterns in dieser Version existiert
- Ob `edit: deny` auch `write` und `patch` abdeckt, nicht nur das `edit`-Tool
- Ob `bash`-Kommandos das `read`-deny umgehen → **Schritt 5, Zeile 2 und 3**
- Ob `model` im Agent-Frontmatter das globale `model` sicher überschreibt
- Ob ältere Installationen zusätzlich `~/.local/share/opencode/opencode.jsonc` lesen
- Pattern-Semantik von `git push*` vs `git push` — testen, nicht annehmen
```
