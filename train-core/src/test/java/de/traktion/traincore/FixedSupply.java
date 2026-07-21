package de.traktion.traincore;

/**
 * Test-Implementierung von {@link PowerSupply} (T-D22). Liefert {@code requestedW} immer —
 * für Tests, nicht Produktion.
 *
 * <p>Die zweite heute benennbare Implementierung ist {@code ManualGenerator} (P2, Plan §3.2)
 * — fester Output, Brennstoff von Hand. Damit erfüllt {@code PowerSupply} Regel 3 (zwei heute
 * benennbare Implementierungen).
 *
 * <p>Diese Klasse liegt im Test-Package, weil sie eine Test-Hilfe ist, kein Produktionscode.
 * Sie ist bewusst trivial — ihre Aufgabe ist es, den {@code PowerSupply}-Port in Tests zu
 * befriedigen, nicht, echte Stromversorgung zu modellieren.
 */
public final class FixedSupply implements PowerSupply {

    /** Liefert {@code requestedW} immer (wenn ≥ 0), sonst 0. */
    @Override
    public double supply(double requestedW, double dtSeconds) {
        if (!(requestedW >= 0) || Double.isInfinite(requestedW)) {
            return 0.0;
        }
        if (!(dtSeconds > 0) || Double.isInfinite(dtSeconds)) {
            return 0.0;
        }
        return requestedW;
    }
}
