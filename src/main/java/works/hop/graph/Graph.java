package works.hop.graph;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    final List<Node> vertices = new LinkedList<>();
    final Map<Node, List<Edge>> adjacent = new LinkedHashMap<>();

    public void addNode(Node node) {
        if (!this.vertices.contains(node)) {
            this.vertices.add(node);
        }
    }

    public void addEdge(Node from, Node to, int weight) {
        this.addEdge(from, to, weight, false);
    }

    public void addEdge(Node from, Node to, int weight, boolean directed) {
        this.addNode(from);
        this.addNode(to);
        if (!this.adjacent.containsKey(from)) {
            this.adjacent.put(from, from.getEdges());
        }
        this.adjacent.get(from).add(new Edge(to, weight));

        if (!directed) {
            this.addEdge(to, from, weight, true);
        }
    }

    public void removeNode(Node node) {
        // remove from vertices
        this.vertices.remove(node);
        // remove from adjacent keys
        this.adjacent.remove(node);
        // remove from relationships
        for (Node from : this.adjacent.keySet()) {
            this.adjacent.get(from).removeIf(edge -> edge.node == node);
        }
    }

    public void traverse(Node start, Node end) {
        if(!start.isVisited()) {
            start.setVisited(true);
            for (Edge edge : start.getEdges()) {
                Node node = edge.getNode();
                if (!node.isVisited()) {
                    if (node == end) {
                        break;
                    }
                    traverse(node, end);
                }
            }
        }
    }

    public List<Node> dfs(Node start, Node end) {
        List<Node> visited = new LinkedList<>();
        Stack<Node> stack = new Stack<>();
        stack.add(start);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            visited.add(node);
            if (node == end) {
                break;
            }
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                if (!visited.contains(next) && !stack.contains(next)) {
                    next.setDepth(node.getDepth() + 1);
                    stack.add(next);
                }
            }
        }
        return visited;
    }

    public List<Node> bfs(Node start, Node end) {
        List<Node> visited = new LinkedList<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            visited.add(node);
            if (node == end) {
                break;
            }
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                if (!visited.contains(next) && !queue.contains(next)) {
                    next.setDepth(node.getDepth() + 1);
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    public List<Node> findPath(Node start, Node end, List<Node> visited) {
        Node last = end;
        List<Node> path = new LinkedList<>();
        path.add(end);
        for (int i = visited.size() - 1; i > 0; i--) {
            Node node = visited.get(i);
            if (node.getDepth() >= last.getDepth()) {
                continue;
            }

            List<Node> adj = node.getEdges().stream().map(Edge::getNode).collect(Collectors.toList());
            if (adj.contains(last)) {
                path.add(node);
                last = node;
            }
        }
        path.add(start);
        return path;
    }
}
