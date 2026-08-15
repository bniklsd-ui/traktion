package de.traktion.traincore;

/**
 * Das Stromnetz — modelliert Bedarf, Angebot und Spannungsabfall über Distanz und Zustand (Z4,
 * T-D5, T-D27).
 *
 * <p><b>Spannungsabfall-Modell (linear, deterministisch):</b>
 * <pre>
 *   effectiveReach = maxReachMeters * condition
 *   deliveredW = requestedW * max(0, 1 - distanceMeters / effectiveReach)
 * </pre>
 * Bei condition = 1.0: volle Reichweite (P1-Verhalten). Bei condition < 1.0: reduzierte
 * Reichweite → weniger Leistung am Verbraucher (T-D5: Verschleiß → Spannungsabfall ↑).
 * Bei Distanz 0: volle Leistung. Bei Distanz ≥ effectiveReach: null.
 * Monoton fallend in der Distanz (größere Distanz → weniger oder gleich).
 *
 * <p><b>condition ∈ [0, 1]:</b> 1.0 = perfekte Infrastruktur, 0.0 = keine Leitfähigkeit.
 * Der Simulator leitet {@code min(edge.railCondition(), edge.overheadCondition())} durch
 * (T-D27).
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
     * Liefert die verfügbare Leistung an einer Kante mit Länge {@code distanceMeters} und
     * Infrastruktur-Zustand {@code condition}, wenn {@code requestedW} Watt angefordert werden
     * (Z4 mit condition, T-D5, T-D27).
     *
     * <p>Der Spannungsabfall reduziert die angeforderte Leistung linear mit der Distanz,
     * aber auch mit dem Zustand: {@code effectiveReach = maxReachMeters * condition}.
     * Das Unterwerk ({@link PowerSupply}) liefert höchstens die reduzierte Menge.
     *
     * @param requestedW      angeforderte Leistung in Watt (≥ 0)
     * @param distanceMeters  Distanz zum Unterwerk in Metern (≥ 0)
     * @param condition       Infrastruktur-Zustand ∈ [0, 1] (1.0 = perfekt, T-D25)
     * @param dtSeconds       Zeitspanne in Sekunden (> 0)
     * @return gelieferte Leistung in Watt (0 ≤ result ≤ requestedW); monoton fallend in distance und
     *         monoton steigend in condition
     */
    public double availableW(double requestedW, double distanceMeters, double condition, double dtSeconds) {
        if (!(requestedW >= 0) || Double.isInfinite(requestedW)) {
            throw new IllegalArgumentException(
                "requestedW must be finite and >= 0: " + requestedW);
        }
        if (!(distanceMeters >= 0) || Double.isInfinite(distanceMeters)) {
            throw new IllegalArgumentException(
                "distanceMeters must be finite and >= 0: " + distanceMeters);
        }
        if (Double.isNaN(condition) || Double.isInfinite(condition) || condition < 0 || condition > 1) {
            throw new IllegalArgumentException(
                "condition must be in [0, 1]: " + condition);
        }
        if (!(dtSeconds > 0) || Double.isInfinite(dtSeconds)) {
            throw new IllegalArgumentException(
                "dtSeconds must be finite and > 0: " + dtSeconds);
        }
        if (requestedW == 0.0) {
            return 0.0;
        }
        if (condition == 0.0) {
            return 0.0; // keine Leitfähigkeit bei völlig degradiertem Zustand
        }
        // Effektive Reichweite skaliert mit condition (T-D27, T-D5)
        double effectiveReach = maxReachMeters * condition;
        double reachFactor = Math.max(0.0, 1.0 - distanceMeters / effectiveReach);
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
