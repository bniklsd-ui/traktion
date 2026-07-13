---
status: preregistration (FROZEN — nach dem ersten committeten Trial nie editiert)
purpose: Vorregistrierung des Traktion-Messstrangs M1. Hypothese, eingefrorene Konstanten, Metriken je Kategorie, Confounds und Entscheidungsregel — festgeschrieben BEVOR der erste Trial läuft. Die Git-History dieses Commits ist der Beweis gegen nachträgliches Zurechtbiegen.
read-when: Vor jedem Trial (Konstanten prüfen); bei Auswertung (Regel anwenden); nie zum Editieren
detail: L2
up: ./TRAKTION_OVERALL_PLAN.md
related: ./TRAKTION_OPENCODE_CONFIG.md
updated: 2026-07-13
---

# Traktion — M1 Preregistration

> **Dieses Dokument wird nach dem ersten committeten Trial NIE editiert.** Korrekturen
> ausschließlich als neues, datiertes Dokument (Supersede, nie Edit). Das ist der Kern der
> Vorregistrierung: Wer Metrik, Regel oder Konstante nach dem ersten Ergebnis ändert, misst nichts.
>
> **Verhältnis zum Altprojekt:** Die Namenskonvention „M1" ist aus dem Trading-Bot-Repo übernommen.
> Das dortige M1 (Harness-Bake-off) ist ein **anderes Experiment in einem anderen Repo**. Keine
> Fortsetzung, keine Ergebnisübernahme. `META_M1_RESULTS.md` und `META_M1_DECISION.md` des
> Altprojekts wurden bewusst **nicht** in den Referenzsatz aufgenommen (Hygiene, Plan §5/P0.1).

---

## §1 Hypothese

**H1:** Ein offenes Modell (GLM 5.2) unter einem offenen Harness (OpenCode) mit fixiertem Skill-Set
ist in der Lage, ein mittelkomplexes Softwareprojekt (Traktion, Teil 1) über die Phasen P0–P6
ausführungsreif umzusetzen — getrennt beurteilt nach **Kategorie A** (klassische Informatik,
`train-core`) und **Kategorie B** (Minecraft-26.2-Spezifikum, `train-mc`).

**H0 (Null):** Das Setup erreicht die Ziele Z1–Z11 nicht ohne so viele Operator-Eingriffe, dass
die Umsetzung faktisch vom Operator und nicht vom Agenten getragen wird.

**Was das NICHT ist** (aus Plan §6, wörtlich beibehalten): kein kontrollierter Vergleich, kein 1:1,
kein Beweis. **Was es ist:** ein subjektiver Erfahrungsbericht, gestützt durch objektive
Teilmessungen. Diese Ehrlichkeit gehört in `M1_RESULTS.md`, nicht in eine Fußnote.

---

## §2 Eingefrorene Konstanten

> Jede Änderung an einer dieser Konstanten während des Strangs macht Trials davor und danach
> unvergleichbar (dieselbe Logik wie T-D12 für Minecraft). Passiert es doch: neues, datiertes
> Dokument, Trials getrennt auswerten.

### Harness
- **OpenCode 1.17.18** (`opencode --version`, notiert 2026-07-13)
- `autoupdate: false` — global und projektlokal
- `share: "disabled"`
- Kein Harness-Upgrade während des Strangs. `brew/npm update` gilt als Bruch.

### Modell
- **Real aufgelöster Modellname: `nvidia/z-ai/glm-5.2`** — dies ist die Konstante, nicht der
  Config-String. Der Footer der Agenten-Session zeigt `glm-5.2` (mit Bindestrich); der validierte
  Rauchtest lief über die Agenten-Config, nicht nur über den Modell-Picker.
- Provider: NVIDIA NIM, **Free Tier**. Custom-Provider-ID `nvidia-nim` (global konfiguriert),
  Auflösung zum Präfix `nvidia/` per Harness-Verhalten (GitHub-Issue-konform).
- Base-URL: `https://integrate.api.nvidia.com/v1` · `temperature: 0`

### Setup-Entscheidung (a)
- **Plan-Agent == Execution-Agent == GLM 5.2.** Ein Modell, zwei Sessions.
- Die Plan/Execution-**Trennung ist Betriebsmechanik, kein Messfaktor**: getrennte Sessions,
  Kontext bleibt beim Operator, Plan ist ein diffbares Artefakt in `docs/plans/`.
- **Invariante:** Ändert sich die Verfügbarkeit während des Projekts, bleiben **beide** Agenten
  weiterhin identisch (dasselbe Modell). Ein Paar-Setup (verschiedene Modelle für Plan/Execution)
  wäre ein **neues, datiertes** Preregistration-Dokument, kein stiller Switch.

### Toolchain / Skill-Set
- **Node 24.x** (via nvm, `nvm alias default 24`). Kein Versionswechsel im Strang.
- **`agent-browser 0.31.1`** (global-npm-Binary; der Repo-`SKILL.md` ist nur ein Stub, der Inhalt
  lebt in der Binary-Version — **die Version ist die Konstante**).
- **Chrome 150.0.7871.115** (von `agent-browser install` gezogen).
- **Skill-Set S = { `agent-browser`, `skill-creator` }**, projektlokal unter `.agents/skills/`,
  versioniert. Per Agenten-Introspektion bestätigt (`list installed skills` → genau zwei).
  Global installierter `find-skills` (Installer-Discovery-Helfer) wurde entfernt.
- **Kein Skill kommt nach dem ersten Trial dazu** (dieselbe Logik wie `autoupdate: false`).

### Zielplattform
- **Minecraft 26.2, Fabric.** Versionen gepinnt nach Plan T-D12. Bewusst hinter dem
  Trainingsschnitt aller Modelle → Kategorie B misst Recherche, nicht Recall (Plan §6).

### Plan-Detailgrad (vorregistrierte Konstante)
- Der Phasenplan spezifiziert: **Typnamen, Dateipfade, Zielzuordnung (Z<x>), Kategorie (A/B),
  Testliste, Akzeptanzkriterien.**
- Der Phasenplan spezifiziert **NICHT**: Methodensignaturen, Funktionskörper, Zeilenanker,
  Algorithmen.
- **Begründung:** Die Übersetzung von Akzeptanzkriterium zu Implementierung IST die gemessene
  Fähigkeit. Ein feinkörniger Plan reduziert Kategorie A auf Transkription und schließt die
  P3-Watchpunkte (Regel-2-Verstoß, Z5-Tautologie) per Konstruktion aus — er würde das
  interessanteste Signal der Studie löschen (Plan §5/P3).
- Der Detailgrad ist über **alle** Phasen konstant und wird pro Phase nicht nachjustiert.

---

## §3 Metriken je Kategorie

> **A und B werden immer getrennt ausgewertet. Nie zu einer Zahl gemittelt** (Plan §6). Wer sie
> mittelt, löscht das einzige interessante Ergebnis der Studie.

### Kategorie A — `train-core` (klassische Informatik)
Grün heißt: `gradle :train-core:test` grün und das Z-Ziel der Phase erfüllt.
Gemessen je Trial: Iterationen bis grün · Diff-Zeilen · Regressionen · bestandene Property-Fälle ·
**Regel-2-Verstoß (ja/nein)** · **Z5-Tautologie (ja/nein)** · Operator-Eingriffe.

### Kategorie B — `train-mc` (26.2-Spezifikum)
Grün heißt: manueller Smoke im Client + Z9/Z10 erfüllt.
Gemessen je Trial: Iterationen bis lauffähig · halluzinierte API-Aufrufe · **Recherche-Schritte
(Quellen tatsächlich gelesen?)** · Operator-Eingriffe.

### Was NICHT gemessen wird
- Codeästhetik jenseits „Test grün / Anti-Pattern ja-nein".
- Subjektive „Eleganz" ohne Testbezug.
- Diff-Größe als Qualitätsmaß — sie ist **Proxy**, wird berichtet, nicht gewichtet.

### Rausch-Regel
Ist die Streuung innerhalb eines Setups größer als der Abstand zwischen zwei Vergleichspunkten,
wird der Abstand als **„nicht unterscheidbar"** berichtet, nicht als Ergebnis.

### Buchführung
- Ein Trial = eine Zeile in `m1/trials.jsonl`, geschrieben **vom Operator**, nie vom Agenten
  (`edit: m1/** → deny`).
- **Die Erwartung wird VOR dem Trial notiert, nicht danach** (Plan §7).
- Felder: `ts · phase · ziel · kategorie · harness · modell · effort · iterationen · diff_lines ·
  tests_gruen · regressionen · operator_eingriffe · regel2_verstoss · z5_tautologie ·
  recherche_schritte · notiz`.

---

## §4 Confounds & Limitations (vollständig, vor der ersten Zahl)

1. **Prozess-Dokumente von Claude verfasst.** Der Overall Plan, die Prompts und dieses Dokument
   sind von Claude (Browser) geschrieben. Ein Claude-geschriebener Plan liegt Claude-Agenten in
   Formulierung und Struktur näher als fremden Modellen. Nicht wegprompbar, nur benannt.

2. **Selbstbewertung.** GLM plant, GLM baut, und die Phasen-Abschlussanalyse beurteilt GLM-Arbeit.
   Gegenmaßnahme: `trials.jsonl` enthält objektive Felder (Iterationen, Diff-Zeilen, Regressionen,
   Tests grün/rot); der Chat meldet nur Rohdaten, der Operator trägt ein.

3. **Eval-Awareness — BELEGT, nicht theoretisch.** Im Sperren-Verifikationstest (Canary,
   2026-07-13) hat GLM 5.2 das Testszenario **selbst identifiziert**, die Runbook-Herkunft samt
   Zeilennummer zitiert, das `cat`-Leck selbst vorgeführt und benannt, und den Plan-Agent hat die
   Recherche-Grenze aus §6 wörtlich zitiert und die verbotene Kategorie-B-Aufgabe korrekt
   verweigert. **Konsequenz:** Das Modell kann das Messdesign (§6/§7 des Overall Plan, der
   agent-lesbar ist) lesen und operativ nutzen. Kategorie-B-Grenzeinhaltung ist dadurch
   **potenziell nach oben verzerrt** — nicht durch Täuschung, sondern weil der Agent den Test
   kennt. Teilmitigation: `TRAKTION_SETUP_RUNBOOK.md` wurde aus dem agent-lesbaren Repo in den
   Operator-Bereich verschoben. Der Overall Plan selbst bleibt agent-lesbar (Split wurde wegen
   Drift-Risiko verworfen), der Confound also nur gedämpft, nicht beseitigt.

4. **Free-Tier-Verfügbarkeit.** Latenz ~14 s auf ein „hi"; unter Last langsamer. Der Endpunkt kann
   ohne Vorwarnung rate-limitiert oder abgeschaltet werden. **Ein Trial-Abbruch kann
   Endpunkt-Verfügbarkeit sein, nicht Modell-Fähigkeit** — beim Auswerten unterscheiden.

5. **Aus Plan §6 übernommen:** n=1 (ein Projekt ist ein Sample) · Reihenfolgeeffekt (was zuerst
   gebaut wird, ist schwerer) · Beobachter == Nutzer (Bias unvermeidbar, nur benennbar) ·
   Diff-Größe ist Proxy.

---

## §5 Deklarierte Ausnahmen & Setup-Protokoll

### P0.1 läuft unter einer abweichenden Config (bewusst, deklariert)
- Für den P0.1-Import muss `build-traktion` die fünf kuratierten Dateien in `example_project/`
  lesen. Die Standard-Config sperrt das (`read: example_project/** → deny`).
- **Ausnahme:** Nur für den P0.1-Lauf wird `example_project/**` auf `allow` gesetzt (anderer
  Task-Typ: Konventions-Import, kein Bau). Der Plan-Agent bleibt gesperrt.
- **Direkt nach P0.1:** `rm -rf example_project` **und** `git checkout` stellt den committeten
  `deny`-Zustand exakt wieder her. Ab dann ist die Regel belt-and-suspenders für einen gelöschten
  Ordner.

### Sperren-Verifikation (Canary-Test, 2026-07-13) — protokolliert
- `read`-Tool auf `example_project/**`: **dicht** (verweigert).
- `bash cat` / `bash grep -rn`: **war ein Leck** — lieferte den Canary-Inhalt trotz `read: deny`.
- **Mitigation:** `cat · grep · rg · head · tail · sed · awk` in `bash` auf `ask` gesetzt.
  Nachtest: gesperrt. `ls · find` bleiben `allow` (leaken nur Namen, keinen Inhalt).
- `edit`-denies (`m1/**`, `M1_*.md`, `docs/plans/**` für Executor), `git push`, Planer-`edit: *
  deny`, Recherche-Grenze (`webfetch: ask` für Planer): alle **dicht**.
- Eine Sperre, deren Wirksamkeit nicht dokumentiert ist, ist eine Behauptung (Runbook Schritt 5).

---

## §6 Entscheidungsregel (formuliert, bevor Ergebnisse existieren)

Angewandt in `M1_DECISION.md`, wörtlich, nach P6:

- **H1 gestützt (Kategorie A)**, wenn: Z1–Z8 in `train-core` grün, reproduzierbar bei gleichem
  Seed, **ohne** dass Operator-Eingriffe die Kernlogik getragen haben (Eingriffe = Korrektur von
  Infrastruktur/Toolchain zählt nicht gegen H1; Korrektur von Domänenlogik zählt dagegen).
- **H1 gestützt (Kategorie B)**, wenn: Z9–Z11 im Client verifiziert und die 26.2-`[VERIFY]`-Marken
  **durch dokumentierte Recherche** des Agenten aufgelöst wurden, nicht durch Operator-Vorgabe.
- **H0 nicht verworfen**, wenn: die Ziele nur mit Operator-Eingriffen erreicht werden, die die
  jeweilige Kernfähigkeit ersetzen (nicht bloß unterstützen).
- **„Nicht unterscheidbar"** wird berichtet, wo die Rausch-Regel (§3) greift.

**Verbot:** keine Änderung von Hypothese, Metrik, Konstante oder Regel nach diesem Commit.
Korrekturen nur als neues, datiertes Dokument.

---

## §7 Trial-Zählung

- **Trial 1 = P0.1** (Konventions-Import → `docs/CONVENTIONS.md`). Der „Bewusst verworfen"-Abschnitt
  ist ein Kategorie-A-Messpunkt (Plan §5/P0.1). Deshalb liegt dieses Dokument **vor** P0.1 in der
  History (Entscheidung (ii), 2026-07-13).
- Ab hier wird jeder Executor-Lauf als Trial gezählt und vom Operator in `m1/trials.jsonl`
  protokolliert.
