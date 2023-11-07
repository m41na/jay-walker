package works.hop.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Edge {

    Node node;
    int weight;

    @Override
    public String toString() {
        return String.format("node - %s", node.toString());
    }
}
