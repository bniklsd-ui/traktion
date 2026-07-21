package de.traktion.traincore;

/**
 * Kante im Schienennetz — verbindet zwei {@link Node}-Endpunkte (T-D2, T-D9).
 *
 * <p>Invarianten (Z1, im kompakten Constructor durchgesetzt):
 * <ul>
 *   <li>{@code railKind} ist nicht {@code null}.</li>
 *   <li>{@code from} und {@code to} sind gesetzt (Record-Komponenten, nicht nullable).</li>
 *   <li>{@code gradient} und {@code lengthMeters} sind endlich (kein NaN, kein Unendlich).</li>
 *   <li>{@code lengthMeters} > 0 (eine Kante der Länge 0 ist kein Gleis).</li>
 * </ul>
 *
 * <p>{@code gradient} ist die Steigung als dimensionslose Zahl (z.B. 0.01 für 1 %). Positiv =
 * bergauf, negativ = bergab (Rekuperation, Z3). {@code lengthMeters} ist die Kantenlänge in Metern.
 *
 * <p>Record = unveränderlich. Eine Kante wird nicht mutiert; sie wird hinzugefügt oder entfernt.
 * Der Graph prüft zusätzlich, dass beide Endpunkte existieren (kein verwaister Knoten, Z1).
 */
public record Edge(Node from, Node to, RailKind railKind, double gradient, double lengthMeters) {

    public Edge {
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
    }
}
