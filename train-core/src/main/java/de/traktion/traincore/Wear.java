package de.traktion.traincore;

/**
 * Verschleiß-Akkumulator für Schiene und Oberleitung (Z6, T-D31, Regel 5).
 *
 * <p>Die Verschleißformel ist: {@code Δcondition = -k * mass * speed * dt}
 * wobei {@code k = 1e-10} (kalibriert auf T-D33: 10.000 Ticks Dauerbetrieb degradieren
 * messbar, blockieren nie total).
 *
 * <p>Eigenschaften (T-D31, Regel 5):
 * <ul>
 *   <li>Positiver Verschleiß nur wenn mass > 0 UND speed > 0 (Regel 5: Verschleiß bestraft
 *       Nutzung, nicht Existenz)</li>
 *   <li>Monoton in mass: größere Masse → mehr Verschleiß</li>
 *   <li>Monoton in speed: größere Geschwindigkeit → mehr Verschleiß</li>
 *   <li>Linear in dt: doppeltes dt → doppelter Verschleiß</li>
 *   <li>Clamping: condition bleibt in [0, 1]</li>
 * </ul>
 *
 * <p>Der Simulator ruft {@link #accumulate} pro Substep auf dem aktuellen {@link Edge} des
 * Tokens auf. Der gesäte {@link java.util.Random} des Simulators wird übergeben, damit die
 * stochastische Komponente (falls genutzt) deterministisch bleibt (Regel 8).
 *
 * <p>Diese Klasse ist eine Utility-Klasse (analog zu {@link Physics}) — keine Instanzen,
 * nur statische Methoden. Die Formel existiert genau hier; der Simulator ruft sie auf,
 * dupliziert sie nicht (Regel 2).
 */
public final class Wear {

    /** Verschleiß-Koeffizient (kalibriert auf T-D33). */
    public static final double WEAR_COEFFICIENT = 1e-10;

    private Wear() { }

    /**
     * Akkumuliert Verschleiß auf einem {@link Edge} für einen Zug mit gegebener Masse und
     * Geschwindigkeit über einen Zeitschritt dt.
     *
     * <p>Wendet wear auf {@code edge.railCondition()} und {@code edge.overheadCondition()} an.
     * Beide werden getrennt degradiert (T-D25: getrennte Infrastruktur).
     *
     * @param edge       die Kante, deren Zustand degradiert
     * @param massKg     Gesamtmasse des Zugs in kg (> 0)
     * @param speedMps   Geschwindigkeit in m/s (> 0)
     * @param dtSeconds  Zeitschritt in Sekunden (> 0)
     * @param rng        gesähter Zufallsgenerator (für stochastische Komponente, Regel 8)
     */
    public static void accumulate(Edge edge, double massKg, double speedMps, double dtSeconds, java.util.Random rng) {
        if (edge == null) {
            throw new IllegalArgumentException("edge must not be null");
        }
        if (massKg < 0 || Double.isNaN(massKg) || Double.isInfinite(massKg)) {
            throw new IllegalArgumentException("massKg must be finite and >= 0: " + massKg);
        }
        if (speedMps < 0 || Double.isNaN(speedMps) || Double.isInfinite(speedMps)) {
            throw new IllegalArgumentException("speedMps must be finite and >= 0: " + speedMps);
        }
        if (dtSeconds <= 0 || Double.isNaN(dtSeconds) || Double.isInfinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be finite and > 0: " + dtSeconds);
        }

        // Regel 5: kein Verschleiß ohne Nutzung (mass=0 oder speed=0)
        if (massKg == 0.0 || speedMps == 0.0) {
            return;
        }

        // Deterministischer Verschleiß (keine stochastische Komponente — der gesäte rng
        // in Simulator ist für zukünftige Nutzung (P3/P5), nicht für Wear-Streuung.
        // Regel 8: deterministisch bei gleichem Seed.
        double delta = -WEAR_COEFFICIENT * massKg * speedMps * dtSeconds;

        // Rail degradiert
        double newRail = edge.railCondition() + delta;
        // Clamp auf [0, 1]
        if (newRail < 0) newRail = 0;
        if (newRail > 1) newRail = 1;
        // Direkter Field-Zugriff (Edge hat keine setRailCondition - die Mutatoren sind repair*)
        // Hier nutzen wir die bestehende repair-Methode mit einem negativen delta
        edge.repairRail(delta); // repairRail akzeptiert auch negative Werte (= Verschleiß)

        // Overhead degradiert (gleicher Betrag, unabhängig)
        double newOverhead = edge.overheadCondition() + delta;
        if (newOverhead < 0) newOverhead = 0;
        if (newOverhead > 1) newOverhead = 1;
        edge.repairOverhead(delta);
    }

    /**
     * Überladene Variante ohne Zufall (für rein deterministische Tests).
     */
    public static void accumulate(Edge edge, double massKg, double speedMps, double dtSeconds) {
        accumulate(edge, massKg, speedMps, dtSeconds, null);
    }
}
