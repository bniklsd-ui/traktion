---
status: plan (Konzept-Ebene — alle Vorab-Entscheidungen gelockt, siehe §2)
purpose: Overall Plan der Fabric-Mod "Traktion" — Mission, Kernprinzip, Architektur-Schnitt, Phasenordnung P0–P6, orthogonaler Mess-Strang M1 (Modell-/Harness-Studie). Einstiegsdokument für Planung und Build.
read-when: Session-Start jeder Phase; vor jeder Architektur-Entscheidung; vor Beginn des M1-Strangs
detail: L1/L2 (Body ist L2, §0–§4 sind L1-tauglich)
up: ./CLAUDE.md
down: ./ROADMAP.md · ./ARCHITECTURE.md · ./docs/CONVENTIONS.md · ./docs/concepts/
supersedes: TRAKTION_OVERALL_PLAN v1 (2026-07-10, vor Lock von T-O1…T-O4)
updated: 2026-07-10
---

# Traktion — Overall Plan (v2)

> Fabric-Mod. Ein Zugnetz ist kein Fahrzeug, sondern ein Betrieb.
>
> **Author:** Browser-Planungschat, 2026-07-10.
> **Audience:** Planungschat (Architektur) + Coding-Agent (Ausführung).
> **Drift-Konvention:** Alles, was gegen die reale Minecraft-/Fabric-API oder gegen As-Built-Code
> geprüft werden muss, ist **[VERIFY]** markiert. **Code ist Wahrheit.** Der Planungschat hat
> Versionsstände per Websuche ermittelt (2026-07-10), nicht ausprobiert.
> **Doc-Layers-Konvention gilt** (importiert in P0, siehe `docs/CONVENTIONS.md`): jede neue `.md`
> bekommt eine L1-Header-Card (≤15 Zeilen YAML) + einen One-Liner in `docs/INDEX.md`
> **im selben Commit**. Genau ein `## Session stopped`-Block pro Phasen-`CLAUDE.md`.

---

## §0 Mission

**Traktion** simuliert ein elektrifiziertes Schienennetz, das der Spieler plant, baut und am Leben
hält. Züge sind Verbraucher an einem Netz, das der Spieler selbst errichtet hat. Der Reiz liegt
nicht darin, dass ein Zug fährt, sondern darin, dass er fährt, **weil ein System existiert, das
ihn fahren lässt** — und langsamer wird, wenn das System schlampig gebaut ist.

| Teil | Inhalt | Status |
|---|---|---|
| **Teil 1 — Zugnetz** | Graph, Physik, Energie, Verschleiß, Planer, Fahrplan, Leitstand | **dieser Plan** |
| **Teil 2 — Industrie** | Kraftwerke, Fabriken, Arbeiter-NPC, Fracht | **geparkt** (§8) |

⚠ **Vorab bekannte Konsequenz (kein Bug):** Teil 1 ist ohne Teil 2 spielbar, aber nicht
*reichhaltig*. Der Spieler befüllt und repariert von Hand. Das ist Absicht — die Handarbeit ist
die dauerhafte Rückfallebene (Regel 4), kein Platzhalter.

---

## §1 Kernprinzip

**Bauprinzip: Die Domäne kennt kein Minecraft.**

```
train-core/   reines Java. Kein Fabric, kein net.minecraft.*, kein NBT.
              Graph · Physik · Energie · Verschleiß · Planer · Simulation · Fahrplan
              → `gradle :train-core:test` läuft in Sekunden, ohne Client-Start.

train-mc/     dünner Adapter. Blöcke, Entities, Rendering, Packets, Persistenz, GUI.
              → übersetzt Weltzustand in Zahlen und Zahlen zurück in Weltzustand.
```

**`train-core` macht:** Graphstruktur + Invarianten · Fahrphysik · Energiebilanz und
Spannungsabfall · Verschleiß und seine Rückwirkung · Blockabschnitte, Kollisionsfreiheit,
Deadlock-Erkennung · Fahrplan inkl. Override · **den Planer, mit derselben Physikfunktion wie
die Simulation** (Regel 2, §4).

**`train-mc` macht NUR:** Weltinteraktion (Gleiseditor) · Token ⇄ Entity · Persistenz ·
Rendering, GUI, Netcode · **Zahlen liefern**, die der Kern verlangt.

Wenn du in `train-core` ein `net.minecraft.*`-Import brauchst, ist der Schnitt falsch. Stopp.

---

## §2 Entscheidungen

### Gelockt (Nikinger, 2026-07-10)

| # | Thema | Lock |
|---|---|---|
| **T-D1** | Loader | **Fabric.** Konsequenz: keine mitgelieferte Energy-API — eigene Bilanz in `train-core`, keine Fremd-API im Kern |
| **T-D2** | Gleis-Repräsentation | **Graph-basiert** (MTR-Muster). Der Graph ist die Wahrheit, Blöcke sind Dekoration. Vanilla-Look = **Renderproblem** |
| **T-D3** | Zug-Repräsentation | **Token ist die Wahrheit, Entity ist Sichtbarmachung.** Entity nur in Spielernähe. Fällt exakt mit der Grenze automatisch/manuell zusammen |
| **T-D4** | Verschleiß-Ursache | **Nutzungsbasiert, nicht zeitbasiert.** `wear += f(masse, v)` pro Durchfahrt. Ungenutzte Strecke verrottet nie |
| **T-D5** | Verschleiß-Wirkung | **Kontinuierlich, nie blockierend.** `condition ∈ [0,1]` → Widerstand ↑ → Spannungsabfall ↑ → Fahrzeit ↑ |
| **T-D6** | Erweiterbarkeit | **Genau zwei Ports** (§3.2). Ein Interface nur, wo die zweite Implementierung **heute benannt** werden kann |
| **T-D7** | Fracht | Der Kern kennt **Masse**, keine Items. `payloadMassKg` ist eine Zahl von außen. Fracht ist **kein Port** |
| **T-D8** | Manuelle Steuerung | Kein Sondermodus. Spieler mutiert `Schedule.override`. Keine zweite Codebahn |
| **T-D9** | Semantische Gleise | `RailKind ∈ {NORMAL, STATION, HIGH_SPEED, DEPOT, TERMINUS}` auf der Kante. Blockabschnitte werden aus der Topologie **abgeleitet** |
| **T-D10** | Industrie | **Nicht in Teil 1.** Kein Arbeiter-NPC, kein Rezept, kein Kraftwerk-Block im MVP |
| **T-D11** | Handarbeit | Handreparatur und Handbefüllung sind **immer** möglich. Dauerhafte Regel (Regel 4) |

### Gelockt am 2026-07-10 (vormals T-O1…T-O4, nach Recherche + Nikinger-Entscheid)

| # | Thema | Lock |
|---|---|---|
| **T-D12** | Zielversion + Pinning | **Minecraft 26.2, Fabric.** Exakt gepinnt, **kein Update-Chasing während M1.** Ein Versionswechsel mitten im Strang macht alle Trials unvergleichbar. Stand 2026-07-10: `fabric-api 0.154.0+26.2` · `loader 0.19.3` · `loom 1.17` · `gradle 9.5.1` · Java **[VERIFY]**. **Bewusste Studiendesign-Entscheidung** — siehe §6 |
| **T-D13** | Physik-Integration | **Sub-Tick, fixed dt, deterministisch.** `dt = TICK_SECONDS / N_SUBSTEPS` (Default 4), semi-implizites Euler. **Keine Wall-Clock, keine variable Schrittweite, jemals.** Geordnete Collections in der Physikschleife, gesäter Zufall — Property-Tests brauchen Reproduzierbarkeit. Sub-Tick dient der **Energie-Integration**, nicht dem Rendering (das ist Interpolation in `train-mc`) |
| **T-D14** | Planer ≠ Simulator | **Asymmetrie ist Pflicht** (§4, Z5). Gleiche Physikfunktion, verschiedene Auflösung und Scope |
| **T-D15** | Persistenz | Graph liegt als **welt-attached Persistent State pro Dimension** — **nicht** per Chunk (Kanten kreuzen Chunk-Grenzen; ein Unload würde den Graph zerreißen). `train-core` liefert ein `GraphSnapshot`-Record, **`train-mc` kodiert** (Regel 1 bleibt). **`schemaVersion: int` + Migrationshaken ab Tag 1.** Ein Blob genügt bis ~10⁵ Knoten. ⚠ Das Welt-Datenformat hat sich in 26.1 stark geändert — **[VERIFY]** den API-Namen gegen echte 26.2-Quellen, nicht gegen 1.21er-Tutorials |
| **T-D16** | Rendering | **Kein roher OpenGL-Call.** Nur Blaze3D-API. 26.2 führt ein experimentelles Vulkan-Backend ein und OpenGL soll entfernt werden; primitives Rendering (§10) umgeht das Thema vollständig |

### Offen — später, nicht blockierend

| # | Frage | Wann |
|---|---|---|
| **T-O5** | Zweiter Druckfaktor (Nachfrage, Aufträge, Fristen) neben Zeit | Nach P4, wenn die Mod spielbar ist. Überlastet den MVP (§8) |

---

## §3 Hard Rules (keine Ausnahmen)

1. **Kein `net.minecraft.*`, kein NBT in `train-core`.** Nicht „nur kurz", nicht „nur für den Test".
   Das ist der einzige Grund, warum dieses Projekt testbar ist.

2. **Physik existiert genau einmal.** Planer und Simulator rufen **dieselbe Funktion** auf.
   Eine zweite Implementierung derselben Formel macht Z5 zu einem Test, der sich selbst bestätigt.

3. **Ein Interface nur bei zwei heute benennbaren Implementierungen.** Keine Abstraktion für
   vorgestellte Zukünfte. Umbenennen ist billig; eine falsche Abstraktion zurückbauen nicht.

4. **Kein Softlock. Niemals.** Aus **jedem** erreichbaren Zustand muss der Spieler durch eigene
   Arbeit herauskommen. Handbefüllung und Handreparatur sind immer verfügbar, nur langsam.
   → testbar als Invariante, Z7.

5. **Verschleiß bestraft Nutzung, nicht Existenz.** Eine gebaute, aber ungenutzte Strecke verfällt
   nicht. Zeitbasierter Verfall macht aus der Mod eine Hausaufgabe.

6. **Reparatur ist ab Tag 1 automatisierbar.** Nicht: „Spieler klickt 6000 Gleisblöcke an."

7. **Der Kern kennt keine Items.** Nur `double`. Ein `ItemStack` in `train-core` → stoppen.

8. **Determinismus ist nicht verhandelbar.** Fixed dt, geordnete Iteration, gesäter Zufall.
   Ohne ihn sind die Property-Tests flaky und der M1-Strang unmessbar.

---

## §3.2 Die zwei Ports (gesamte geplante Erweiterbarkeit)

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

## §4 Inhaltliche Ziele (X tut erfolgreich Y)

| # | Ziel | Ort | Automatisch prüfbar |
|---|---|---|---|
| **Z1** | Graph-Mutationen erhalten Invarianten: kein verwaister Knoten, keine Kante ohne zwei Endpunkte, `RailKind` gesetzt | core | ✅ |
| **Z2** | Blockabschnitte aus Topologie; zwei Züge nie im selben Abschnitt; Deadlocks erkannt | core | ✅ |
| **Z3** | Leistungsbedarf = f(Masse, v, Steigung); Rekuperation bergab; Zug hält bei Unterversorgung | core | ✅ |
| **Z4** | Spannungsabfall = f(Distanz, `condition`); Unterwerk setzt zurück | core | ✅ |
| **Z5** | **Planer-Prognose ≈ Simulation, < 5 %, bei beliebigem Netzzustand** | core | ✅ **Kern-Orakel** |
| **Z6** | Verschleiß entsteht aus Nutzung, degradiert kontinuierlich, blockiert nie total | core | ✅ Langlauf-Sim |
| **Z7** | **Bootstrap-Invariante:** aus jedem Zustand ist Handarbeit ein Ausweg | core | ✅ **Softlock-Schutz** |
| **Z8** | Schedule-Override (`SERVE`/`SKIP`/`TERMINATE`) ändert die Route korrekt | core | ✅ |
| **Z9** | Token ⇄ Entity ohne Zustandsverlust | mc | ⚠ teilweise |
| **Z10** | Graph überlebt Server-Neustart und Chunk-Unload verlustfrei; `schemaVersion` migriert | mc | ⚠ teilweise |
| **Z11** | **Leitstand macht Ineffizienz sichtbar:** Soll- vs. Ist-Fahrzeit + benannte Ursache (Abschnitt, Zustand, Verlust) | core + mc | ✅ Zahlenteil |

**Acht von elf Zielen leben ohne Minecraft.**

### Z5 — das Kern-Orakel, und warum es fast tautologisch geworden wäre

Ein Planer, der einfach den Simulator laufen lässt, beweist `f(x) == f(x)`. **Das ist die
naheliegende Lösung, und genau deshalb wird ein Agent sie bauen.** Die Asymmetrie ist Pflicht:

| | Simulator | Planer |
|---|---|---|
| Schrittweite | fein (Substeps, T-D13) | grob (1 Tick oder pro Kante analytisch) |
| Züge | alle | einer |
| Verkehr | Blockabschnitte, Reservierung | ignoriert |
| Override | berücksichtigt | ignoriert |
| **Physikfunktion** | **`Physics.requiredPowerW`** | **`Physics.requiredPowerW`** |

Regel 2 bleibt intakt: **eine** Formel, zwei Auflösungen. Z5 prüft, ob das grobe Modell eine
gültige Approximation des feinen ist. Das ist eine echte, verletzbare Aussage.

**Property-based, nicht fallbasiert:**

> Für jedes zufällig generierte Netz mit zufälligem Verschleißzustand, zufälliger Last und
> zufälligem Höhenprofil gilt: `|planner.predict(r) − simulator.run(r)| / simulator.run(r) < 0.05`

**Vertragsgrenze:** Die Prognose ist **nicht anwendbar** (nicht *falsch*), wenn (a) ein Override
aktiv war **oder** (b) fremder Verkehr die Route belegt hat. Ohne diesen Vertrag wird Z5 flaky,
und man debuggt wochenlang einen Test, der recht hat.

### Z11 — warum der Leitstand kein Komfort ist

Wenn der einzige Druck Zeit ist (§2, T-O4-Lock), sind Verschleiß und Handreparatur **dieselbe
Währung**. Der Spieler kann jederzeit mit den Schultern zucken. Das ist als Sandkasten in Ordnung —
aber nur, wenn er **sieht**, dass er 30 % Durchsatz verliert.

> **Die Mod hat kein Spiel, wenn die Ineffizienz unsichtbar ist.**

Damit ist der Leitstand die Kernschleife, nicht ein Nice-to-have. Konsequenz in §10.

---

## §5 Phasenordnung (strikt)

> Jede Phase ist vollständig testbar, bevor die nächste beginnt. Kein Vorgreifen.
> Nach P4 fährt ein Zug im Spiel. Alles danach ist Ausbau.

### P0 — Fundament, Konventions-Import & Messinstrument

> Startet im **Browser-Chat** (neuer Kontext). Der Chat plant und liefert Prompts;
> die Ausführung passiert im Agenten-Harness. Prompts: `PHASE0_PROMPTS.md`.

**P0.1 — Konventions-Import (allererste Aktion, docs-only)**

Eine Sicherheitskopie des Trading-Bot-Repos liegt unter `example_project/` (read-only Referenz).

- [ ] **Kuratierte Teilmenge lesen** — nicht das ganze Repo (§9, Anti-Pattern):
      `example_project/CLAUDE.md` · `ROADMAP.md` · `docs/DOC_LAYERS_CONVENTION.md` ·
      `docs/concepts/meta_m1_harness_plan.md` (§0–§4, das **Messdesign**) ·
      `META_M1_PREREGISTRATION.md` (Methode)
- [ ] **`docs/CONVENTIONS.md`** erzeugen: was wird **übernommen**, was **explizit verworfen**.
      Übernommen: Header-Cards · `docs/INDEX.md` · `## Session stopped` · `[VERIFY]`-Konvention ·
      „Code ist Wahrheit" · Commit ⇒ Note-Update · atomare Commits · Vorregistrierung ·
      Anti-Shopping-Regel · Rausch-Regel · Drop-Order.
      Verworfen: alles Python-Spezifische (pytest-Zählungen, `requests`-Konvention, Keyring,
      Envelope-Kontrakt) · phasennummerierte Verzeichnisse (hier: Gradle-Module) · Broker/VETO-Semantik.
- [ ] `example_project/` in `.gitignore` **oder** nach P0 löschen. **Kein Agent liest es nach P0.**

⚠ **Zwei Hygiene-Regeln, die nicht optional sind:**

1. **Kopiere die Methode, nicht die Ergebnisse.** `META_M1_RESULTS.md` und `META_M1_DECISION.md`
   des Altprojekts gehören **nicht** in `example_project/`. Ein Modell soll kein Urteil über
   sich selbst lesen, bevor es gemessen wird. Kostet nichts, vermeidet eine dumme Angriffsfläche.
2. **Scannen ist Kontamination.** Ein Agent, der ein Python-Repo scannt, schleppt Python-Reflexe
   in ein Java-Projekt. Die Antwort auf „Kontext mitnehmen" ist **ein destilliertes Dokument**,
   kein dauerhaft mitlaufendes Referenz-Repo.

**P0.2 — Skelett**

- [ ] Gradle-Multi-Modul: `train-core` (plain Java) + `train-mc` (Fabric Loom 1.17) **[VERIFY]**
- [ ] Root-`CLAUDE.md` · `docs/INDEX.md` · `AGENTS.md` (harness-neutraler Einstieg) — **erste Aktion**
- [ ] `ROADMAP.md`, `ARCHITECTURE.md` (Stubs, zeigen auf dieses Dokument)
- [ ] JUnit + Property-Testing (jqwik o. ä.) in `train-core` **[VERIFY: läuft unter Gradle 9.5.1?]**
- [ ] Log-Konventionen + Testmatrix festgeschrieben (§7)

**P0.3 — Vorregistrierung (kritischer Commit)**

- [ ] **`M1_PREREGISTRATION.md`** committet, **bevor** irgendein Trial läuft. Git-History = Beweis.

**P0.4 — MC-Spike (Wegwerf, eigener Branch, wird nie gemerged)**

- [ ] Fährt eine Entity entlang eines hartkodierten Pfades? Despawnt sie bei Spielerentfernung
      und wird bei Annäherung zustandserhaltend rekonstruiert? → **beweist T-D3 technisch**,
      bevor drei Phasen darauf gebaut werden.
- [ ] Nebenbei: `[VERIFY]` Java-Version, `PersistentState`-API-Name in 26.2, jqwik/Gradle.

**Done when:** `gradle test` grün (leer) · `docs/CONVENTIONS.md` existiert und benennt Verworfenes ·
Spike beantwortet T-D3 mit ja/nein · Preregistration liegt **vor** dem ersten Trial in der History ·
`example_project/` ist stillgelegt.

⚠ Widerlegt der Spike T-D3, ist der **Plan** falsch, nicht die Realität. Dann zurück zu §2.

---

### P1 — `train-core`: Durchstich · *Kategorie A*

Ein Zug fährt in einem Unit-Test von A nach B und wird langsamer, wenn Leistung fehlt.
**Kein Minecraft. Kein Verschleiß. Kein Planer.**

- [ ] `RailGraph` — Knoten, Kanten, `RailKind`, `gradient`, Länge (Z1)
- [ ] `Consist` — `carCount`, `tareMassKg`, `payloadMassKg` (T-D7)
- [ ] `Physics` — **eine** Funktion `requiredPowerW(consist, speed, gradient)` (Regel 2)
- [ ] `PowerGrid` — Bedarf, Angebot über `PowerSupply`, Unterwerk-Reset (Z4 ohne `condition`)
- [ ] `Simulator` — fixed-dt-Substep-Schleife, Token bewegt sich, Unterversorgung bremst (Z3, T-D13)
- [ ] `BlockSection` — Reservierung, Kollisionsfreiheit, Deadlock-Erkennung (Z2)

**Done when:** Z1–Z4 grün; `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken;
zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8).

---

### P2 — Verschleiß + Ports · *Kategorie A*

- [ ] `condition ∈ [0,1]` auf Kante und Oberleitung
- [ ] `wear += f(masse, v)` pro Durchfahrt (T-D4)
- [ ] `condition` → Widerstand → Spannungsabfall (T-D5, schließt Z4 ab)
- [ ] `PowerSupply` / `ManualGenerator` · `MaintenanceSupply` / `PlayerLabor`
- [ ] **Z7-Invariantentest:** aus zufälligem Verfallszustand ist Handarbeit ein Ausweg

**Done when:** Z6 + Z7 grün. Ein Langlauf über 10.000 Ticks Dauerbetrieb degradiert messbar und
blockiert nie total.

---

### P3 — Planer · *Kategorie A — der interessanteste Punkt der Studie*

- [ ] `Planner.predict(route, consist, netState) → Prognose` — **grobe Auflösung, ein Zug,
      kein Verkehr** (T-D14). Ruft **dieselbe** `Physics`-Funktion (Regel 2)
- [ ] Engpass-Erkennung: wo bricht die Leistung ein, was hilft
- [ ] Soll-/Ist-Vergleich als Zahlen (Z11, Kern-Anteil)
- [ ] **Z5 property-based**, Generator für Netze/Verschleiß/Last/Profil; Vertragsgrenze durchgesetzt

**Done when:** Z5 grün über ≥ 1000 generierte Fälle, kein Fall über 5 % Abweichung.

⚠ **Watch (Messpunkt, kein Ärgernis):** Ein Agent wird versuchen, entweder die Formel zu
duplizieren (Regel-2-Verstoß) **oder** den Planer den Simulator aufrufen zu lassen
(tautologisches Z5). Beides **protokollieren**, nicht stillschweigend wegkorrigieren. Genau hier
zeigt sich, ob ein Modell Architektur versteht oder Code produziert.

---

### P4 — `train-mc`: erste spielbare Version · *Kategorie B*

- [ ] Gleiseditor (Knoten setzen, verbinden, `RailKind` zuweisen)
- [ ] Graph-Persistenz nach T-D15 inkl. `schemaVersion` (Z10)
- [ ] Token ⇄ Entity (Z9), Rendering-Interpolation client-seitig
- [ ] Hässliches Rendering. Ein Minecart-Reskin genügt. **Kein roher OpenGL-Call** (T-D16)
- [ ] Handbefüllung / Handreparatur als Weltinteraktion (Regel 4, 6)
- [ ] **Leitstand-Block mit Soll/Ist-Ausgabe** (Z11) — nicht droppable, §10

**Done when:** Ein Zug fährt im Spiel von A nach B, wird bei Stromknappheit langsamer, die Strecke
nutzt sich ab, der Leitstand nennt den Engpass, ein Server-Neustart verliert nichts.

**Das ist der erste Meilenstein, der etwas wert ist.** Wenn hier Schluss ist, existiert eine Mod.

---

### P5 — Fahrplan + Lokführer · *Kategorie A + B*

- [ ] `Schedule` mit `Stop{node, behavior}` (Z8)
- [ ] `ManualOverride` — Spieler auf dem Zug mutiert `override`
- [ ] Z5-Vertragsgrenze im Code durchgesetzt: nach Override → „nicht anwendbar"

---

### P6 — Auswertung des M1-Strangs · *docs-only*

- [ ] `M1_RESULTS.md` (dated Snapshot), **A und B getrennt**
- [ ] `M1_DECISION.md` — Anwendung der vorregistrierten Regel, wörtlich

---

## §6 Mess-Strang M1 (orthogonal, hochzählbar)

> Namenskonvention aus dem Altprojekt übernommen. **Das dortige M1 (Harness-Bake-off) ist ein
> anderes Experiment in einem anderen Repo.** Keine Fortsetzung, keine Ergebnisübernahme.

**Frage:** *Eignen sich offene Modelle unter einem offenen Harness für ein mittelkomplexes
Softwareprojekt?*

**Was das nicht ist:** kein kontrollierter Vergleich, kein 1:1, kein Beweis.
**Was es ist:** ein **subjektiver Erfahrungsbericht, gestützt durch objektive Teilmessungen.**
Diese Ehrlichkeit gehört in `M1_RESULTS.md`, nicht in eine Fußnote.

### Vorregistrierung (Pflicht, vor dem ersten Trial)

`M1_PREREGISTRATION.md` enthält:

- **Hypothese** + Nullhypothese, wörtlich, vor jeder Zahl
- **Metriken je Kategorie** (§7), inkl. was *nicht* gemessen wird
- **Entscheidungsregel**, formuliert, bevor Ergebnisse existieren
- **Rausch-Regel:** Ist die Streuung innerhalb eines Setups größer als der Abstand zwischen den
  Setups, wird der Abstand als **„nicht unterscheidbar"** berichtet
- **Versionswahl als Studiendesign-Entscheidung** (siehe unten), explizit begründet
- **Verbot:** keine Änderung von Metrik, Regel oder Design nach diesem Commit. Korrekturen nur
  als neues, dated Dokument (Supersede, nie Edit)

### ⚠ Die Versionswahl ist Teil des Messdesigns

Minecraft 26.2 liegt hinter dem Trainingsschnitt **aller** eingesetzten Modelle. Neues
Versionsschema (26.x statt 1.21.x), geändertes Welt-Datenformat (26.1), veränderte
Yarn/Loom-Landschaft. Jedes Tutorial und jeder Trainingsdaten-Schnipsel aus der 1.21.x-Ära ist
potenziell falsch.

> **Kategorie B misst deshalb nicht „kennt das Modell Fabric", sondern
> „kann der Agent recherchieren, dekompilieren und lesen, statt sich zu erinnern."**

Das ist fairer (beide Modelle sind gleich ahnungslos) und interessanter — aber riskanter für den
Projektfortschritt. Die Alternative wäre 1.21.11 gewesen (maximale Trainingsabdeckung, misst
**Recall**). **Bewusst verworfen.** Das gehört in die Preregistration, nicht in die Retrospektive.

### ⚠ Das Ergebnis, das du nicht wegmitteln darfst

**Kategorie A** (`train-core`) ist klassische Informatik: Graphen, Zustandsautomaten, numerische
Bilanzen. Davon haben alle Modelle viel gesehen.
**Kategorie B** (`train-mc`) ist 26.2-Spezifikum, das kein Modell gesehen hat.

> **Getrennt auswerten. Immer.** Wer A und B zu einer Zahl mittelt, löscht das einzige
> interessante Ergebnis der ganzen Studie.

### Vorab notierte Schwachstellen

- n ist klein. Ein Projekt ist ein Sample.
- Reihenfolgeeffekt: Was zuerst gebaut wird, ist schwerer.
- Der Beobachter ist der Nutzer. Bias ist unvermeidbar, nur benennbar.
- Diff-Größe ist eine Proxy-Metrik, kein Qualitätsmaß. Wird berichtet, nicht gewichtet.

---

## §7 Testmatrix & Log-Konventionen

| Kategorie | Ort | Grün heißt | Metriken |
|---|---|---|---|
| **A** | `train-core` | `gradle :train-core:test` grün, Z-Ziel erfüllt | Iterationen bis grün · Diff-Zeilen · Regressionen · Property-Fälle bestanden · **Regel-2-Verstoß ja/nein** · **Z5-Tautologie ja/nein** |
| **B** | `train-mc` | manueller Smoke im Client + Z9/Z10 | Iterationen bis lauffähig · halluzinierte API-Aufrufe · Recherche-Schritte (Quellen gelesen?) · manuelle Eingriffe des Operators |

**Kein Trial ohne vorher notierte Erwartung.** Wer erst nach dem Ergebnis weiß, was er erwartet
hat, hat nichts gemessen.

**Logs:**
- Ein Trial = eine Zeile in `m1/trials.jsonl`, geschrieben **vom Operator**, nicht vom Agenten
- Felder: `ts` · `phase` · `ziel` · `kategorie` · `harness` · `modell` · `effort` · `iterationen` ·
  `diff_lines` · `tests_gruen` · `regressionen` · `operator_eingriffe` · `regel2_verstoss` ·
  `z5_tautologie` · `recherche_schritte` · `notiz`
- Code-Logging: `slf4j`, nie `System.out` **[VERIFY Fabric-Konvention 26.2]**
- **Kein Agent schreibt in `trials.jsonl`.** Die Messung gehört nicht dem Gemessenen.

---

## §8 Geparkt (bewusst NICHT Teil 1)

- **Industrie-Subsystem** — Kraftwerke (Kohle → Wasser → Solar, gestaffelt nach *Art der
  Abhängigkeit*: Logistik / Geographie / Speicher), Fabriken, Rezepte
- **Arbeiter-NPC** — eigene Entity mit flacher Zustandsmaschine. **Nicht** `extends VillagerEntity`.
  Die Anstellung gehört **dem Block** (`Set<UUID> employedWorkers`), nicht dem Arbeiter.
  Blöcke despawnen nicht.
- **T-O5 — zweiter Druckfaktor** (Nachfrage, Aufträge, Fristen). Nach P4 bewerten.
- **Fracht als Items** — der Kern kennt `payloadMassKg`; es fehlt nur, wer sie setzt
- **Signal-Blöcke** — Blockabschnitte kommen aus der Topologie (T-D9)
- **Cosmetics, Bahnhofsschilder, Zugmodelle, Vanilla-Look** — Renderproblem (T-D2)
- **Lokführer-NPC, Villager-Transport** — trivial, sobald `workerCount` eine Zahl ist

---

## §9 Anti-Patterns (sofort stoppen und diskutieren)

- Ein `net.minecraft.*`- oder NBT-Import in `train-core`
- **Die Physik-Formel existiert an zwei Stellen** (Regel 2)
- **Der Planer ruft den Simulator auf** → Z5 wird tautologisch (T-D14)
- Variable Schrittweite, Wall-Clock, `HashSet`-Iteration in der Physikschleife (Regel 8)
- Ein Interface, dessen zweite Implementierung niemand benennen kann
- Ein Weltzustand, aus dem der Spieler nicht durch eigene Arbeit herauskommt (Regel 4)
- Zeitbasierter Verfall (Regel 5) · ein `ItemStack` im Kern (Regel 7)
- Ein roher OpenGL-Call (T-D16)
- **Ein Agent, der `example_project/` nach P0 noch liest** (Kontamination, §5/P0.1)
- Ein Trial, dessen Erwartung erst nach dem Ergebnis notiert wurde (§7)
- Eine Metrik, die A und B zu einer Zahl mittelt (§6)
- Eine Phase ohne `CLAUDE.md` als allererste Aktion
- Eine Session ohne `## Session stopped`-Block

---

## §10 Drop-Order unter Zeitdruck (fixiert)

Unter Druck fällt zuerst, was oben steht. **Nie umsortieren.**

1. **P6** (Auswertung) — verschiebbar, nicht streichbar
2. **P5** (Fahrplan / Lokführer) — die Mod ist ohne ihn spielbar
3. **Die *Schönheit* der Leitstand-Ausgabe** — Chat-Text statt GUI ist erlaubt
4. **Rendering-Qualität** in P4 — hässlich reicht
5. **Die Leitstand-*Ausgabe* selbst fällt nie** (Z11). Ohne sie ist die Ineffizienz unsichtbar,
   und die Mod hat keinen einzigen Druckpunkt mehr (§4, Z11)
6. **P3 (Planer) fällt nie.** Kern-Orakel und Grund, warum P1/P2 überprüfbar sind
7. **Z7 (Softlock-Schutz) fällt nie.** Wenn er wackelt, wird gestoppt, nicht gestutzt

> ⚠ v1 dieses Plans erklärte die Leitstand-GUI für droppable. **Falsch** — der T-O4-Lock
> („nur Zeit drückt") macht den Leitstand zur Kernschleife. Punkt 3/5 sind die Korrektur.

---

## §11 Querschnitt (immer)

- **`CLAUDE.md` als erste Aktion jeder Phase** — nie nachgelagert
- **Atomare Commits**, Format `<scope>: <imperative>`
- **Commit ⇒ Note-Update:** Statuszeile + `## Session stopped` im selben Commit
- **Neue `.md` ⇒ One-Liner in `docs/INDEX.md`** im selben Commit
- **TDD in `train-core`.** Kein Minecraft-Start, keine Netzwerk-Calls in Unit-Tests
- **Code ist Wahrheit** über Konzept-Dokument über Status-Prosa
- **[VERIFY] bleibt stehen**, bis jemand tatsächlich verifiziert hat
- **Handover nach ~20–30 Tool-Calls** via `## Session stopped`

---

> **Definition of Done — Teil 1:**
> (1) Z1–Z8 grün in `train-core`, ohne Minecraft, reproduzierbar bei gleichem Seed.
> (2) Z9–Z11 im Client verifiziert.
> (3) Ein Zug fährt von A nach B, wird bei Stromknappheit langsamer, die Strecke nutzt sich ab,
>     der Leitstand nennt den Engpass, ein Neustart verliert nichts.
> (4) `M1_PREREGISTRATION.md` liegt in der History **vor** dem ersten Trial;
>     `M1_RESULTS.md` wertet A und B **getrennt** aus.
> (5) Kein Eintrag aus §9 ist im Code.
