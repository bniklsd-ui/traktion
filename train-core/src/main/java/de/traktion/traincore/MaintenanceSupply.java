package de.traktion.traincore;

/**
 * Port 2 — woher die Instandhaltungsgüter kommen (Plan §3.2).
 *
 * <p>Zwei heute benennbare Implementierungen (Regel 3, T-D28):
 * <ul>
 *   <li><b>PlayerLabor</b> (heute, P2) — Spieler repariert selbst, kostet Spielzeit.
 *       Zeit-Akkumulator: {@code workAvailable} wird pro Tick aufgefüllt.</li>
 *   <li><b>DepotStock</b> (später, P5) — Betriebswerk, per Zug beliefert.</li>
 * </ul>
 *
 * <p>Vertrag (Plan §3.2): {@code withdraw} entnimmt bis zu {@code requested} Einheiten
 * aus dem Vorrat und gibt zurück, wie viele es tatsächlich wurden. Die Einheit ist
 * "Instandhaltungspunkte" — ein abstraktes Maß, das die Zeit modelliert, die der Spieler
 * investiert.
 *
 * <p>Der Vorrat ist <b>nicht instant</b> aufgefüllt — {@link PlayerLabor} treibt die Zeit
 * über {@code tick(dt)} voran. Ein kostenloser Stub (unendlicher Vorrat ohne Zeit) würde
 * Z6 und Z7 zu leeren Tests machen (T-D29).
 */
public interface MaintenanceSupply {

    /**
     * Entnimmt bis zu {@code requested} Einheiten aus dem Vorrat.
     *
     * @param requested angeforderte Einheiten (≥ 0)
     * @return tatsächlich entnommene Einheiten (0 ≤ result ≤ requested)
     */
    int withdraw(int requested);

    /**
     * Treibt die Zeit um {@code dtSeconds} voran und füllt den Vorrat auf.
     * Wie schnell aufgefüllt wird, ist implementierungsabhängig.
     *
     * @param dtSeconds vergangene Zeit in Sekunden (> 0)
     */
    void tick(double dtSeconds);
}
