package works.hop.graph;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GraphTest {

    static Map<String, Node> vertices() {
        return Map.of("nod1", new Node("func1"),
                "nod2", new Node("func2"),
                "nod3", new Node("func3"),
                "nod4", new Node("func4"),
                "nod5", new Node("func5"),
                "nod6", new Node("func6"),
                "nod7", new Node("func7"),
                "nod8", new Node("func8"),
                "nod9", new Node("func9"),
                "nod10", new Node("func10"));
    }

    static Map<String, Node> letters() {
        return Map.of("A", new Node("A"),
                "B", new Node("B"),
                "C", new Node("C"),
                "D", new Node("D"),
                "E", new Node("E"),
                "F", new Node("F"));
    }

    static Graph graph(Map<String, Node> vertices) {
        return graph(vertices, false);
    }

    static Graph graph(Map<String, Node> vertices, boolean directed) {
        Graph g = new Graph();

        g.addEdge(vertices.get("nod1"), vertices.get("nod2"), 2, directed);
        g.addEdge(vertices.get("nod1"), vertices.get("nod3"), 1, directed);
        g.addEdge(vertices.get("nod1"), vertices.get("nod5"), 2, directed);
        g.addEdge(vertices.get("nod3"), vertices.get("nod7"), 4, directed);
        g.addEdge(vertices.get("nod3"), vertices.get("nod4"), 3, directed);
        g.addEdge(vertices.get("nod2"), vertices.get("nod6"), 2, directed);
        g.addEdge(vertices.get("nod4"), vertices.get("nod5"), 2, directed);
        g.addEdge(vertices.get("nod4"), vertices.get("nod6"), 2, directed);
        g.addEdge(vertices.get("nod4"), vertices.get("nod7"), 3, directed);
        g.addEdge(vertices.get("nod4"), vertices.get("nod9"), 3, directed);
        g.addEdge(vertices.get("nod6"), vertices.get("nod7"), 3, directed);
        g.addEdge(vertices.get("nod7"), vertices.get("nod8"), 4, directed);
        g.addEdge(vertices.get("nod7"), vertices.get("nod10"), 3, directed);

        return g;
    }

    @Test
    void addNode() {
        Map<String, Node> v = vertices();
        Graph g = new Graph();
        g.addNode(v.get("nod1"));
        g.addNode(v.get("nod1"));
        g.addNode(v.get("nod2"));

        assertThat(g.vertices).hasSize(2);
    }

    @Test
    void addEdge() {
        Map<String, Node> v = vertices();
        Graph g = new Graph();
        g.addEdge(v.get("nod1"), v.get("nod2"), 1);
        g.addEdge(v.get("nod1"), v.get("nod3"), 2);
        g.addEdge(v.get("nod2"), v.get("nod4"), 3);

        assertThat(g.vertices).hasSize(4);
        assertThat(g.adjacent).hasSize(4);
    }

    @Test
    void removeEdge() {
        Map<String, Node> v = vertices();
        Graph g = new Graph();
        g.addEdge(v.get("nod1"), v.get("nod2"), 1);
        g.addEdge(v.get("nod1"), v.get("nod3"), 2);
        g.addEdge(v.get("nod2"), v.get("nod4"), 3);

        assertThat(g.vertices).hasSize(4);
        assertThat(g.adjacent).hasSize(4);

        g.removeNode(v.get("nod1"));

        assertThat(g.vertices).hasSize(3);
        assertThat(g.adjacent).hasSize(3);
    }

    @Test
    void check_out_graph_nodes() {
        Map<String, Node> v = vertices();
        Graph g = graph(v);

        assertThat(g.vertices).hasSize(10);
        assertThat(g.adjacent).hasSize(10);

        g.traverse(v.get("nod1"), v.get("nod7"));
        assertThat(g.vertices.stream().filter(Node::isVisited).count()).isGreaterThan(2);

        List<Node> dfs2Visited = g.dfs(v.get("nod1"), v.get("nod7"));
        assertThat(dfs2Visited.size()).isGreaterThan(2);

        List<Node> dfs2Path = g.path(v.get("nod1"), v.get("nod9"), dfs2Visited);
        assertThat(dfs2Path.size()).isGreaterThan(2);

        List<Node> bfsVisited = g.bfs(v.get("nod1"), v.get("nod9"));
        assertThat(bfsVisited.size()).isGreaterThan(2);

        List<Node> bfsPath = g.path(v.get("nod1"), v.get("nod9"), bfsVisited);
        assertThat(bfsPath.size()).isGreaterThan(2);
    }

    @Test
    void topological_sorting(){
        Map<String, Node> v = vertices();
        Graph g = graph(v, true);

        List<Node> sorted = new LinkedList<>();
        g.topSort(v.get("nod1"), sorted, new LinkedList<>());
        assertThat(sorted).hasSizeGreaterThan(2);
    }

    @Test
    void minimum_spanning_tree(){
        Map<String, Node> v = letters();
        Graph g = new Graph();

        g.addEdge(v.get("A"), v.get("B"));
        g.addEdge(v.get("A"), v.get("C"));
        g.addEdge(v.get("A"), v.get("D"));
        g.addEdge(v.get("A"), v.get("E"));
        g.addEdge(v.get("B"), v.get("C"));
        g.addEdge(v.get("B"), v.get("D"));
        g.addEdge(v.get("B"), v.get("E"));
        g.addEdge(v.get("C"), v.get("D"));
        g.addEdge(v.get("C"), v.get("E"));
        g.addEdge(v.get("D"), v.get("E"));

        List<Node> tree = new LinkedList<>();
        g.mst(v.get("A1"), tree, new LinkedList<>());
        assertThat(tree).hasSizeGreaterThan(2);
    }

    @Test
    void dijkstras_shortest_path(){
        Map<String, Node> v = letters();
        Graph g = new Graph();

        g.addEdge(v.get("A"), v.get("B"), 2);
        g.addEdge(v.get("A"), v.get("D"), 8);
        g.addEdge(v.get("B"), v.get("D"), 5);
        g.addEdge(v.get("B"), v.get("E"), 6);
        g.addEdge(v.get("D"), v.get("E"), 3);
        g.addEdge(v.get("D"), v.get("F"), 2);
        g.addEdge(v.get("E"), v.get("C"), 9);
        g.addEdge(v.get("E"), v.get("F"), 1);
        g.addEdge(v.get("F"), v.get("C"), 3);

        List<Node> path = g.dijkstra(v.get("A"), v.get("C"));
        assertThat(path).hasSize(4);
    }
}