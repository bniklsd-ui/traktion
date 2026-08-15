package de.traktion.traincore;

/**
 * Produktions-Implementierung von {@link PowerSupply} (T-D30, Plan §3.2).
 *
 * <p>Fester Maximal-Output, Brennstoff von Hand — die dauerhafte Rückfallebene (Regel 4).
 * Modelliert einen Generator mit endlichem Brennstoff-Vorrat und einer Maximalleistung.
 *
 * <p>Kalibrierung: {@code fuelAmount} in Megajoule (MJ). 1000 MJ ≈ 278 kWh. Ein vollgetankter
 * 100-kW-Generator läuft damit etwa 2.78 Stunden. Das ist ein vernünftiger Skalierungsfaktor.
 *
 * <p>Verhalten:
 * <ul>
 *   <li>{@code supply(reqW, dt)} liefert {@code min(reqW, maxOutputW, fuelAmount*1e6/dt)} in Watt</li>
 *   <li>Brennstoff-Verbrauch: für {@code delivered} Watt über {@code dt} Sekunden werden
 *       {@code delivered * dt / 1e6} MJ verbraucht</li>
 *   <li>Leerer Tank → liefert {@code 0}</li>
 *   <li>{@code refuel(amount)} fügt {@code amount} MJ hinzu (Test-Hook; P4 nutzt das für
 *       Weltinteraktion)</li>
 * </ul>
 *
 * <p>Mit {@code FixedSupply} (Test-Stub) sind zwei Implementierungen für {@code PowerSupply}
 * vorhanden — Regel 3 erfüllt.
 */
public final class ManualGenerator implements PowerSupply {

    /** Standard-Maximalleistung in Watt (100 kW). */
    public static final double DEFAULT_MAX_OUTPUT_W = 100_000.0;

    /** Standard-Brennstoffmenge in MJ (1000 MJ ≈ 278 kWh). */
    public static final double DEFAULT_INITIAL_FUEL_MJ = 1_000.0;

    private final double maxOutputW;
    private double fuelMj; // in Megajoule

    /**
     * Konstruktor mit expliziten Werten.
     *
     * @param maxOutputW  maximale Leistung in Watt (> 0)
     * @param fuelMj      anfängliche Brennstoffmenge in Megajoule (≥ 0)
     */
    public ManualGenerator(double maxOutputW, double fuelMj) {
        if (maxOutputW <= 0 || Double.isInfinite(maxOutputW)) {
            throw new IllegalArgumentException("maxOutputW must be > 0 and finite: " + maxOutputW);
        }
        if (Double.isNaN(fuelMj) || Double.isInfinite(fuelMj) || fuelMj < 0) {
            throw new IllegalArgumentException("fuelMj must be >= 0 and finite: " + fuelMj);
        }
        this.maxOutputW = maxOutputW;
        this.fuelMj = fuelMj;
    }

    /**
     * Standard-Konstruktor (T-D30 Defaults: maxOutput=100 kW, fuel=1000 MJ).
     */
    public ManualGenerator() {
        this(DEFAULT_MAX_OUTPUT_W, DEFAULT_INITIAL_FUEL_MJ);
    }

    @Override
    public double supply(double requestedW, double dtSeconds) {
        // Vertrags-Validierung
        if (Double.isNaN(requestedW) || Double.isInfinite(requestedW) || requestedW < 0) {
            return 0.0;
        }
        if (Double.isNaN(dtSeconds) || Double.isInfinite(dtSeconds) || dtSeconds <= 0) {
            return 0.0;
        }
        if (requestedW == 0 || fuelMj <= 0) {
            return 0.0;
        }

        // Hardware-Limit: maxOutputW
        double maxFromHardware = maxOutputW;

        // Brennstoff-Limit: fuelMj in Joule / dtSeconds = verfügbare Leistung
        // fuelMj [MJ] * 1e6 [J/MJ] / dtSeconds [s] = Leistung [W]
        double maxFromFuel = fuelMj * 1_000_000.0 / dtSeconds;

        double maxDeliverableW = Math.min(requestedW, Math.min(maxFromHardware, maxFromFuel));
        if (maxDeliverableW <= 0) {
            return 0.0;
        }

        // Gelieferte Energie: Watt * Sekunden = Joule
        double deliveredJ = maxDeliverableW * dtSeconds;

        // Brennstoff-Verbrauch: Joule / 1e6 = MJ
        double consumedMj = deliveredJ / 1_000_000.0;
        fuelMj -= consumedMj;
        if (fuelMj < 0) {
            fuelMj = 0; // Clamping bei numerischer Ungenauigkeit
        }

        return maxDeliverableW;
    }

    /**
     * Betankt den Generator mit {@code amountMJ} Megajoule Brennstoff.
     *
     * @param amountMJ zusätzliche Brennstoffmenge in MJ (≥ 0)
     * @throws IllegalArgumentException wenn amount negativ oder nicht endlich
     */
    public void refuel(double amountMJ) {
        if (Double.isNaN(amountMJ) || Double.isInfinite(amountMJ)) {
            throw new IllegalArgumentException("amountMJ must be finite: " + amountMJ);
        }
        if (amountMJ < 0) {
            throw new IllegalArgumentException("amountMJ must be >= 0: " + amountMJ);
        }
        fuelMj += amountMJ;
    }

    /**
     * Verbleibender Brennstoff in Megajoule.
     */
    public double fuelMj() {
        return fuelMj;
    }

    /**
     * Maximale Leistung in Watt.
     */
    public double maxOutputW() {
        return maxOutputW;
    }
}
