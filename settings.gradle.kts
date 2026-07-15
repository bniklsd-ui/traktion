// Traktion — Gradle Settings. Multi-Modul: train-core + train-mc.
// Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-15. · Nikinger (Operator).

pluginManagement {
    // Loom veröffentlicht keinen Gradle-Plugin-Marker-Artifact (fabric-loom.gradle.plugin).
    // Daher muss die Plugin-ID hier auf die echte Koordinate gemappt werden.
    // Siehe phase0/Fabric_Loom_Mappings_Fix_01.md: 26.x nutzt die non-remap Plugin-ID.
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.fabricmc.fabric-loom") {
                useModule("net.fabricmc:fabric-loom:${requested.version}")
            }
        }
    }
    repositories {
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "traktion"

include("train-core")
include("train-mc")
