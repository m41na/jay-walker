package works.hop.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphTest {

    Node nod1 = new Node("func1");
    Node nod2 = new Node("func2");
    Node nod3 = new Node("func3");
    Node nod4 = new Node("func4");
    Node nod5 = new Node("func5");
    Node nod6 = new Node("func6");
    Node nod7 = new Node("func7");
    Node nod8 = new Node("func8");
    Node nod9 = new Node("func9");
    Node nod10 = new Node("func10");

    @Test
    void addNode() {
        Graph g = new Graph();
        g.addNode(nod1);
        g.addNode(nod1);
        g.addNode(nod2);

        assertThat(g.vertices).hasSize(2);
    }

    @Test
    void addEdge() {
        Graph g = new Graph();
        g.addEdge(nod1, nod2, 1);
        g.addEdge(nod1, nod3, 2);
        g.addEdge(nod2, nod4, 3);

        assertThat(g.vertices).hasSize(4);
        assertThat(g.adjacent).hasSize(4);
    }

    @Test
    void removeEdge() {
        Graph g = new Graph();
        g.addEdge(nod1, nod2, 1);
        g.addEdge(nod1, nod3, 2);
        g.addEdge(nod2, nod4, 3);

        assertThat(g.vertices).hasSize(4);
        assertThat(g.adjacent).hasSize(4);

        g.removeNode(nod1);

        assertThat(g.vertices).hasSize(3);
        assertThat(g.adjacent).hasSize(3);
    }

    @Test
    void check_out_graph_nodes() {
        Graph g = new Graph();
        g.addEdge(nod1, nod2, 2);
        g.addEdge(nod1, nod3, 1);
        g.addEdge(nod1, nod5, 2);
        g.addEdge(nod3, nod7, 4);
        g.addEdge(nod3, nod4, 3);
        g.addEdge(nod2, nod6, 2);
        g.addEdge(nod4, nod5, 2);
        g.addEdge(nod4, nod6, 2);
        g.addEdge(nod4, nod7, 3);
        g.addEdge(nod4, nod9, 3);
        g.addEdge(nod6, nod7, 3);
        g.addEdge(nod7, nod8, 4);
        g.addEdge(nod7, nod10, 3);

        assertThat(g.vertices).hasSize(10);
        assertThat(g.adjacent).hasSize(10);

        g.traverse(nod1, nod7);
        assertThat(g.vertices.stream().filter(Node::isVisited).count()).isGreaterThan(2);

        List<Node> dfs2Visited = g.dfs(nod1, nod7);
        assertThat(dfs2Visited.size()).isGreaterThan(2);

        List<Node> dfs2Path = g.findPath(nod1, nod9, dfs2Visited);
        assertThat(dfs2Path.size()).isGreaterThan(2);

        List<Node> bfsVisited = g.bfs(nod1, nod9);
        assertThat(bfsVisited.size()).isGreaterThan(2);

        List<Node> bfsPath = g.findPath(nod1, nod9, bfsVisited);
        assertThat(bfsPath.size()).isGreaterThan(2);
    }
}