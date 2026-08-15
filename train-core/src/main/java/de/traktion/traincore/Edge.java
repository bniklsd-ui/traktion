package de.traktion.traincore;

/**
 * Kante im Schienennetz — verbindet zwei {@link Node}-Endpunkte (T-D2, T-D9).
 *
 * <p>Invarianten (Z1, im kompakten Constructor durchgesetzt):
 * <ul>
 *   <li>{@code railKind} ist nicht {@code null}.</li>
 *   <li>{@code from} und {@code to} sind gesetzt (nicht nullable).</li>
 *   <li>{@code gradient} und {@code lengthMeters} sind endlich (kein NaN, kein Unendlich).</li>
 *   <li>{@code lengthMeters} > 0 (eine Kante der Länge 0 ist kein Gleis).</li>
 * </ul>
 *
 * <p>{@code gradient} ist die Steigung als dimensionslose Zahl (z.B. 0.01 für 1 %). Positiv =
 * bergauf, negativ = bergab (Rekuperation, Z3). {@code lengthMeters} ist die Kantenlänge in Metern.
 *
 * <p>Verschleiß (T-D25): {@code railCondition} und {@code overheadCondition} sind beide
 * {@code double ∈ [0,1]}, Default 1.0 (perfekte Neuanlage). Beide starten perfekt;
 * Verschleiß senkt, {@link #repairRail(double)} / {@link #repairOverhead(double)} heben.
 * Beide getrennt, weil Schiene und Oberleitung verschiedene Infrastruktur sind und verschiedene
 * Reparatur-Vorgänge haben. Die elektrisch relevante Condition ist
 * {@code min(railCondition, overheadCondition)} — der schlechtere Wert dominiert.
 *
 * <p>Records sind per Definition unveränderlich. Da die Condition-Werte sich während der Simulation
 * ändern (Verschleiß akkumuliert pro Substep), ist Edge hier als {@code final class} mit
 * mutable Condition-Feldern implementiert. Die Identity (from/to/railKind/gradient/lengthMeters)
 * ändert sich nicht — nur die Verschleiß-Werte.
 */
public final class Edge {

    private final Node from;
    private final Node to;
    private final RailKind railKind;
    private final double gradient;
    private final double lengthMeters;

    private double railCondition;
    private double overheadCondition;

    public Edge(Node from, Node to, RailKind railKind, double gradient, double lengthMeters) {
        this(from, to, railKind, gradient, lengthMeters, 1.0, 1.0);
    }

    public Edge(Node from, Node to, RailKind railKind, double gradient, double lengthMeters,
                double railCondition, double overheadCondition) {
        if (railKind == null) {
            throw new IllegalArgumentException("railKind must not be null (Z1 invariant)");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("edge endpoints must not be null (Z1 invariant)");
        }
        if (Double.isNaN(gradient) || Double.isInfinite(gradient)) {
            throw new IllegalArgumentException("gradient must be finite (Z1 invariant): " + gradient);
        }
        if (Double.isNaN(lengthMeters) || Double.isInfinite(lengthMeters)) {
            throw new IllegalArgumentException("lengthMeters must be finite (Z1 invariant): " + lengthMeters);
        }
        if (!(lengthMeters > 0)) {
            throw new IllegalArgumentException("lengthMeters must be > 0 (Z1 invariant): " + lengthMeters);
        }
        if (Double.isNaN(railCondition) || Double.isInfinite(railCondition)
                || railCondition < 0 || railCondition > 1) {
            throw new IllegalArgumentException("railCondition must be in [0, 1] (T-D25): " + railCondition);
        }
        if (Double.isNaN(overheadCondition) || Double.isInfinite(overheadCondition)
                || overheadCondition < 0 || overheadCondition > 1) {
            throw new IllegalArgumentException("overheadCondition must be in [0, 1] (T-D25): " + overheadCondition);
        }
        this.from = from;
        this.to = to;
        this.railKind = railKind;
        this.gradient = gradient;
        this.lengthMeters = lengthMeters;
        this.railCondition = railCondition;
        this.overheadCondition = overheadCondition;
    }

    public Node from() { return from; }
    public Node to() { return to; }
    public RailKind railKind() { return railKind; }
    public double gradient() { return gradient; }
    public double lengthMeters() { return lengthMeters; }

    /**
     * Mechanischer Verschleiß der Schiene. {@code double ∈ [0, 1]}, 1.0 = neu/perfect.
     */
    public double railCondition() { return railCondition; }

    /**
     * Elektrischer Verschleiß der Oberleitung. {@code double ∈ [0, 1]}, 1.0 = neu/perfect.
     */
    public double overheadCondition() { return overheadCondition; }

    /**
     * Die elektrisch relevante Condition — der schlechtere von rail und overhead.
     * Wird für den Spannungsabfall verwendet (T-D5: condition → Widerstand → Spannungsabfall ↑).
     */
    public double effectiveCondition() {
        return Math.min(railCondition, overheadCondition);
    }

    /**
     * Repariert die Schiene um {@code amount}. Values werden geclampt auf [0, 1].
     * Erhöht {@code railCondition}, lässt {@code overheadCondition} unverändert.
     */
    public void repairRail(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("amount must be finite: " + amount);
        }
        railCondition = Math.max(0, Math.min(1, railCondition + amount));
    }

    /**
     * Repariert die Oberleitung um {@code amount}. Values werden geclampt auf [0, 1].
     * Erhöht {@code overheadCondition}, lässt {@code railCondition} unverändert.
     */
    public void repairOverhead(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("amount must be finite: " + amount);
        }
        overheadCondition = Math.max(0, Math.min(1, overheadCondition + amount));
    }
}
