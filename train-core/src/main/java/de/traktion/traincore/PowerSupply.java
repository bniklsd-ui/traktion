package de.traktion.traincore;

/**
 * Port 1 — woher der Strom kommt (Plan §3.2).
 *
 * <p>Zwei heute benennbare Implementierungen (Regel 3, T-D22):
 * <ul>
 *   <li><b>FixedSupply</b> (heute, Test-Package) — liefert {@code requestedW} immer. Für Tests,
 *       nicht Produktion.</li>
 *   <li><b>ManualGenerator</b> (P2, Plan §3.2) — fester Output, Brennstoff von Hand. Die
 *       dauerhafte Rückfallebene (Regel 4).</li>
 * </ul>
 *
 * <p>Später: {@code IndustrialGrid} (Kraftwerke, Netz, Speicher) — nicht in Teil 1.
 *
 * <p>Vertrag (Plan §3.2): {@code supply} liefert höchstens {@code requestedW}, weniger wenn
 * nichts da ist. Die Einheit ist Watt. {@code dtSeconds} ist die Zeitspanne, über die die
 * Leistung geliefert wird (für Energiebilanz, nicht für Spannungsabfall — der kommt im
 * {@code PowerGrid}).
 */
public interface PowerSupply {
    /**
     * Liefert höchstens {@code requestedW} über {@code dtSeconds}. Weniger, wenn nichts da ist.
     *
     * @param requestedW angeforderte Leistung in Watt (≥ 0)
     * @param dtSeconds  Zeitspanne in Sekunden (> 0)
     * @return gelieferte Leistung in Watt (0 ≤ result ≤ requestedW)
     */
    double supply(double requestedW, double dtSeconds);
}
