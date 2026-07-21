package de.traktion.traincore;

/**
 * Die EINE Physikfunktion (Regel 2). Planer (P3) und Simulator (Step 7) rufen dieselbe Funktion
 * auf — eine zweite Implementierung derselben Formel macht Z5 zu einem Test, der sich selbst
 * bestätigt (Plan §3 Regel 2, §9 Anti-Pattern).
 *
 * <p>Z3: "Leistungsbedarf = f(Masse, v, Steigung); Rekuperation bergab; Zug hält bei
 * Unterversorgung." Diese Funktion liefert den Leistungsbedarf in Watt. Ob er gedeckt ist,
 * entscheidet der Simulator (Step 7) über {@code PowerGrid} — nicht die Physik.
 *
 * <p><b>Formel:</b>
 * <pre>
 *   P = (F_roll + F_grade + F_air) * v
 *   F_roll  = c_roll * m * g              (Rollwiderstand, gegen Bewegung)
 *   F_grade = m * g * gradient           (Steigungswiderstand; negativ bei Gefälle)
 *   F_air   = AIR_DRAG_COEFF * v^2        (Luftwiderstand, gegen Bewegung)
 * </pre>
 *
 * <p>Bei Gefälle (gradient < 0) wird {@code F_grade} negativ. Überwiegt sie die anderen
 * Widerstände, wird {@code P} negativ → Rekuperation (Z3). Bei {@code v = 0} ist {@code P = 0}
 * (stehender Zug braucht keine Fahrleistung). Bei Masse 0 ist {@code P = 0} (kein Zug, kein
 * Bedarf).
 *
 * <p><b>Konstanten</b> sind {@code static final} hier — sichtbar, änderbar, nicht dupliziert.
 * SI-Einheiten: Masse in kg, Geschwindigkeit in m/s, gradient dimensionslos (0.01 = 1 %),
 * Leistung in Watt.
 */
public final class Physics {

    /** Erdbeschleunigung (m/s^2). */
    private static final double G = 9.81;

    /** Rollwiderstandskoeffizient (typisch für Schiene, dimensionslos). */
    private static final double C_ROLL = 0.002;

    /**
     * Luftwiderstands-Konstante (kg/m). Fasst {@code 0.5 * rho * c_w * A} zusammen —
     * plausible Größenordnung für einen Zug. Wird nicht pro-Consist parametrisiert (Regel 3:
     * keine vorgestellte Abstraktion; wenn P2/P4 echte Aero-Daten braucht, wird das ein Typ).
     */
    private static final double AIR_DRAG_COEFF = 8.0;

    private Physics() {
        // Utility-Klasse — keine Instanzen. Die EINE Funktion ist statisch.
    }

    /**
     * Leistungsbedarf in Watt, um einen {@link Consist} mit Geschwindigkeit {@code speedMps}
     * über eine Steigung {@code gradient} zu bewegen (Z3, Regel 2).
     *
     * @param consist    Zugverband (liefert die Masse); darf Masse 0 haben (dann ist P = 0)
     * @param speedMps   Geschwindigkeit in m/s (≥ 0; negativ ist nicht spezifiziert)
     * @param gradient   Steigung dimensionslos (0.0 = eben, 0.01 = 1 % bergauf, -0.01 = Gefälle)
     * @return Leistungsbedarf in Watt; negativ bei Rekuperation (Gefälle überwiegt)
     */
    public static double requiredPowerW(Consist consist, double speedMps, double gradient) {
        if (consist == null) {
            throw new IllegalArgumentException("consist must not be null");
        }
        if (Double.isNaN(speedMps) || Double.isInfinite(speedMps)) {
            throw new IllegalArgumentException("speedMps must be finite: " + speedMps);
        }
        if (Double.isNaN(gradient) || Double.isInfinite(gradient)) {
            throw new IllegalArgumentException("gradient must be finite: " + gradient);
        }
        if (speedMps < 0) {
            throw new IllegalArgumentException("speedMps must be >= 0: " + speedMps);
        }

        double massKg = consist.totalMassKg();
        if (massKg == 0.0 || speedMps == 0.0) {
            return 0.0;
        }

        double fRoll = C_ROLL * massKg * G;
        double fGrade = massKg * G * gradient;
        double fAir = AIR_DRAG_COEFF * speedMps * speedMps;
        double force = fRoll + fGrade + fAir;
        return force * speedMps;
    }
}
