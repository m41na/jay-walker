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

    public void addEdge(Node from, Node to) {
        this.addEdge(from, to, 0, false);
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
        if (!start.isVisited()) {
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

    public List<Node> path(Node start, Node end, List<Node> visited) {
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

    public void topSort(Node start, List<Node> path, List<Node> visited) {
        for (Edge edge : start.getEdges()) {
            Node node = edge.getNode();
            if (!visited.contains(node)) {
                visited.add(node);
                topSort(node, path, visited);
                path.add(node);
            }
        }
    }

    public void mst(Node start, List<Node> tree, List<Node> visited) {

    }

    public List<Node> dijkstra(Node start, Node end) {
        List<Node> visited = new LinkedList<>();
        Map<Node, Integer> weights = vertices.stream().collect(Collectors.toMap(v -> v, v -> Integer.MAX_VALUE));
        PriorityQueue<Trail> queue = new PriorityQueue<>();
        queue.add(new Trail(start, 0));
        while (!queue.isEmpty()) {
            Trail head = queue.poll();
            Node node = head.current;
            weights.put(node, head.distance);
            if (node.equals(end)) {
                break;
            }
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                if(!visited.contains(next)) {
                    next.setDepth(node.getDepth() + 1);
                    int currentDist = weights.get(next);
                    int nextDist = head.distance + edge.weight;
                    if (nextDist < currentDist) {
                        queue.add(new Trail(next, nextDist, node));
                        weights.put(next, nextDist);
                    } else {
                        queue.add(new Trail(next, currentDist, node));
                    }
                }
            }

            // remove from consideration
            visited.add(node);
        }
        return new LinkedList<>(weights.keySet());
    }
}
