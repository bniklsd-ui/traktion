package de.traktion.traincore;

import java.util.List;

/**
 * Ergebnis einer Planer-Prognose für eine Route (T-D38).
 *
 * <p>Enthält die Soll-Fahrzeit (Prognose mit condition = 1.0 überall), die Ist-Fahrzeit
 * (Prognose mit aktuellem Netzzustand) und die prozentuale Abweichung.
 * Die Bottleneck-Top-3-Liste benennt die größten Engpässe auf der Route.
 *
 * <p>Record = unveränderlich. Die {@code bottlenecks}-Liste wird als defensive Kopie gespeichert
 * (Regel 8 — keine geteilte mutable Liste nach außen).
 *
 * @param sollFahrzeitSekunden  Fahrzeit-Prognose in Sekunden bei condition = 1.0 überall
 * @param istFahrzeitSekunden   Fahrzeit-Prognose in Sekunden mit aktuellem Netzzustand
 * @param deltaProzent          prozentuale Abweichung: {@code (ist - soll) / soll * 100}
 *                              (positiv = Ist länger als Soll = Planer pessimistisch)
 * @param bottlenecks           Top-3-Liste der größten Engpässe, absteigend nach
 *                              {@link Bottleneck#beitragSekunden()} sortiert; nie {@code null}
 */
public record RouteForecast(
        double sollFahrzeitSekunden,
        double istFahrzeitSekunden,
        double deltaProzent,
        List<Bottleneck> bottlenecks) {

    public RouteForecast {
        if (Double.isNaN(sollFahrzeitSekunden) || Double.isInfinite(sollFahrzeitSekunden)) {
            throw new IllegalArgumentException("sollFahrzeitSekunden must be finite: " + sollFahrzeitSekunden);
        }
        if (!(sollFahrzeitSekunden >= 0)) {
            throw new IllegalArgumentException("sollFahrzeitSekunden must be >= 0: " + sollFahrzeitSekunden);
        }
        if (Double.isNaN(istFahrzeitSekunden) || Double.isInfinite(istFahrzeitSekunden)) {
            throw new IllegalArgumentException("istFahrzeitSekunden must be finite: " + istFahrzeitSekunden);
        }
        if (!(istFahrzeitSekunden >= 0)) {
            throw new IllegalArgumentException("istFahrzeitSekunden must be >= 0: " + istFahrzeitSekunden);
        }
        if (Double.isNaN(deltaProzent) || Double.isInfinite(deltaProzent)) {
            throw new IllegalArgumentException("deltaProzent must be finite: " + deltaProzent);
        }
        if (bottlenecks == null) {
            throw new IllegalArgumentException("bottlenecks must not be null (T-D38)");
        }
        // Defensive copy — keine geteilte mutable Liste nach außen (Regel 8)
        bottlenecks = List.copyOf(bottlenecks);
    }
}
