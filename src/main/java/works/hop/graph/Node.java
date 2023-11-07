package works.hop.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Node {

    final List<Edge> edges = new LinkedList<>();
    String name;
    int depth = 0;
    boolean visited = false;

    public Node(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
