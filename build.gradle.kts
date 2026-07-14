// Traktion — Root Build
// Multi-Modul: train-core (plain Java) + train-mc (Fabric Loom)
// Plan §1: train-core hat NULL externe Abhängigkeiten außer Test-Bibliotheken.

plugins {
    // Loom wird nur in train-mc angewendet, hier nur deklariert (siehe train-mc/build.gradle.kts)
    // Kein `apply false` nötig, wenn der Plugin-Klasspfad nur im Submodul geladen wird.
}

allprojects {
    group = property("maven_group") as String
    version = "${property("mod_version")}"
}

subprojects {
    // Gemeinsame Java-Toolchain: Java 21 (Build-Toolchain, T-D17)
    // Mod-Target-Java [VERIFY in P0.4]
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(property("java_build_version") as String))
            }
        }
    }

    // Gemeinsame Repositories
    repositories {
        mavenCentral()
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}
