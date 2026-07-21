package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für {@link RailGraph} — Z1-Invarianten (Plan §4/Z1, Step 3 Testliste).
 *
 * <p>Jede Invariante aus dem Plan hat mindestens einen Test. Die Tests sind fallbasiert
 * (JUnit 5); ein optionaler jqwik-Property-Test für Invarianten unter beliebigen
 * Mutations-Sequenzen kommt am Ende.
 */
class RailGraphTest {

    // --- Knoten hinzufügen → Knoten existiert im Graph ---

    @Test
    void addNode_nodeExistsInGraph() {
        RailGraph g = new RailGraph();
        Node n = new Node(1L);
        g.addNode(n);
        assertTrue(g.hasNode(1L));
        assertEquals(n, g.node(1L));
        assertEquals(1, g.nodeCount());
    }

    @Test
    void addNode_duplicateId_throws() {
        RailGraph g = new RailGraph();
        g.addNode(new Node(1L));
        assertThrows(IllegalStateException.class, () -> g.addNode(new Node(1L)));
    }

    @Test
    void addNode_null_throws() {
        RailGraph g = new RailGraph();
        assertThrows(IllegalArgumentException.class, () -> g.addNode(null));
    }

    // --- Kante hinzufügen → Kante existiert, beide Endpunkte gesetzt ---

    @Test
    void addEdge_edgeExistsWithBothEndpoints() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        g.addNode(a);
        g.addNode(b);
        Edge e = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0);
        g.addEdge(e);
        assertTrue(g.hasEdge(e));
        assertEquals(1, g.edgeCount());
    }

    // --- Kante ohne zweiten Endpunkt → Invarianten-Verletzung erkannt ---

    @Test
    void addEdge_missingFromNode_throws() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        g.addNode(b); // 'from' fehlt
        Edge e = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0);
        assertThrows(IllegalStateException.class, () -> g.addEdge(e));
    }

    @Test
    void addEdge_missingToNode_throws() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        g.addNode(a); // 'to' fehlt
        Edge e = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0);
        assertThrows(IllegalStateException.class, () -> g.addEdge(e));
    }

    @Test
    void addEdge_null_throws() {
        RailGraph g = new RailGraph();
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(null));
    }

    // --- Knoten entfernen → verwaiste Kanten werden mit entfernt (Invarianten erhalten) ---

    @Test
    void removeNode_removesOrphanedEdges() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        g.addNode(a);
        g.addNode(b);
        g.addNode(c);
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0);
        Edge bc = new Edge(b, c, RailKind.NORMAL, 0.01, 200.0);
        Edge ca = new Edge(c, a, RailKind.NORMAL, -0.01, 150.0);
        g.addEdge(ab);
        g.addEdge(bc);
        g.addEdge(ca);

        g.removeNode(2L); // b — berührt ab und bc

        assertFalse(g.hasNode(2L));
        assertFalse(g.hasEdge(ab));
        assertFalse(g.hasEdge(bc));
        assertTrue(g.hasEdge(ca)); // berührt b nicht
        assertEquals(2, g.nodeCount());
        assertEquals(1, g.edgeCount());
    }

    @Test
    void removeNode_notInGraph_throws() {
        RailGraph g = new RailGraph();
        assertThrows(IllegalStateException.class, () -> g.removeNode(42L));
    }

    // --- RailKind ist auf jeder Kante gesetzt (nicht null) — Invariante ---

    @Test
    void edge_nullRailKind_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, null, 0.0, 100.0));
    }

    @Test
    void edge_allFiveRailKindsAccepted() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        for (RailKind kind : RailKind.values()) {
            assertDoesNotThrow(() -> new Edge(a, b, kind, 0.0, 100.0));
        }
        assertEquals(5, RailKind.values().length, "T-D21: fünf RailKind-Werte");
    }

    // --- gradient und Länge sind endlich (kein NaN, kein Unendlich) — Invariante ---

    @Test
    void edge_nanGradient_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, RailKind.NORMAL, Double.NaN, 100.0));
    }

    @Test
    void edge_infiniteGradient_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, RailKind.NORMAL, Double.POSITIVE_INFINITY, 100.0));
    }

    @Test
    void edge_nanLength_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, RailKind.NORMAL, 0.0, Double.NaN));
    }

    @Test
    void edge_infiniteLength_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, RailKind.NORMAL, 0.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void edge_zeroLength_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, RailKind.NORMAL, 0.0, 0.0));
    }

    @Test
    void edge_negativeLength_throws() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(a, b, RailKind.NORMAL, 0.0, -1.0));
    }

    @Test
    void edge_nullEndpoint_throws() {
        Node b = new Node(2L);
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(null, b, RailKind.NORMAL, 0.0, 100.0));
        assertThrows(IllegalArgumentException.class,
            () -> new Edge(b, null, RailKind.NORMAL, 0.0, 100.0));
    }

    // --- Iteration in Einfügereihenfolge (Determinismus, Regel 8) ---

    @Test
    void nodes_iterateInInsertionOrder() {
        RailGraph g = new RailGraph();
        g.addNode(new Node(3L));
        g.addNode(new Node(1L));
        g.addNode(new Node(2L));
        long[] ids = g.nodes().stream().mapToLong(Node::id).toArray();
        assertEquals(3L, ids[0]);
        assertEquals(1L, ids[1]);
        assertEquals(2L, ids[2]);
    }

    @Test
    void edges_iterateInInsertionOrder() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        g.addNode(a);
        g.addNode(b);
        g.addNode(c);
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0);
        Edge bc = new Edge(b, c, RailKind.NORMAL, 0.0, 200.0);
        g.addEdge(ab);
        g.addEdge(bc);
        Edge[] edges = g.edges().toArray(Edge[]::new);
        assertEquals(ab, edges[0]);
        assertEquals(bc, edges[1]);
    }
}
