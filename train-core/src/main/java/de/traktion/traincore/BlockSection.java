package de.traktion.traincore;

/**
 * Ein Blockabschnitt (Z2, T-D9). Aus der Topologie abgeleitet — in P1 ist jede Kante ein
 * Abschnitt. Komplexere Modelle (Abschnitte zwischen Weichen) kommen später.
 *
 * <p>Ein Abschnitt hat genau einen {@code owner} (Token-ID) oder keinen (frei). Z2: zwei Züge
 * nie im selben Abschnitt — Reservierung ist exklusiv.
 *
 * <p>Veränderlich: der {@code owner} wird bei Reservierung gesetzt und bei Freigabe gelöscht.
 * Der Abschnitt selbst ist stabil (identifiziert durch seine {@code id}).
 */
public final class BlockSection {

    private final long id;
    private final Edge edge;
    private Long owner; // Token-ID oder null (frei)

    /**
     * @param id   eindeutige Abschnitts-ID (Caller vergeben, Determinismus)
     * @param edge Kante, die dieser Abschnitt abdeckt (T-D9: aus Topologie)
     */
    public BlockSection(long id, Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge must not be null");
        }
        this.id = id;
        this.edge = edge;
    }

    public long id() { return id; }
    public Edge edge() { return edge; }

    /** True, wenn der Abschnitt frei ist (kein owner). */
    public boolean isFree() { return owner == null; }

    /** Token-ID des owners, oder null wenn frei. */
    public Long owner() { return owner; }

    /** Reserviert den Abschnitt für {@code tokenId}. Wirft, wenn belegt. */
    void reserve(long tokenId) {
        if (owner != null && owner != tokenId) {
            throw new IllegalStateException(
                "section " + id + " already reserved by token " + owner);
        }
        owner = tokenId;
    }

    /** Gibt den Abschnitt frei. Wirft, wenn er nicht von {@code tokenId} gehalten wird. */
    void release(long tokenId) {
        if (owner == null) {
            throw new IllegalStateException("section " + id + " is free");
        }
        if (owner != tokenId) {
            throw new IllegalStateException(
                "section " + id + " held by " + owner + ", not " + tokenId);
        }
        owner = null;
    }
}
