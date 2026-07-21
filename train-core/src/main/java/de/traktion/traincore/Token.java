package de.traktion.traincore;

/**
 * Token-Position und Geschwindigkeit (T-D3: Token ist die Wahrheit, Entity ist Sichtbarmachung).
 *
 * <p>In P1 existiert nur der Token — keine Entity, kein Rendering. Der Token bewegt sich auf
 * einer Kante ({@link Edge}) mit einem Fortschritt {@code progressMeters} (0 bis
 * {@code edge.lengthMeters()}) und einer Geschwindigkeit {@code speedMps}.
 *
 * <p>Ein Token ist veränderlich — der {@link Simulator} mutiert ihn pro Substep. Das ist
 * Absicht: der Token IST der Simulationszustand. Records wären hier falsch (sie sind
 * unveränderlich); der Simulator braucht einen veränderlichen Zustand.
 *
 * <p><b>maxPowerW</b> ist das Leistungsbudget des Zugs (Watt). Der Simulator fragt diese
 * Leistung beim {@link PowerGrid} an (mit Spannungsabfall), nicht den aktuellen Bedarf —
 * so gibt es Überschuss für Beschleunigung und Defizit für Bremsung (Z3).
 */
public final class Token {

    private final Consist consist;
    private final double maxPowerW;
    private final long id;

    private Edge edge;
    private double progressMeters;
    private double speedMps;

    /**
     * @param id              eindeutige Token-ID (Caller vergeben, Determinismus)
     * @param consist         Zugverband (Masse)
     * @param maxPowerW       Leistungsbudget in Watt (> 0)
     * @param startEdge       Kante, auf der der Token startet
     * @param startSpeedMps   Startgeschwindigkeit in m/s (≥ 0)
     */
    public Token(long id, Consist consist, double maxPowerW, Edge startEdge, double startSpeedMps) {
        if (consist == null) {
            throw new IllegalArgumentException("consist must not be null");
        }
        if (startEdge == null) {
            throw new IllegalArgumentException("startEdge must not be null");
        }
        if (!(maxPowerW > 0) || Double.isInfinite(maxPowerW)) {
            throw new IllegalArgumentException("maxPowerW must be finite and > 0: " + maxPowerW);
        }
        if (!(startSpeedMps >= 0) || Double.isInfinite(startSpeedMps)) {
            throw new IllegalArgumentException(
                "startSpeedMps must be finite and >= 0: " + startSpeedMps);
        }
        this.id = id;
        this.consist = consist;
        this.maxPowerW = maxPowerW;
        this.edge = startEdge;
        this.progressMeters = 0.0;
        this.speedMps = startSpeedMps;
    }

    public long id() { return id; }
    public Consist consist() { return consist; }
    public double maxPowerW() { return maxPowerW; }
    public Edge edge() { return edge; }
    public double progressMeters() { return progressMeters; }
    public double speedMps() { return speedMps; }

    /** Position auf der Kante als Bruchteil [0, 1] (für Spannungsabfall: Distanz zum Unterwerk). */
    public double distanceOnEdgeMeters() { return progressMeters; }

    /** True, wenn der Token das Ende der Kante erreicht hat. */
    public boolean reachedEndOfEdge() {
        return progressMeters >= edge.lengthMeters();
    }

    /** Setzt den Token auf eine neue Kante (zurück am Anfang). */
    public void moveToEdge(Edge nextEdge) {
        if (nextEdge == null) {
            throw new IllegalArgumentException("nextEdge must not be null");
        }
        this.edge = nextEdge;
        this.progressMeters = 0.0;
    }

    /** Mutiert den Token (nur für den Simulator). */
    void setSpeedMps(double speedMps) {
        if (!(speedMps >= 0) || Double.isInfinite(speedMps)) {
            throw new IllegalArgumentException("speedMps must be finite and >= 0: " + speedMps);
        }
        this.speedMps = speedMps;
    }

    /** Mutiert den Token (nur für den Simulator). */
    void setProgressMeters(double progressMeters) {
        if (!(progressMeters >= 0) || Double.isInfinite(progressMeters)) {
            throw new IllegalArgumentException(
                "progressMeters must be finite and >= 0: " + progressMeters);
        }
        this.progressMeters = progressMeters;
    }
}
