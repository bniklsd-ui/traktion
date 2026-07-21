package de.traktion.traincore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Veraltet alle {@link BlockSection}s und erzwingt Z2 + T-D23.
 *
 * <p><b>T-D9:</b> Blockabschnitte werden aus der Topologie abgeleitet. In P1 ist jede Kante
 * ein Abschnitt — {@link #fromGraph(RailGraph)} erzeugt sie automatisch.
 *
 * <p><b>Z2:</b> zwei Züge nie im selben Abschnitt. {@link #reserve(long, long)} reserviert
 * exklusiv; {@link #isFree(long)} prüft; {@link #release(long, long)} gibt frei.
 *
 * <p><b>T-D23 (triviale Deadlock-Erkennung):</b> ein Deadlock liegt vor, wenn es einen Zyklus
 * im Wartegraphen gibt — Token A hält X und wartet auf Y, Token B hält Y und wartet auf X.
 * {@link #hasDeadlock()} erkennt das durch Verfolgen der Wartekette. <b>Nicht aufgelöst</b> —
 * Auflösung kommt in P5 (Fahrplan).
 *
 * <p><b>Determinismus (Regel 8):</b> {@link LinkedHashMap} für Abschnitte (Iteration in
 * Einfügereihenfolge), {@link ArrayList} für die Wartegraph-Verfolgung. Kein {@code HashSet}.
 */
public final class BlockSystem {

    private final Map<Long, BlockSection> sections = new LinkedHashMap<>();
    private final Map<Long, Long> waitingFor = new LinkedHashMap<>(); // tokenId → sectionId

    /** Leitet Blockabschnitte aus dem Graph ab (T-D9: jede Kante ist ein Abschnitt). */
    public static BlockSystem fromGraph(RailGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        BlockSystem system = new BlockSystem();
        long sectionId = 1L;
        for (Edge edge : graph.edges()) {
            system.sections.put(sectionId, new BlockSection(sectionId, edge));
            sectionId++;
        }
        return system;
    }

    /** Liefert den Abschnitt zur ID, oder wirft. */
    public BlockSection section(long sectionId) {
        BlockSection s = sections.get(sectionId);
        if (s == null) {
            throw new IllegalStateException("section not found: " + sectionId);
        }
        return s;
    }

    /** Liefert den Abschnitt, der eine Kante abdeckt (nach Kanten-Identität). */
    public BlockSection sectionFor(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge must not be null");
        }
        for (BlockSection s : sections.values()) {
            if (s.edge().equals(edge)) {
                return s;
            }
        }
        throw new IllegalStateException("no section for edge: " + edge);
    }

    /** True, wenn der Abschnitt frei ist (Z2-Check). */
    public boolean isFree(long sectionId) {
        return section(sectionId).isFree();
    }

    /**
     * Reserviert den Abschnitt für {@code tokenId} (Z2: exklusiv).
     *
     * @return true, wenn reserviert; false, wenn belegt (dann wird {@code tokenId} als
     *         wartend auf diesen Abschnitt eingetragen — für Deadlock-Erkennung)
     */
    public boolean reserve(long tokenId, long sectionId) {
        BlockSection s = section(sectionId);
        if (s.isFree() || s.owner() == tokenId) {
            s.reserve(tokenId);
            waitingFor.remove(tokenId);
            return true;
        }
        waitingFor.put(tokenId, sectionId);
        return false;
    }

    /** Gibt den Abschnitt frei (Z2). */
    public void release(long tokenId, long sectionId) {
        section(sectionId).release(tokenId);
        waitingFor.remove(tokenId);
    }

    /**
     * Triviale Deadlock-Erkennung (T-D23): verfolgt die Wartekette. Ein Deadlock liegt vor,
     * wenn ein Zyklus existiert — Token A wartet auf Abschnitt gehalten von B, B wartet auf
     * Abschnitt gehalten von A (oder längere Zyklen).
     *
     * @return true, wenn ein Deadlock erkannt wurde (nicht aufgelöst — P5)
     */
    public boolean hasDeadlock() {
        for (Long startToken : waitingFor.keySet()) {
            if (detectCycleFrom(startToken, new ArrayList<>())) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycleFrom(Long token, List<Long> visited) {
        if (visited.contains(token)) {
            return true; // Zyklus gefunden
        }
        visited.add(token);
        Long waitingSectionId = waitingFor.get(token);
        if (waitingSectionId == null) {
            return false; // Token wartet nicht
        }
        BlockSection heldBy = sections.get(waitingSectionId);
        if (heldBy == null || heldBy.owner() == null) {
            return false; // Abschnitt frei oder nicht gefunden
        }
        Long holder = heldBy.owner();
        return detectCycleFrom(holder, visited);
    }

    /** Anzahl der Abschnitte. */
    public int sectionCount() {
        return sections.size();
    }
}
