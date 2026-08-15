package de.traktion.traincore;

import java.util.List;

/**
 * Ein Engpass auf einer Kante der Route (T-D39).
 *
 * <p>Ein {@code Bottleneck} repräsentiert eine Kante, die die Fahrzeit der Route
 * gegenüber dem Idealzustand (condition = 1.0, gradient = 0) verlängert.
 * Der Beitrag ist in Sekunden Fahrzeit-Verlängerung angegeben.
 *
 * <p>Record = unveränderlich. Die Felder werden im Constructor validiert.
 *
 * @param edge        die Kante, auf der der Engpass liegt; darf nicht {@code null} sein
 * @param art         die Art des Engpasses ({@link BottleneckArt}); darf nicht {@code null} sein
 * @param beitragSekunden geschätzter Beitrag zur Fahrzeit-Verlängerung in Sekunden; {@code >= 0}
 */
public record Bottleneck(Edge edge, BottleneckArt art, double beitragSekunden) {

    public Bottleneck {
        if (edge == null) {
            throw new IllegalArgumentException("edge must not be null (T-D39)");
        }
        if (art == null) {
            throw new IllegalArgumentException("art must not be null (T-D39)");
        }
        if (Double.isNaN(beitragSekunden) || Double.isInfinite(beitragSekunden)) {
            throw new IllegalArgumentException("beitragSekunden must be finite: " + beitragSekunden);
        }
        if (!(beitragSekunden >= 0)) {
            throw new IllegalArgumentException("beitragSekunden must be >= 0: " + beitragSekunden);
        }
    }
}
