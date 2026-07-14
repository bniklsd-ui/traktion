pluginManagement {
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
