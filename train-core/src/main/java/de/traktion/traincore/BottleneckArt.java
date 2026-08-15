package de.traktion.traincore;

/**
 * Art einer Engpass-Klassifikation im Planer (T-D39).
 *
 * <p>Ein Bottleneck entsteht, wenn die Route auf einer Kante durch eine der folgenden
 * Bedingungen verlängert wird:
 * <ul>
 *   <li>{@link #SPANNUNG}: die effective condition der Kante ist {@code < 1.0} —
 *       der Spannungsabfall reduziert die verfügbare Leistung und verlängert die Fahrzeit.</li>
 *   <li>{@link #STEIGUNG}: die Steigung der Kante ist {@code > 0} —
 *       der Steigungswiderstand erhöht den Leistungsbedarf.</li>
 *   <li>{@link #KOMBI}: beide Bedingungen叠加 — sowohl schlechte Condition als auch Steigung
 *       wirken gemeinsam.</li>
 * </ul>
 */
public enum BottleneckArt {
    /** Spannungsbedingter Engpass — condition {@code < 1.0} auf dieser Kante. */
    SPANNUNG,

    /** Steigungsbedingter Engpass — gradient {@code > 0} auf dieser Kante. */
    STEIGUNG,

    /** Kombination aus Spannungs- und Steigungs-Engpass auf derselben Kante. */
    KOMBI
}
