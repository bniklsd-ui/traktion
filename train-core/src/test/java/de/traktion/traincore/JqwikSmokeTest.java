package de.traktion.traincore;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * jqwik-Smoke-Test — beweist, dass jqwik 1.9.0 unter Gradle 9.5.1 mit JUnit 5.12.2 läuft
 * (P1 Step 1, T-D20). Löst das [VERIFY] "jqwik unter Gradle 9.5.1?" aus P0 auf.
 *
 * <p>Keine Domänenlogik hier — nur der Beweis, dass die Property-Testing-Infrastruktur steht.
 * Ab P1 Step 3 (RailGraph) kommen echte Property-Tests dazu.
 *
 * <p>Die Eigenschaft {@code -(i) == -i} ist eine echte Invariante (unäres Minus ist total).
 * Ein naiver {@code Math.abs(i) >= 0}-Test wäre am Integer.MIN_VALUE-Overflow gescheitert —
 * jqwik hat genau diesen Edge-Case beim ersten Lauf gefunden. Das ist der Beweis, dass die
 * Engine läuft und nicht nur auf dem Classpath liegt.
 */
class JqwikSmokeTest {

    @Property
    void unaryMinusIsTotal(@ForAll int i) {
        assertEquals(-i, -i, "unäres Minus ist total — jqwik läuft");
    }
}

