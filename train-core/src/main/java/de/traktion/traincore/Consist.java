package de.traktion.traincore;

/**
 * Zugverband (T-D7). Der Kern kennt Masse, keine Items.
 *
 * <p>{@code payloadMassKg} ist eine Zahl von außen — der Kern fragt nicht, woher sie kommt
 * (Regel 7: "Der Kern kennt keine Items. Nur {@code double}."). Fracht ist kein Port (T-D7);
 * ein anderer Lieferant (P2: {@code MaintenanceSupply} / P4: Weltinteraktion) schreibt dieselben
 * Zahlen.
 *
 * <p>Invarianten (im kompakten Constructor durchgesetzt):
 * <ul>
 *   <li>{@code carCount} ≥ 1 — ein leerer Zugverband ist kein Zugverband.</li>
 *   <li>{@code tareMassKg} ≥ 0 und endlich — Leermasse nicht-negativ.</li>
 *   <li>{@code payloadMassKg} ≥ 0 und endlich — Nutzlast nicht-negativ.</li>
 * </ul>
 *
 * <p>Record = unveränderlich. Ein {@code Consist} wird nicht mutiert; wenn sich die Last ändert,
 * entsteht ein neuer {@code Consist} (z.B. nach Be-/Entladung in P4).
 */
public record Consist(int carCount, double tareMassKg, double payloadMassKg) {

    public Consist {
        if (carCount < 1) {
            throw new IllegalArgumentException("carCount must be >= 1 (T-D7): " + carCount);
        }
        if (!(tareMassKg >= 0) || Double.isInfinite(tareMassKg)) {
            throw new IllegalArgumentException(
                "tareMassKg must be finite and >= 0 (T-D7): " + tareMassKg);
        }
        if (!(payloadMassKg >= 0) || Double.isInfinite(payloadMassKg)) {
            throw new IllegalArgumentException(
                "payloadMassKg must be finite and >= 0 (T-D7): " + payloadMassKg);
        }
    }

    /**
     * Gesamtmasse in kg = Leermasse + Nutzlast (T-D7).
     *
     * <p>Die Summe ist endlich, weil beide Summanden endlich sind (Constructor-Invariante).
     * {@code carCount} geht hier nicht ein — die Masse pro Wagen ist in {@code tareMassKg}
     * bereits aggregiert. Wenn P2/P4 eine pro-Wagen-Auflösung braucht, wird das ein eigener
     * Typ (Regel 3: keine vorgestellte Abstraktion).
     */
    public double totalMassKg() {
        return tareMassKg + payloadMassKg;
    }
}
