// train-core — reines Java. Kein Fabric, kein net.minecraft.*, kein NBT. (Plan §1, §3 Regel 1)
// NULL externe Abhängigkeiten außer Test-Bibliotheken.

plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // Nur Test-Bibliotheken. Keine Runtime-Abhängigkeiten. (Plan §1)
    testImplementation(platform("org.junit:junit-bom:${property("junit_jupiter_version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // jqwik [VERIFY in P0.4 — läuft unter Gradle 9.5.1?]
    // testImplementation("net.jqwik:jqwik:${property("jqwik_version")}")
}

tasks.test {
    useJUnitPlatform()
    // Determinismus: Tests müssen reproduzierbar sein (Plan §3 Regel 8)
    testLogging {
        events("passed", "skipped", "failed")
    }
}
