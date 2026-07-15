---
status: frozen
purpose: Recherche-Antwort zu Mappings für MC 26.2 — klärt, dass 26.x unobfuskiert ist, Yarn/Mojmap obsolet, und die Plugin-ID net.fabricmc.fabric-loom (non-remap) der Fix ist.
read-when: bei Build-Fehlern mit Mappings/Remapping für 26.x; vor P0.4-Spike; bei Loom-Plugin-Fragen
detail: L2
up: ./CLAUDE.md
down:
related: ../docs/plans/PHASE0_PLAN.md
updated: 2026-07-15
---

# Mappings für Minecraft 26.2 (Fabric): Antwort für das Projekt „Traktion”

## TL;DR

- **Es gibt für 26.2 weder Yarn noch Mojmap – und das ist Absicht, keine Verzögerung.** Seit der 26.1-Reihe ist Minecraft Java Edition unobfuskiert; Fabric hat Yarn/Intermediary nach 1.21.11 eingestellt, und Mojang liefert keine `client_mappings`/`server_mappings` mehr, weil der Code bereits lesbare Namen enthält. Ein Fallback auf eine ältere MC-Version ist NICHT nötig.
- **Der Build scheitert nicht an fehlenden Mappings, sondern an der falschen Loom-Plugin-ID.** Traktion nutzt offenbar noch das remappende `fabric-loom` mit `officialMojangMappings()`/`mappings`-Zeile. Für 26.x muss das nicht-remappende Plugin `net.fabricmc.fabric-loom` verwendet und die Mappings-Zeile komplett entfernt werden.
- **Konkrete Lösung:** Plugin-ID auf `net.fabricmc.fabric-loom` umstellen, `mappings`-Zeile und `officialMojangMappings()` löschen, `modImplementation`→`implementation`, `remapJar`→`jar`, Java 25, Gradle 9.5.1, Loader 0.19.3. Kein Yarn, kein Mojmap, kein Parchment nötig.

## Key Findings

**1. Mojang hat seine Mojmap-Praxis nicht „geändert” im Sinne einer Verzögerung – die Mappings sind schlicht überflüssig geworden.** Die Unobfuskierung begann mit „26.1 Snapshot 1” (Dezember 2025); laut Minecraft Wiki werden separate unobfuskierte Builds „no longer being released separately starting with 26.1 Snapshot 1, as the standard versions are no longer obfuscated”.  Die erste unobfuskierte *Stable* war 26.1 („Tiny Takeover”, 24.03.2026); 26.2 („Chaos Cubed”, laut Minecraft Wiki „a game drop released on June 16, 2026”,  angekündigt bei Minecraft LIVE – März 2026) setzt das fort. Der Spielcode enthält jetzt echte Klassen-, Methoden-, Feld- und Parameternamen. Deshalb enthält das Mojang-Manifest für 26.2 unter `downloads` nur noch `client` und `server`, aber keine `client_mappings`/`server_mappings` mehr – korrekt und dauerhaft, nicht „nachreichbar”. Die Mappings sind auch nicht an anderer URL, in anderem Format oder als separater Download verfügbar; sie existieren konzeptionell nicht mehr.

**2. Es gibt keine alternative Mappings-Quelle, weil keine gebraucht wird.**

- **Yarn:** Endgültig eingestellt. Der Fabric-Blog „Fabric for Minecraft 1.21.11” (05.12.2025) sagt wörtlich: „No, the plan is to stop updating Yarn and Intermediary after 1.21.11. Modders should begin migrating to Mojang Mappings as soon as possible.”  Die letzte Yarn-Version ist 1.21.11 (`meta.fabricmc.net/v2/versions/yarn/26.2` → `[]` ist erwartetes Verhalten).
- **Parchment:** Wird ab 26.1 nicht mehr benötigt, da Parameternamen jetzt vom unobfuskierten Spiel selbst kommen. Parchment/Unpick-Definitionen werden nur noch für ältere Versionen gepflegt (PaperMC/ParchmentMappings: „Since 26.1, the client is no longer obfuscated at all leading all the parameters and local variables readable. Therefore Parchment is not needed anymore”). 
- **Quilt/Community-Mappings:** Ebenfalls obsolet für unobfuskierte Versionen.
- Wer möchte, kann optional weiterhin eigene/benutzerdefinierte Mappings über Loom-Remapping verwenden, aber das ist für 26.2 unnötiger Mehraufwand.

**3. Loom 1.17 ist grundsätzlich der richtige Weg – aber der eigentliche Fix ist die Plugin-ID, nicht die Versionsnummer.** Seit Loom 1.14 (04.12.2025) existieren getrennte Plugin-IDs (Loom-Doku, docs.fabricmc.net/develop/loom):

- `net.fabricmc.fabric-loom` → für unobfuskierte Versionen (MC 26.1+)
- `net.fabricmc.fabric-loom-remap` → für obfuskierte Versionen (MC 1.21.11 und älter)
- `fabric-loom` (legacy) → „only supported for backwards compatibility with obfuscated versions. Use net.fabricmc.fabric-loom-remap instead.” 

Der Fabric-Blog „Fabric for Minecraft 26.1” (14.03.2026) formuliert es direkt: „As mappings are not provided for 26.1, developers should switch from the old net.fabricmc.fabric-loom-remap or fabric-loom plugins to the new net.fabricmc.fabric-loom plugin, which does not remap Minecraft or mods.”  Der Fehler „Failed to find official mojang mappings for 26.2” entsteht, weil das remappende Plugin für 26.2 nach Mojmap sucht, die es nicht gibt. Laut Loom-1.14-Release-Notes gilt mit der neuen ID: „When you use the new plugin ID for non-obfuscated versions, loom will skip configuring everything related to remapping. This provides a significant performance boost and greatly simplifies what loom does removing a lot of edge-case issues related to remapping mods.” 

**Wichtiger Konflikt bei der Loom-Version (ehrlich gekennzeichnet):** Der offizielle Fabric-Blog „Fabric for Minecraft 26.2” (15.06.2026) empfiehlt wörtlich: „Developers should use Loom 1.17 and Gradle 9.5.1 (at the time of writing) to develop mods for Minecraft 26.2.”  und „Players should install the latest stable version of Fabric Loader (currently 0.19.3). Fabric Loader 0.19.0 and Loom 1.17 bring with them a new API for enum extensions…”.  Meine Verifikation der GitHub-Releases und des Fabric-Maven ergab jedoch, dass zum Recherchezeitpunkt (15.07.2026) **1.16 (20.04.2026) die neueste auf Maven auflösbare Loom-Version** war und kein 1.17-Maven-Artefakt existierte (deckt sich mit dem vom Nutzer gemeldeten 404). Empfehlung daher: **zuerst `1.17` versuchen** (falls inzwischen veröffentlicht, siehe Fabric-Develop-Seite), bei Auflösungsfehler **auf `1.16` zurückfallen** – beide unterstützen unobfuskierte 26.x-Versionen vollständig, da die Non-Remap-Unterstützung bereits mit 1.14 kam. (Hinweis: Loom 1.16 setzt Gradle 9.4+ und Loader 0.19.0+ voraus.)

**4. `loom.noIntermediateMappings()` ist der falsche Ansatz und nicht nötig.** Diese experimentelle Methode war für obfuskierte Versionen ohne Intermediary gedacht (Remapping-Pfad). Für unobfuskierte Versionen ist der vorgesehene Weg nicht „Remapping ohne Intermediary”, sondern gar kein Remapping – über die Plugin-ID `net.fabricmc.fabric-loom`. `noIntermediateMappings()` im remappenden Plugin würde später beim Remapping-Schritt zu Folgeproblemen führen. Nicht verwenden.

**5. Bekannte Diskussionen/Issues:** Der Sachverhalt ist offiziell dokumentiert (nicht als „Bug”), u. a. in der Fabric-Doku „Porting to 26.1”, „Migrating Mappings”, der Loom-Doku (Plugin-IDs) sowie in den Fabric-Blogposts zu 1.21.11, 26.1 und 26.2. Relevante Loom-Issues betreffen Randfälle bei gemischten obfuskiert/unobfuskiert-Multiprojekt-Builds (z. B. FabricMC/fabric-loom #1477: „Cannot get mappings configuration in a non-obfuscated environment”). 

## Details

### Warum die „VERIFIED FACTS” korrekt beobachtet, aber falsch interpretiert waren

Jede einzelne Beobachtung des Nutzers stimmt – aber die Schlussfolgerung „Build fails because no mappings are available” ist die falsche Diagnose:

- `yarn/26.2` → `[]`: korrekt und endgültig (Yarn endet bei 1.21.11).
- Kein `client_mappings` im Mojang-Manifest: korrekt, weil 26.x unobfuskiert ist.
- „Failed to find official mojang mappings for 26.2”: Symptom des falschen (remappenden) Plugins.

Der Build braucht für 26.2 **keinerlei** Mappings-Artefakt.

### Konkrete build.gradle-Änderungen (Traktion)

**plugins-Block (build.gradle) bzw. settings.gradle:**

```gradle
plugins {
    id 'net.fabricmc.fabric-loom' version '1.17'   // Fallback: '1.16', falls 1.17 nicht auflösbar
    id 'maven-publish'
}
```

**pluginManagement (settings.gradle):**

```gradle
pluginManagement {
    repositories {
        maven { name = 'Fabric'; url = 'https://maven.fabricmc.net/' }
        gradlePluginPortal()
    }
}
```

**dependencies-Block:**

```gradle
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    // KEINE mappings-Zeile mehr! Weder yarn noch officialMojangMappings()
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"
    implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
}
```

**Weitere nötige Anpassungen:**

- `modImplementation`/`modCompileOnly`/`modApi` → `implementation`/`compileOnly`/`api` 
- Alle `remapJar`-Verweise → `jar`
- `tasks.withType(JavaCompile).configureEach { it.options.release = 25 }`, `sourceCompatibility`/`targetCompatibility = JavaVersion.VERSION_25`
- AccessWidener/ClassTweaker-Header: `named` → `official` 
- Jegliche Nutzung von Fabric Loaders `MappingResolver` sowie `method_`/`field_`-Referenzen entfernen

**gradle.properties (Zielwerte):**

```
minecraft_version=26.2
loader_version=0.19.3
loom_version=1.17          # Fallback 1.16
# kein yarn_mappings mehr
```

### Referenz

Als Vorlage dient `FabricMC/fabric-example-mod` (Branch/Tag `26.1.2` bzw. `26.2` für 26.x): dort ist bereits kein `mappings`-Eintrag mehr vorhanden, `implementation` statt `modImplementation`, und `options.release = 25`. 

## Recommendations

1. **Sofort:** Plugin-ID von `fabric-loom`/`officialMojangMappings()` auf `net.fabricmc.fabric-loom` umstellen und die komplette `mappings`-Zeile entfernen. Das behebt „Failed to find official mojang mappings for 26.2” direkt. Kein Fallback auf eine ältere MC-Version.
1. **Loom-Version:** `1.17` eintragen; falls Gradle den Plugin nicht auflöst (404/`Plugin not found`), auf `1.16` zurückfallen. Beide funktionieren mit 26.2.
1. **Cache leeren:** Nach der Umstellung `./gradlew build --refresh-dependencies` laufen lassen, um alte Loom-Caches zu invalidieren. 
1. **Code migrieren:** `modImplementation`→`implementation`, `remapJar`→`jar`, Java 25, AccessWidener-Header `named`→`official`, `MappingResolver`/`method_`/`field_` bereinigen.
1. **`noIntermediateMappings()` nicht verwenden.**

**Schwellen, die die Empfehlung ändern würden:** Nur falls Traktion aus zwingenden Gründen bei einer *obfuskierten* Version bleiben muss (z. B. Abhängigkeit von einer Bibliothek, die nur bis 1.21.11 existiert), wäre der Rückfall auf **1.21.11** die nächstgelegene Version mit sowohl Yarn (`1.21.11+build.6`) als auch Mojmap – dann mit `net.fabricmc.fabric-loom-remap` und Loom 1.14+. Für 26.2 selbst gibt es aber keinen solchen Grund.

## Caveats

- **Loom-Versionskonflikt:** Offizieller 26.2-Blog nennt „Loom 1.17”; zum Recherchezeitpunkt war jedoch 1.16 die neueste auf `maven.fabricmc.net` auflösbare Version, ohne 1.17-Artefakt (deckt sich mit dem 404 des Nutzers). Bitte die Fabric-Develop-Seite (`fabricmc.net/develop`) als Live-Quelle für die aktuell empfohlene, auflösbare Version prüfen.
- Einige Detailinfos zur Deobfuskierungs-Historie stammen aus Sekundärquellen (z. B. Java Code Geeks); die primären Fakten (unobfuskiert ab 26.1-Snapshots, Yarn-Ende bei 1.21.11, Plugin-IDs) sind jedoch durch offizielle Fabric-Doku und -Blog belegt.
- Der Fabric-Discord (#loom/#yarn) war nicht direkt einsehbar; die Aussagen stützen sich auf öffentliche Fabric-Doku, -Blog und GitHub.
