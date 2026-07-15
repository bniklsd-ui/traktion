// train-mc — dünner Adapter. Fabric Loom, Minecraft 26.2.
// Übersetzt Weltzustand in Zahlen und Zahlen zurück in Weltzustand. (Plan §1)
// Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-15. · Nikinger (Operator).

// MC 26.x ist unobfuskiert (seit 26.1 "Tiny Takeover"). Yarn/Mojmap sind obsolet.
// Non-remap Plugin-ID net.fabricmc.fabric-loom statt fabric-loom (remap).
// Siehe phase0/Fabric_Loom_Mappings_Fix_01.md für Recherche-Grundlage.
plugins {
    // Kotlin-DSL-Constraint: der plugins-Block kann keine property()-Auflösung machen.
    // Version als Literal; Pin dokumentiert in gradle.properties (T-D12).
    id("net.fabricmc.fabric-loom") version "1.16.3"
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    // KEINE mappings-Zeile — 26.x ist unobfuskiert, Remapping entfällt.
    // Siehe phase0/Fabric_Loom_Mappings_Fix_01.md.
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

java {
    // MC 26.2 erfordert Java 25 (verifiziert P0.4, Loom 1.16.3 Fehlermeldung).
    // Root setzt Java 21 für train-core; train-mc überschreibt auf 25.
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

// Toolchain überschreiben: Root setzt 21 (java_build_version), train-mc braucht 25 (java_mod_target).
// Loom fordert Java 25 für MC 26.2; Gradle selbst läuft weiter unter 21.
extensions.configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("java_mod_target") as String))
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}
