package de.traktion.traincore;

import java.util.List;
import java.util.Optional;

/**
 * Statische Planer-Utility — analytische Fahrzeit-Prognose für eine Route (T-D37).
 *
 * <p><b>Architektur (T-D35, T-D36, T-D37, T-D44, T-D45):</b>
 * <ul>
 *   <li>Statische Utility (keine Instanz, analog zu {@link Physics} und {@link Wear})</li>
 *   <li>Pro Kante analytisch — keine Sub-Tick-Simulation, keine {@link Simulator}-Interaktion</li>
 *   <li>Ignoriert Verkehr — genau ein Zug, keine {@link BlockSection}-Reservierung</li>
 *   <li>Ruft {@link Physics#requiredPowerW} auf — Regel 2, keine Formel-Duplikation</li>
 *   <li>Deterministisch — keine {@link java.util.Random}, keine Wall-Clock</li>
 * </ul>
 *
 * <p><b>Algorithmus (grob analytisch, T-D35):</b>
 * Pro Kante wird die maximal verfügbare Geschwindigkeit per Binary Search bestimmt
 * (Gleichgewicht: {@code Physics.requiredPowerW(consist, v, gradient) == availablePower}),
 * dann die Fahrzeit über die Kantenlänge summiert.
 *
 * <p><b>Vertragsgrenze (T-D40):</b>
 * Gibt {@link Optional#empty} zurück für:
 * <ul>
 *   <li>leere Route (T-D40 b)</li>
 *   <li>{@code consist.totalMassKg() == 0} (T-D40 c)</li>
 *   <li>{@code maxPowerW <= 0} (T-D40 d)</li>
 *   <li>{@code effectiveCondition == 0} auf mindestens einer Kante der Route (T-D40 a)</li>
 * </ul>
 *
 * <p><b>Harte Fehler (T-D41):</b>
 * {@link IllegalArgumentException} für {@code null}-Argumente und nicht-zusammenhängende Routen.
 */
public final class Planner {

    /** Kleinste Geschwindigkeit für die Binary Search (m/s). */
    private static final double MIN_SPEED_MPS = 0.0;

    /** Höchste Geschwindigkeit für die Binary Search (m/s). */
    private static final double MAX_SPEED_MPS = 50.0;

    /** Iterationen für die Binary Search (hinreichend für Konvergenz). */
    private static final int BINARY_SEARCH_ITERATIONS = 50;

    /** Konvergenz-Toleranz für die Binary Search (m/s). */
    private static final double SPEED_TOLERANCE_MPS = 1e-6;

    /** Minimale nicht-null Geschwindigkeit (m/s) — vermeidet Division durch 0. */
    private static final double NON_ZERO_SPEED_MPS = 0.01;

    private Planner() {
        // Utility-Klasse — keine Instanzen.
    }

    /**
     * Analytische Fahrzeit-Prognose für eine Route (T-D35–T-D37, T-D44, T-D45).
     *
     * @param route          Liste von Kanten, die die Route definieren (von Start nach Ziel)
     * @param consist        Zugverband (liefert die Masse)
     * @param netState       Stromnetz-Zustand (für {@code availableW} mit Spannungsabfall)
     * @param maxPowerW      maximale verfügbare Leistung in Watt (> 0)
     * @param startSpeedMps  Anfangsgeschwindigkeit in m/s (≥ 0)
     * @return {@link Optional} mit {@link RouteForecast} bei Erfolg,
     *         {@link Optional#empty} wenn die Route nicht befahrbar ist (T-D40)
     * @throws IllegalArgumentException wenn {@code route} oder {@code consist} {@code null} ist (T-D41)
     * @throws IllegalArgumentException wenn die Route nicht zusammenhängend ist (T-D41)
     */
    public static Optional<RouteForecast> predict(
            List<Edge> route,
            Consist consist,
            PowerGrid netState,
            double maxPowerW,
            double startSpeedMps) {

        // T-D41: harte Fehler bei null
        if (route == null) {
            throw new IllegalArgumentException("route must not be null (T-D41)");
        }
        if (consist == null) {
            throw new IllegalArgumentException("consist must not be null (T-D41)");
        }
        if (netState == null) {
            throw new IllegalArgumentException("netState must not be null (T-D41)");
        }
        if (Double.isNaN(startSpeedMps) || Double.isInfinite(startSpeedMps)) {
            throw new IllegalArgumentException("startSpeedMps must be finite: " + startSpeedMps);
        }
        if (!(startSpeedMps >= 0)) {
            throw new IllegalArgumentException("startSpeedMps must be >= 0: " + startSpeedMps);
        }
        if (Double.isNaN(maxPowerW) || Double.isInfinite(maxPowerW)) {
            throw new IllegalArgumentException("maxPowerW must be finite: " + maxPowerW);
        }

        // T-D40 (b): leere Route
        if (route.isEmpty()) {
            return Optional.empty();
        }

        // T-D40 (c): Masse 0
        double mass = consist.totalMassKg();
        if (mass == 0.0) {
            return Optional.empty();
        }

        // T-D40 (d): keine Leistung
        if (!(maxPowerW > 0)) {
            return Optional.empty();
        }

        // T-D41 (e): Route muss zusammenhängen
        validateRouteConnected(route);

        // T-D40 (a): condition == 0 auf einer Kante → nicht befahrbar
        for (Edge edge : route) {
            if (edge.effectiveCondition() == 0.0) {
                return Optional.empty();
            }
        }

        // Fahrzeit mit aktuellem Netz-Zustand (ist)
        double istFahrzeitSekunden = computeRouteTravelTime(route, consist, netState, maxPowerW, startSpeedMps);

        // Fahrzeit mit condition = 1.0 überall (soll — idealer Zustand)
        double sollFahrzeitSekunden = computeRouteTravelTimeWithPerfectCondition(
                route, consist, netState, maxPowerW, startSpeedMps);

        double deltaProzent = (istFahrzeitSekunden - sollFahrzeitSekunden) / sollFahrzeitSekunden * 100.0;

        // Bottleneck-Liste kommt in Step 5 — hier noch leer
        return Optional.of(new RouteForecast(
                sollFahrzeitSekunden,
                istFahrzeitSekunden,
                deltaProzent,
                List.of()));
    }

    /**
     * Validiert, dass die Route zusammenhängt (T-D41 e).
     *
     * @throws IllegalArgumentException wenn route[i].to() != route[i+1].from() für ein i
     */
    private static void validateRouteConnected(List<Edge> route) {
        for (int i = 0; i < route.size() - 1; i++) {
            if (!route.get(i).to().equals(route.get(i + 1).from())) {
                throw new IllegalArgumentException(
                        "Route is not connected at index " + i + ": " +
                        route.get(i).to() + " != " + route.get(i + 1).from() + " (T-D41 e)");
            }
        }
    }

    /**
     * Berechnet die Fahrzeit für eine Route mit dem aktuellen Netz-Zustand.
     */
    private static double computeRouteTravelTime(
            List<Edge> route,
            Consist consist,
            PowerGrid netState,
            double maxPowerW,
            double startSpeedMps) {

        double totalSeconds = 0.0;
        double currentSpeedMps = startSpeedMps;

        for (Edge edge : route) {
            // Anfang der Kante: hier ist die verfügbare Leistung am höchsten.
            // Diese Vereinfachung ist robust für alle Kantenlängen — der Zug hat hier
            // maximalen Strom und kann beschleunigen.
            double availablePower = netState.availableW(
                    maxPowerW,
                    0.0,  // Anfang der Kante: volle Leistung (abzüglich condition)
                    edge.effectiveCondition(),
                    1.0);

            double edgeSpeed = maxSustainableSpeed(consist, edge.gradient(), availablePower, currentSpeedMps);
            double edgeTime = edge.lengthMeters() / edgeSpeed;
            totalSeconds += edgeTime;
            currentSpeedMps = edgeSpeed;
        }

        return totalSeconds;
    }

    /**
     * Berechnet die Fahrzeit für eine Route mit condition = 1.0 überall (idealer Zustand).
     */
    private static double computeRouteTravelTimeWithPerfectCondition(
            List<Edge> route,
            Consist consist,
            PowerGrid netState,
            double maxPowerW,
            double startSpeedMps) {

        double totalSeconds = 0.0;
        double currentSpeedMps = startSpeedMps;

        for (Edge edge : route) {
            // Perfekte Condition: effectiveReach = maxReachMeters * 1.0 = maxReachMeters
            double availablePower = netState.availableW(
                    maxPowerW,
                    0.0,  // Anfang der Kante: volle Leistung
                    1.0,
                    1.0);

            double edgeSpeed = maxSustainableSpeed(consist, edge.gradient(), availablePower, currentSpeedMps);
            double edgeTime = edge.lengthMeters() / edgeSpeed;
            totalSeconds += edgeTime;
            currentSpeedMps = edgeSpeed;
        }

        return totalSeconds;
    }

    /**
     * Findet die maximal nachhaltige Geschwindigkeit (Gleichgewicht: benötigte Leistung =
     * verfügbare Leistung) per Binary Search.
     *
     * <p>Der Zug kann eine Geschwindigkeit halten, wenn {@code Physics.requiredPowerW == availablePower}.
     * Da {@code requiredPowerW} eine kubische Funktion in {@code v} ist (Air-Drag), gibt es
     * keine geschlossene Lösung — Binary Search findet das Gleichgewicht.
     */
    static double maxSustainableSpeed(Consist consist, double gradient, double availablePowerW, double currentSpeedMps) {
        if (availablePowerW <= 0.0) {
            // Keine Leistung verfügbar: der Zug kann seine Geschwindigkeit nicht halten.
            // Minimale Kriechgeschwindigkeit (NON_ZERO_SPEED) um Division durch 0 zu vermeiden.
            // In einem echten Netz würde der Zug hier stehen bleiben — das Modell ist hier grob.
            return Math.max(currentSpeedMps, NON_ZERO_SPEED_MPS);
        }

        double vLow = MIN_SPEED_MPS;
        double vHigh = MAX_SPEED_MPS;

        // Prüfe, ob überhaupt eine Lösung existiert
        double powerAtHigh = Physics.requiredPowerW(consist, MAX_SPEED_MPS, gradient);
        if (powerAtHigh <= availablePowerW) {
            // Die maximale Geschwindigkeit ist niedriger als unsere Obergrenze
            vHigh = MAX_SPEED_MPS;
        } else {
            // Binary Search nach der Gleichgewichtsdrehzahl
            for (int i = 0; i < BINARY_SEARCH_ITERATIONS; i++) {
                double vMid = (vLow + vHigh) / 2.0;
                double powerAtMid = Physics.requiredPowerW(consist, vMid, gradient);
                if (powerAtMid < availablePowerW) {
                    vLow = vMid;
                } else {
                    vHigh = vMid;
                }
                if (vHigh - vLow < SPEED_TOLERANCE_MPS) {
                    break;
                }
            }
        }

        double equilibriumSpeed = (vLow + vHigh) / 2.0;
        // Der Zug beschleunigt nicht über seine Gleichgewichtsdrehzahl hinaus
        return Math.min(Math.max(equilibriumSpeed, NON_ZERO_SPEED_MPS), MAX_SPEED_MPS);
    }
}
