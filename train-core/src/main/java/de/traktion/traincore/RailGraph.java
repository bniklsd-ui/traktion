package de.traktion.traincore;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Der Schienennetz-Graph (T-D2: der Graph ist die Wahrheit, Blöcke sind Dekoration).
 *
 * <p>Erlaubt Knoten- und Kanten-Mutationen und erzwingt die Z1-Invarianten:
 * <ul>
 *   <li><b>Kein verwaister Knoten:</b> eine Kante kann nur hinzugefügt werden, wenn beide
 *       Endpunkte im Graph existieren. Wird ein Knoten entfernt, werden alle Kanten, die ihn
 *       berühren, mit entfernt.</li>
 *   <li><b>Keine Kante ohne zwei Endpunkte:</b> {@link Edge} prüft dies im Constructor; der
 *       Graph prüft zusätzlich, dass beide Endpunkte registriert sind.</li>
 *   <li><b>{@code RailKind} gesetzt:</b> {@link Edge} prüft {@code railKind != null}.</li>
 *   <li><b>{@code gradient}/{@code lengthMeters} endlich:</b> {@link Edge} prüft dies.</li>
 * </ul>
 *
 * <p><b>Determinismus (Regel 8):</b> {@link LinkedHashMap} für Knoten und {@link LinkedHashSet}
 * für Kanten garantieren Iteration in Einfügereihenfolge. Kein {@code HashMap}/{@code HashSet}
 * — die Iterationsreihenfolge wäre undefiniert und zwei Läufe mit gleichem Seed könnten
 * abweichen. Der Graph ist nicht die Physikschleife, aber der Simulator (Step 7) iteriert über
 * ihn — deterministisch von hier an.
 *
 * <p>IDs werden vom Caller vergeben, nicht vom Graph generiert. Der Graph ist ein reiner
 * Datencontainer (kein Zufallsgenerator, keine Wall-Clock).
 */
public final class RailGraph {

    private final Map<Long, Node> nodes = new LinkedHashMap<>();
    private final Set<Edge> edges = new LinkedHashSet<>();

    /** Fügt einen Knoten hinzu. Wirft, wenn die ID schon existiert (Invariante: ID eindeutig). */
    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        if (nodes.containsKey(node.id())) {
            throw new IllegalStateException("node id already exists: " + node.id());
        }
        nodes.put(node.id(), node);
    }

    /**
     * Fügt eine Kante hinzu. Wirft, wenn ein Endpunkt nicht im Graph existiert (Z1: kein
     * verwaister Knoten). {@link Edge} prüft {@code railKind != null} und endlich-Gradient/Länge.
     */
    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge must not be null");
        }
        if (!nodes.containsKey(edge.from().id())) {
            throw new IllegalStateException(
                "edge endpoint 'from' not in graph: " + edge.from().id());
        }
        if (!nodes.containsKey(edge.to().id())) {
            throw new IllegalStateException(
                "edge endpoint 'to' not in graph: " + edge.to().id());
        }
        edges.add(edge);
    }

    /**
     * Entfernt einen Knoten und alle Kanten, die ihn berühren (Z1: keine verwaisten Kanten).
     * Wirft, wenn der Knoten nicht existiert.
     */
    public void removeNode(long nodeId) {
        Node removed = nodes.remove(nodeId);
        if (removed == null) {
            throw new IllegalStateException("node not in graph: " + nodeId);
        }
        edges.removeIf(e -> e.from().id() == nodeId || e.to().id() == nodeId);
    }

    /** Liefert true, wenn ein Knoten mit der ID existiert. */
    public boolean hasNode(long nodeId) {
        return nodes.containsKey(nodeId);
    }

    /** Liefert den Knoten zur ID, oder wirft, wenn er nicht existiert. */
    public Node node(long nodeId) {
        Node n = nodes.get(nodeId);
        if (n == null) {
            throw new IllegalStateException("node not in graph: " + nodeId);
        }
        return n;
    }

    /** Liefert true, wenn die Kante existiert (Wertgleichheit via {@link Edge#equals}). */
    public boolean hasEdge(Edge edge) {
        return edges.contains(edge);
    }

    /** Unveränderliche Sicht auf alle Knoten (Iteration in Einfügereihenfolge). */
    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /** Unveränderliche Sicht auf alle Kanten (Iteration in Einfügereihenfolge). */
    public Collection<Edge> edges() {
        return Collections.unmodifiableCollection(edges);
    }

    /** Anzahl der Knoten. */
    public int nodeCount() {
        return nodes.size();
    }

    /** Anzahl der Kanten. */
    public int edgeCount() {
        return edges.size();
    }
}
