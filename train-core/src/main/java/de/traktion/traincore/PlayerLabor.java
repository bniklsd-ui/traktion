package de.traktion.traincore;

/**
 * Produktions-Implementierung von {@link MaintenanceSupply} (T-D28, T-D29).
 *
 * <p>Zeit-Akkumulator: der Spieler hat unbegrenzte Ausdauer, aber <b>Zeit ist der
 * Engpass</b>. Der Vorrat {@code workAvailable} wird pro {@link #tick(double)} um
 * {@code ratePerTick * dt} erhöht, gedeckelt bei {@code maxWork}.
 *
 * <p>Defaults (T-D29):
 * <ul>
 *   <li>{@code ratePerTick = 5} Einheiten pro Sekunde</li>
 *   <li>{@code maxWork = 20} Einheiten (Obergrenze)</li>
 *   <li>Start: {@code workAvailable = 0}</li>
 * </ul>
 *
 * <p>Modelliert: ein Spieler sammelt 5 "Arbeitspunkte" pro Sekunde an, bis maximal 20.
 * Mit {@code withdraw(n)} entnimmt er Punkte; wenn der Vorrat leer ist, gibt {@code 0}
 * zurück. Der Spieler muss warten, bis sich der Vorrat wieder füllt.
 *
 * <p>Die Einheit "Instandhaltungspunkte" ist so kalibriert, dass eine vollständige
 * Reparatur einer Kante (rail oder overhead von 0 auf 1) in endlicher, aber nicht
 * instantaner Zeit möglich ist — der genaue Preis hängt von der Reparatur-Logik ab
 * (P4-Weltinteraktion).
 */
public final class PlayerLabor implements MaintenanceSupply {

    /** Standard-Rate: 5 Einheiten pro Sekunde (T-D29 Default). */
    public static final int DEFAULT_RATE_PER_SECOND = 5;

    /** Standard-Obergrenze: 20 Einheiten (T-D29 Default). */
    public static final int DEFAULT_MAX_WORK = 20;

    private final int ratePerSecond;
    private final int maxWork;

    private int workAvailable;

    /**
     * Konstruktor mit expliziten Werten.
     *
     * @param ratePerSecond Auffüllrate pro Sekunde (> 0)
     * @param maxWork      maximale Einheiten (≥ 0)
     */
    public PlayerLabor(int ratePerSecond, int maxWork) {
        if (ratePerSecond <= 0) {
            throw new IllegalArgumentException("ratePerSecond must be > 0: " + ratePerSecond);
        }
        if (maxWork < 0) {
            throw new IllegalArgumentException("maxWork must be >= 0: " + maxWork);
        }
        this.ratePerSecond = ratePerSecond;
        this.maxWork = maxWork;
        this.workAvailable = 0;
    }

    /**
     * Standard-Konstruktor (T-D29 Defaults: rate=5, maxWork=20).
     */
    public PlayerLabor() {
        this(DEFAULT_RATE_PER_SECOND, DEFAULT_MAX_WORK);
    }

    @Override
    public int withdraw(int requested) {
        if (requested < 0) {
            throw new IllegalArgumentException("requested must be >= 0: " + requested);
        }
        if (requested == 0) {
            return 0;
        }
        int actual = Math.min(requested, workAvailable);
        workAvailable -= actual;
        return actual;
    }

    @Override
    public void tick(double dtSeconds) {
        if (Double.isNaN(dtSeconds) || Double.isInfinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be finite: " + dtSeconds);
        }
        if (dtSeconds < 0) {
            throw new IllegalArgumentException("dtSeconds must be >= 0: " + dtSeconds);
        }
        if (dtSeconds == 0) {
            return; // nichts zu tun
        }
        // Auffüllung: rate * dt, geclampt bei maxWork
        double earned = (double) ratePerSecond * dtSeconds;
        workAvailable = (int) Math.min(maxWork, workAvailable + earned);
    }

    /**
     * Aktueller Vorrat (主要 für Tests).
     */
    public int workAvailable() {
        return workAvailable;
    }

    /**
     * Maximale Kapazität.
     */
    public int maxWork() {
        return maxWork;
    }

    /**
     * Rate pro Sekunde.
     */
    public int ratePerSecond() {
        return ratePerSecond;
    }
}
