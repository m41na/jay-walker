package works.hop.graph;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    static Map<String, Node> letters(char start, char end) {
        return IntStream.rangeClosed(start, end).mapToObj(i -> new Node(Character.toString((char) i)))
                .collect(Collectors.toMap(n -> n.name, n -> n));
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

        List<Node> visited = new LinkedList<>();
        g.depth_first_post_order_traversal(v.get("nod1"), visited::add, visited);
        assertThat(visited).hasSize(10);

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
    void find_minimum_spanning_tree() {
        Map<String, Node> v = letters('A', 'E');
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
        g.minimum_spanning_tree(v.get("A1"), tree, new LinkedList<>());
        assertThat(tree).hasSizeGreaterThan(2);
    }

    @Test
    void find_bridges_and_articulation_points() {
        Map<String, Node> v = letters('A', 'I');
        Graph g = new Graph();

        g.addEdge(v.get("A"), v.get("B"));
        g.addEdge(v.get("B"), v.get("C"));
        g.addEdge(v.get("C"), v.get("A"));
        g.addEdge(v.get("C"), v.get("D"));
        g.addEdge(v.get("D"), v.get("E"));
        g.addEdge(v.get("C"), v.get("F"));
        g.addEdge(v.get("F"), v.get("G"));
        g.addEdge(v.get("G"), v.get("H"));
        g.addEdge(v.get("H"), v.get("I"));
        g.addEdge(v.get("I"), v.get("F"));

        List<List<Node>> bridges = new LinkedList<>();
        g.bridges_and_articulation_points(v.get("A"), bridges);
        assertThat(bridges).hasSize(2);
    }

    @Test
    void shortest_path_for_weighted_graph_using_dijkstras() {
        Map<String, Node> v = letters('A', 'F');
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

        List<Node> path = g.shortest_path_weighted_dijkstra(v.get("A"), v.get("C"));
        assertThat(path).hasSize(5);
        assertThat(path.stream().map(vl -> vl.name).collect(Collectors.toList())).containsAll(List.of("C", "F", "D", "B", "A"));
    }

    @Test
    void shortest_path_for_unweighted_graph_using_bfs() {
        Map<String, Node> v = letters('A', 'E');
        Graph g = new Graph();

        g.addEdge(v.get("A"), v.get("B"), 0, true);
        g.addEdge(v.get("A"), v.get("C"), 0, true);
        g.addEdge(v.get("B"), v.get("D"), 0, true);
        g.addEdge(v.get("C"), v.get("B"), 0, true);
        g.addEdge(v.get("C"), v.get("E"), 0, true);
        g.addEdge(v.get("E"), v.get("D"), 0, true);

        List<Node> path = g.shortest_path_unweighted_bfs(v.get("A"), v.get("D"));
        assertThat(path).hasSize(3);
        assertThat(path.stream().map(vl -> vl.name).collect(Collectors.toList())).containsAll(List.of("D", "B", "A"));
    }

    @Test
    void directed_acyclic_graph_ordering_topological_sort() {
        Map<String, Node> v = letters('A', 'M');
        Graph g = new Graph();

        g.addEdge(v.get("A"), v.get("D"), 0, true);
        g.addEdge(v.get("B"), v.get("D"), 0, true);
        g.addEdge(v.get("C"), v.get("A"), 0, true);
        g.addEdge(v.get("C"), v.get("B"), 0, true);
        g.addEdge(v.get("D"), v.get("G"), 0, true);
        g.addEdge(v.get("D"), v.get("H"), 0, true);
        g.addEdge(v.get("E"), v.get("A"), 0, true);
        g.addEdge(v.get("E"), v.get("D"), 0, true);
        g.addEdge(v.get("E"), v.get("F"), 0, true);
        g.addEdge(v.get("F"), v.get("J"), 0, true);
        g.addEdge(v.get("F"), v.get("K"), 0, true);
        g.addEdge(v.get("G"), v.get("I"), 0, true);
        g.addEdge(v.get("H"), v.get("I"), 0, true);
        g.addEdge(v.get("H"), v.get("J"), 0, true);
        g.addEdge(v.get("I"), v.get("L"), 0, true);
        g.addEdge(v.get("J"), v.get("L"), 0, true);
        g.addEdge(v.get("J"), v.get("M"), 0, true);
        g.addEdge(v.get("K"), v.get("J"), 0, true);

        List<Node> path = g.dag_topological_sort();
        assertThat(path).hasSize(13);
    }
}