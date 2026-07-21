package de.traktion.traincore;

/**
 * Knoten im Schienennetz (T-D2: der Graph ist die Wahrheit, Blöcke sind Dekoration).
 *
 * <p>Ein Knoten ist durch seine {@code id} eindeutig identifiziert. Die {@code id} wird vom
 * Caller vergeben (nicht vom Graph generiert), damit der Graph ein reiner Datencontainer bleibt
 * und keine Zufallsgenerierung braucht (Determinismus, Regel 8).
 *
 * <p>Position ist in P1 nicht enthalten (Plan: "ggf. Position"). P4 (Rendering) ergänzt sie,
 * wenn die Token-Position auf die Schienenhöhe gemappt werden muss. Regel 3 (Plan §3): keine
 * Abstraktion für vorgestellte Zukünfte — ein Feld, das heute niemand liest, kommt nicht rein.
 *
 * <p>Record = unveränderlich. Ein Knoten wird nicht mutiert; er wird hinzugefügt oder entfernt.
 */
public record Node(long id) {
}
