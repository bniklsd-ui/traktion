package de.traktion.traincore;

/**
 * Semantische Gleisart auf der Kante (T-D9, T-D21).
 *
 * <p>Fünf Werte: {@link #NORMAL}, {@link #STATION}, {@link #HIGH_SPEED}, {@link #DEPOT},
 * {@link #TERMINUS}. In P1 wird nur {@link #NORMAL} semantisch genutzt — die anderen sind
 * Platzhalter für P2/P5 (Plan §5/P1, T-D21). Blockabschnitte werden aus der Topologie
 * abgeleitet (T-D9), {@code RailKind} steuert das später mit.
 *
 * <p>Regel 3 (Plan §3) gilt für Interfaces, nicht für Enum-Werte — ein Enum mit Platzhaltern
 * ist kein Anti-Pattern (T-D21).
 */
public enum RailKind {
    /** Normales Gleis. In P1 der einzige semantisch genutzte Wert. */
    NORMAL,
    /** Bahnhofsgleis. Platzhalter für P5 (Fahrplan). */
    STATION,
    /** Hochgeschwindigkeitsgleis. Platzhalter für P2/P5. */
    HIGH_SPEED,
    /** Betriebswerk-Gleis. Platzhalter für P2 (Verschleiß/Reparatur). */
    DEPOT,
    /** Endgleis / Stumpen. Platzhalter für P5. */
    TERMINUS
}
