package de.traktion.traincore;

/**
 * Das Stromnetz — modelliert Bedarf, Angebot und Spannungsabfall über Distanz (Z4 ohne
 * condition, T-D22).
 *
 * <p><b>Z4 ohne condition:</b> in P1 ist der Spannungsabfall nur f(Distanz), nicht
 * f(Distanz, condition). {@code condition} kommt in P2 (Verschleiß) und erhöht den
 * Spannungsabfall. Das ist bewusst — P1 misst den Durchstich, nicht den Vollumfang.
 *
 * <p><b>Spannungsabfall-Modell (linear, deterministisch):</b>
 * <pre>
 *   deliveredW = requestedW * max(0, 1 - distanceMeters / maxReachMeters)
 * </pre>
 * Bei Distanz 0: volle Leistung. Bei Distanz ≥ maxReach: null. Monoton fallend in der Distanz
 * (größere Distanz → weniger oder gleich). Kein Zustand, keine Wall-Clock — reine Funktion
 * der Distanz.
 *
 * <p><b>Unterwerk-Reset (Z4):</b> in P1 ohne Verschleiß ist das Netz zustandslos — das
 * Unterwerk liefert immer, solange der {@link PowerSupply} liefert. Der Reset ist in P1 ein
 * No-Op, der die API etabliert. P2 macht daraus echtes Zustandsmanagement (Verschleiß
 * degradiert das Angebot, Reset stellt es wieder her).
 *
 * <p><b>Determinismus (Regel 8):</b> keine Wall-Clock, kein Zufall. Die Berechnung ist rein
 * funktional in den Eingaben.
 */
public final class PowerGrid {

    /** Maximale Reichweite in Metern, bis zu der Leistung geliefert wird (Spannungsabfall). */
    private static final double DEFAULT_MAX_REACH_METERS = 1000.0;

    private final PowerSupply supply;
    private final double maxReachMeters;

    /**
     * @param supply        woher der Strom kommt (Port, Plan §3.2)
     * @param maxReachMeters Distanz, ab der der Spannungsabfall die Leistung auf null bringt
     */
    public PowerGrid(PowerSupply supply, double maxReachMeters) {
        if (supply == null) {
            throw new IllegalArgumentException("supply must not be null");
        }
        if (!(maxReachMeters > 0) || Double.isInfinite(maxReachMeters)) {
            throw new IllegalArgumentException(
                "maxReachMeters must be finite and > 0: " + maxReachMeters);
        }
        this.supply = supply;
        this.maxReachMeters = maxReachMeters;
    }

    /** Default-Reichweite (1000 m). */
    public PowerGrid(PowerSupply supply) {
        this(supply, DEFAULT_MAX_REACH_METERS);
    }

    /**
     * Liefert die verfügbare Leistung an einer Kante mit Länge {@code distanceMeters}, wenn
     * {@code requestedW} Watt angefordert werden (Z4 ohne condition: f(Distanz)).
     *
     * <p>Der Spannungsabfall reduziert die angeforderte Leistung linear mit der Distanz.
     * Das Unterwerk ({@link PowerSupply}) liefert höchstens die reduzierte Menge.
     *
     * @param requestedW      angeforderte Leistung in Watt (≥ 0)
     * @param distanceMeters  Distanz zum Unterwerk in Metern (≥ 0)
     * @param dtSeconds       Zeitspanne in Sekunden (> 0)
     * @return gelieferte Leistung in Watt (0 ≤ result ≤ requestedW); monoton fallend in distance
     */
    public double availableW(double requestedW, double distanceMeters, double dtSeconds) {
        if (!(requestedW >= 0) || Double.isInfinite(requestedW)) {
            throw new IllegalArgumentException(
                "requestedW must be finite and >= 0: " + requestedW);
        }
        if (!(distanceMeters >= 0) || Double.isInfinite(distanceMeters)) {
            throw new IllegalArgumentException(
                "distanceMeters must be finite and >= 0: " + distanceMeters);
        }
        if (!(dtSeconds > 0) || Double.isInfinite(dtSeconds)) {
            throw new IllegalArgumentException(
                "dtSeconds must be finite and > 0: " + dtSeconds);
        }
        if (requestedW == 0.0) {
            return 0.0;
        }
        double reachFactor = Math.max(0.0, 1.0 - distanceMeters / maxReachMeters);
        double effectiveRequest = requestedW * reachFactor;
        return supply.supply(effectiveRequest, dtSeconds);
    }

    /**
     * Unterwerk-Reset (Z4). In P1 ohne Verschleiß ein No-Op — das Netz ist zustandslos.
     * P2 überschreibt/erweitert dies, um degradiertes Angebot wiederherzustellen.
     */
    public void resetSubstation() {
        // P1: zustandslos, nichts zurückzusetzen. P2: echtes Zustandsmanagement.
    }

    /** Maximale Reichweite in Metern (für Tests). */
    public double maxReachMeters() {
        return maxReachMeters;
    }
}
