// train-mc — dünner Adapter. Fabric Loom, Minecraft 26.2.
// Übersetzt Weltzustand in Zahlen und Zahlen zurück in Weltzustand. (Plan §1)

plugins {
    id("fabric-loom") version "${property("loom_version")}"
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("minecraft_version")}+build.4:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    // Mod-Target-Java [VERIFY in P0.4]
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version": project.version)
    }
}
