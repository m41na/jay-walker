package works.hop.graph;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * shortest path algorithms
 * 1. bfs
 * 2. dijkstras
 * 3. bellman-ford
 * 4. floyd-warshall
 */
public class Graph {

    final List<Node> vertices = new LinkedList<>();
    final Map<Node, List<Edge>> adjacent = new LinkedHashMap<>();

    public void addNode(Node node) {
        if (!this.vertices.contains(node)) {
            this.vertices.add(Objects.requireNonNull(node));
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

    public void depth_first_post_order_traversal(Node start, Consumer<Node> callback, List<Node> visited) {
        if (!visited.contains(start)) {
            visited.add(start);
            for (Edge edge : start.getEdges()) {
                Node node = edge.getNode();
                if (!visited.contains((node))) {
                    depth_first_post_order_traversal(node, callback, visited);
                    callback.accept(node);
                }
            }
        }
    }

    public void depth_first_pre_order_traversal(Node start, Consumer<Node> callback, List<Node> visited) {
        if (!visited.contains(start)) {
            visited.add(start);
            for (Edge edge : start.getEdges()) {
                Node node = edge.getNode();
                if (!visited.contains((node))) {
                    callback.accept(node);
                    depth_first_pre_order_traversal(node, callback, visited);
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
            if (node == end) {
                break;
            }
            if (visited.contains(node)) {
                // necessary check to avoid duplication in the stack
                continue;
            }
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                if (!visited.contains(next)) {
                    next.setDepth(node.getDepth() + 1);
                    stack.add(next);
                }
            }
            visited.add(node);
        }
        return visited;
    }

    public List<Node> bfs(Node start, Node end) {
        List<Node> visited = new LinkedList<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node == end) {
                break;
            }
            if (visited.contains(node)) {
                // necessary check to avoid duplication in the queue
                continue;
            }
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                if (!visited.contains(next) && !queue.contains(next)) {
                    next.setDepth(node.getDepth() + 1);
                    queue.add(next);
                }
            }
            visited.add(node);
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

    public List<Node> dag_topological_sort() {
        List<Node> sorted = new LinkedList<>();
        List<Node> visited = new LinkedList<>();
        for (Node node : vertices) {
            depth_first_post_order_traversal(node, next -> sorted.add(0, next), visited);
            if (!sorted.contains(node)) {
                sorted.add(0, node);
            }
        }
        return sorted;
    }

    public List<Node> shortest_path_unweighted_bfs(Node start, Node end) {
        Map<Node, Trail> weights = vertices.stream().collect(Collectors.toMap(
                node -> node,
                node -> new Trail(node, Integer.MIN_VALUE)
        ));

        Queue<Trail> queue = new LinkedList<>();
        Trail startNode = weights.get(start);
        startNode.setDistance(0);
        startNode.setPrev(start);
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Trail head = queue.poll();
            Node node = head.current;
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                Trail nextNode = weights.get(next);
                if (!nextNode.visited) {
                    int currentDist = head.distance + 1;
                    nextNode.setDistance(currentDist);
                    nextNode.setPrev(node);

                    if (queue.stream().noneMatch(t -> t == nextNode)) {
                        // necessary check to avoid duplication in the queue
                        queue.add(nextNode);
                    }
                }
            }
            // remove from further consideration
            weights.get(node).setVisited(true);
        }
        // recreate the shortest path
        return tracePath(weights, end, start);
    }

    public List<Node> shortest_path_weighted_dijkstra(Node start, Node end) {
        Map<Node, Trail> weights = vertices.stream().collect(Collectors.toMap(
                node -> node,
                node -> new Trail(node, Integer.MAX_VALUE)
        ));

        PriorityQueue<Trail> queue = new PriorityQueue<>();
        Trail startNode = weights.get(start);
        startNode.setDistance(0);
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Trail head = queue.poll();
            Node node = head.current;
            for (Edge edge : node.getEdges()) {
                Node next = edge.getNode();
                Trail nextNode = weights.get(next);
                if (!nextNode.visited) {
                    int nextDist = nextNode.distance;
                    int currentDist = head.distance + edge.weight;
                    if (currentDist < nextDist) {
                        nextNode.setDistance(currentDist);
                        nextNode.setPrev(node);
                    }
                    if (queue.stream().noneMatch(t -> t == nextNode)) {
                        // necessary check to avoid duplication in the queue
                        queue.add(nextNode);
                    }
                }
            }
            // remove from further consideration
            weights.get(node).setVisited(true);
        }
        // recreate the shortest path
        return tracePath(weights, end, start);
    }

    private List<Node> tracePath(Map<Node, Trail> weights, Node end, Node start) {
        Trail tail = weights.get(end);
        List<Node> path = new LinkedList<>();
        while (tail.current != start) {
            path.add(tail.current);
            tail = weights.get(tail.prev);
        }
        path.add(start);
        return path;
    }

    public void minimum_spanning_tree(Node start, List<Node> tree, List<Node> visited){

    }

    public void bridges_and_articulation_points(Node start, List<List<Node>> bridges) {
        Map<Integer, Trail> weights = IntStream.range(0, vertices.size())
                .mapToObj(i -> new Trail(vertices.get(i), i))
                .collect(Collectors.toMap(
                node -> node.distance,
                node -> node
        ));

        List<Node> visited = new LinkedList<>();
        depth_first_pre_order_traversal(weights.get(0).current, node -> {
            System.out.println(node.name);
        }, visited);
    }
}
